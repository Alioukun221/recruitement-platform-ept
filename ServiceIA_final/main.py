"""
API FastAPI pour le service IA de recrutement
"""
import os
import logging
import time
from datetime import datetime
from typing import Optional
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, BackgroundTasks, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import httpx
from dotenv import load_dotenv

from models import (
    ParseCVRequest, ParseCVResponse,
    ScoreCVRequest, ScoreCVResponse,
    ProcessCVRequest, ProcessCVResponse,
    HealthResponse
)
from services import CVParsingService, CVScoringService

# Charger les variables d'environnement
load_dotenv()

# Configuration du logger
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('ia_service.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# Variables globales pour les services
parsing_service: Optional[CVParsingService] = None
scoring_service: Optional[CVScoringService] = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Gestion du cycle de vie de l'application"""
    global parsing_service, scoring_service

    # Startup
    logger.info(" Démarrage du service IA de recrutement...")

    # Vérifier la clé API
    api_key = os.getenv("MISTRAL_API_KEY")
    if not api_key:
        logger.error(" MISTRAL_API_KEY non trouvée dans les variables d'environnement")
        raise RuntimeError("Configuration manquante: MISTRAL_API_KEY")

    # Initialiser les services
    try:
        parsing_service = CVParsingService(api_key=api_key)
        scoring_service = CVScoringService(api_key=api_key)
        logger.info(" Services initialisés avec succès")
    except Exception as e:
        logger.error(f" Erreur lors de l'initialisation des services: {e}")
        raise

    yield

    # Shutdown
    logger.info(" Arrêt du service IA de recrutement...")


# Création de  l'application FastAPI
app = FastAPI(
    title="Service IA de Recrutement",
    description="API pour le parsing et le scoring de CV avec Mistral AI",
    version="1.0.0",
    lifespan=lifespan
)

# Configuration CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ==================== ENDPOINTS ====================
@app.get("/", tags=["Root"])
async def root():
    """Endpoint racine"""
    return {
        "service": "Service IA de Recrutement",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "health": "/health",
            "parse": "/api/ia/parse-cv",
            "score": "/api/ia/score-cv",
            "process": "/api/ia/process-cv"
        }
    }

@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    """
    Vérification de la santé du service
    """
    try:
        mistral_ocr_status = "OK" if parsing_service.health_check() else "ERROR"
        mistral_chat_status = "OK" if scoring_service.health_check() else "ERROR"

        overall_status = "healthy" if (
                mistral_ocr_status == "OK" and mistral_chat_status == "OK"
        ) else "degraded"

        return HealthResponse(
            status=overall_status,
            timestamp=datetime.now(),
            version="1.0.0",
            mistral_api_status=f"OCR: {mistral_ocr_status}, Chat: {mistral_chat_status}"
        )
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Service unhealthy: {str(e)}"
        )

@app.post(
    "/api/ia/parse-cv",
    response_model=ParseCVResponse,
    tags=["Parsing"],
    status_code=status.HTTP_200_OK
)
async def parse_cv(request: ParseCVRequest):
    """
    Parse un CV et extrait les données structurées

    - **application_id**: ID de la candidature
    - **cv_base64**: CV encodé en base64
    - **filename**: Nom du fichier
    """
    logger.info(f"📄 Requête de parsing reçue pour candidature {request.application_id}")

    try:
        result = parsing_service.parse_cv(
            cv_base64=request.cv_base64,
            application_id=request.application_id
        )

        if not result.success:
            logger.warning(
                f" Parsing échoué pour candidature {request.application_id}: "
                f"{result.error_message}"
            )

        return result

    except Exception as e:
        logger.error(f" Erreur inattendue lors du parsing: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Erreur lors du parsing: {str(e)}"
        )


@app.post(
    "/api/ia/score-cv",
    response_model=ScoreCVResponse,
    tags=["Scoring"],
    status_code=status.HTTP_200_OK
)
async def score_cv(request: ScoreCVRequest):
    """
    Score un CV par rapport à une offre d'emploi

    - **application_id**: ID de la candidature
    - **parsed_cv_data**: Données du CV parsé
    - **job_offer**: Données de l'offre d'emploi
    """
    logger.info(
        f" Requête de scoring reçue pour candidature {request.application_id} "
        f"(offre: {request.job_offer.job_title})"
    )

    try:
        result = scoring_service.score_cv(
            cv_data=request.parsed_cv_data,
            job_offer=request.job_offer,
            application_id=request.application_id
        )

        if not result.success:
            logger.warning(
                f" Scoring échoué pour candidature {request.application_id}: "
                f"{result.error_message}"
            )

        return result

    except Exception as e:
        logger.error(f" Erreur inattendue lors du scoring: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Erreur lors du scoring: {str(e)}"
        )

