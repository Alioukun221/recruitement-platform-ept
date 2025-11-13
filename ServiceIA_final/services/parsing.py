"""
Service de parsing de CV avec Mistral AI OCR
"""

import os
import json
import base64
import logging
import time
import uuid
from typing import Optional

from dotenv import load_dotenv
from mistralai import Mistral, DocumentURLChunk
from mistralai.extra import response_format_from_pydantic_model


from models import ResumeData,ParseCVResponse

load_dotenv()
logger = logging.getLogger(__name__)

class CVParsingService:
    """Service pour parser les CV en utilisant Mistral OCR"""

    def __init__(self, api_key: Optional[str] = None):
        """
        Initialise le service de parsing de CV.

        Args:
            api_key: Clé API Mistral
        """
        self.api_key = api_key or os.getenv("MISTRAL_API_KEY")
        self.client = Mistral(api_key=self.api_key)
        self.model = "mistral-ocr-latest"
        logger.info(f" CVParsingService initialisé avec le modèle {self.model}")

    def health_check(self) -> bool:
        """Vérifie si le service est opérationnel"""
        try:
            return self.client is not None
        except Exception as e:
            logger.error(f"Health check failed: {e}")
            return False

    def parse_cv(self, cv_path: str, application_id: int, save_folder="save_cvs") -> ParseCVResponse:
        """
        Parse un CV PDF et extrait les données structurées.

        Args:
            :param cv_path: Chemin du fichier PDF à parser
            :param application_id: ID de la candidature
            :param save_folder:

        Returns:
            ParseCVResponse avec les données extraites

        """
        start_time = time.time()

        try:
            logger.info(f" Lecture du CV pour la candidature {application_id} : {cv_path}")

            # Encoder le PDF en base64
            with open(cv_path, "rb") as f:
                encoded_pdf = base64.b64encode(f.read()).decode("utf-8")

            document_url = f"data:application/pdf;base64,{encoded_pdf}"

            logger.info(" Appel à Mistral OCR...")
            response = self.client.ocr.process(
                model=self.model,
                document=DocumentURLChunk(document_url=document_url),
                document_annotation_format=response_format_from_pydantic_model(ResumeData),
            )

            # Convertir la réponse JSON en dict
            response_dict = json.loads(response.document_annotation)

            # Récupère le nom du fichier depuis le chemin
            original_filename = os.path.basename(cv_path)
            # Génère un UUID unique
            unique_id = str(uuid.uuid4())
            # Crée le nom de fichier final pour le JSON
            filename = f"{unique_id}_{original_filename}.json"

            # Crée le dossier de sauvegarde s'il n'existe pas
            os.makedirs(save_folder, exist_ok=True)
            filepath = os.path.join(save_folder, filename)

            # Sauvegarde locale
            with open(filepath, "w", encoding="utf-8") as f:
                json.dump(response_dict, f, indent=2, ensure_ascii=False)

            processing_time = time.time() - start_time

            logger.info(
                f" OCR réussi pour la candidature {application_id} "
                f"(Temps total: {processing_time:.2f}s)"
            )

            return ParseCVResponse(
                success=True,
                application_id=application_id,
                parsed_data= ResumeData(**response_dict),
                processing_time=processing_time,
                error_message=None,
            )

        except Exception as e:
            processing_time = time.time() - start_time
            error_msg = f"Erreur lors du parsing du CV: {str(e)}"
            logger.error(f" {error_msg}", exc_info=True)

            return ParseCVResponse(
                success=False,
                application_id=application_id,
                parsed_data=None,
                processing_time=processing_time,
                error_message=error_msg,
            )