async def send_callback(callback_url: str, data: dict):
    """
    Envoie un callback asynchrone au service Spring Boot

    Args:
        callback_url: URL du webhook
        data: Données à envoyer
    """
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(callback_url, json=data)
            response.raise_for_status()
            logger.info(f" Callback envoyé avec succès à {callback_url}")
    except Exception as e:
        logger.error(f" Erreur lors de l'envoi du callback à {callback_url}: {e}")


@app.post(
    "/api/ia/process-cv",
    response_model=ProcessCVResponse,
    tags=["Processing"],
    status_code=status.HTTP_200_OK
)
async def process_cv(
        request: ProcessCVRequest,
        background_tasks: BackgroundTasks
):
    """
    Traitement complet: Parsing + Scoring

    - **application_id**: ID de la candidature
    - **cv_base64**: CV encodé en base64
    - **filename**: Nom du fichier
    - **job_offer**: Données de l'offre d'emploi
    - **callback_url**: URL optionnelle pour callback asynchrone

    Si callback_url est fourni, le traitement se fait en arrière-plan
    et un callback est envoyé à Spring Boot avec les résultats.
    """
    logger.info(
        f" Requête de traitement complet pour candidature {request.application_id}"
    )

    start_time = time.time()

    try:
        # ÉTAPE 1: Parsing
        logger.info(f" Étape 1/2: Parsing du CV...")
        parse_result = parsing_service.parse_cv(
            cv_base64=request.cv_base64,
            application_id=request.application_id
        )

        if not parse_result.success:
            logger.error(f" Parsing échoué: {parse_result.error_message}")
            return ProcessCVResponse(
                success=False,
                application_id=request.application_id,
                parsed_data=None,
                scoring_result=None,
                error_message=f"Parsing échoué: {parse_result.error_message}",
                total_processing_time=time.time() - start_time
            )

        # ÉTAPE 2: Scoring
        logger.info(f" Étape 2/2: Scoring du CV...")
        score_result = scoring_service.score_cv(
            cv_data=parse_result.parsed_data,
            job_offer=request.job_offer,
            application_id=request.application_id
        )

        if not score_result.success:
            logger.error(f" Scoring échoué: {score_result.error_message}")
            return ProcessCVResponse(
                success=False,
                application_id=request.application_id,
                parsed_data=parse_result.parsed_data,
                scoring_result=None,
                error_message=f"Scoring échoué: {score_result.error_message}",
                total_processing_time=time.time() - start_time
            )

        total_time = time.time() - start_time

        # Créer la réponse
        response = ProcessCVResponse(
            success=True,
            application_id=request.application_id,
            parsed_data=parse_result.parsed_data,
            scoring_result=score_result.scoring_result,
            error_message=None,
            total_processing_time=total_time
        )

        logger.info(
            f" Traitement complet réussi pour candidature {request.application_id} "
            f"(Score: {score_result.scoring_result.score_global:.1f}, "
            f"Temps total: {total_time:.2f}s)"
        )

        # Si callback URL fourni, envoyer le résultat de manière asynchrone
        if request.callback_url:
            logger.info(f" Envoi du callback à {request.callback_url}")
            background_tasks.add_task(
                send_callback,
                request.callback_url,
                response.model_dump()
            )

        return response

    except Exception as e:
        total_time = time.time() - start_time
        logger.error(f" Erreur inattendue lors du traitement: {e}", exc_info=True)

        return ProcessCVResponse(
            success=False,
            application_id=request.application_id,
            parsed_data=None,
            scoring_result=None,
            error_message=f"Erreur inattendue: {str(e)}",
            total_processing_time=total_time
        )

# ==================== GESTION DES ERREURS ====================
@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc):
    """Handler pour les exceptions HTTP"""
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "success": False,
            "error": exc.detail,
            "timestamp": datetime.now().isoformat()
        }
    )

@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    """Handler pour toutes les autres exceptions"""
    logger.error(f"Erreur non gérée: {exc}", exc_info=True)
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "success": False,
            "error": "Erreur interne du serveur",
            "timestamp": datetime.now().isoformat()
        }
    )


if __name__ == "__main__":
    import uvicorn

    # Configuration du serveur
    port = int(os.getenv("PORT", 8000))
    host = os.getenv("HOST", "0.0.0.0")

    logger.info(f" Démarrage du serveur sur {host}:{port}")

    uvicorn.run(
        "main:app",
        host=host,
        port=port,
        reload=True,  # À désactiver en production
        log_level="info"
    )
