"""
Modèles Pydantic pour le service IA de recrutement
"""
from pydantic import BaseModel, Field, validator
from typing import List, Optional, Dict, Any
from datetime import datetime
from enum import Enum


# ==================== MODÈLES DE CV (PARSING) ====================

class Education(BaseModel):
    """Modèle pour une formation académique"""
    degree: str = Field(..., description="Nom du diplôme (ex: Master en Informatique)")
    institution: str = Field(..., description="Nom de l'établissement")
    year: Optional[str] = Field(None, description="Année d'obtention")
    field_of_study: Optional[str] = Field(None, description="Domaine d'études")

    class Config:
        json_schema_extra = {
            "example": {
                "degree": "Master en Informatique",
                "institution": "Université Cheikh Anta Diop",
                "year": "2023",
                "field_of_study": "Génie Logiciel"
            }
        }

class WorkExperience(BaseModel):
    """Modèle pour une expérience professionnelle"""
    position: str = Field(..., description="Titre du poste")
    company: str = Field(..., description="Nom de l'entreprise")
    start_date: Optional[str] = Field(None, description="Date de début")
    end_date: Optional[str] = Field(None, description="Date de fin (ou 'Présent')")
    description: Optional[str] = Field(None, description="Description des responsabilités")
    achievements: Optional[List[str]] = Field(default_factory=list, description="Réalisations clés")

    class Config:
        json_schema_extra = {
            "example": {
                "position": "Développeur Full Stack",
                "company": "Tech Solutions SARL",
                "start_date": "2021-01",
                "end_date": "2023-06",
                "description": "Développement d'applications web",
                "achievements": ["Migration vers microservices", "Réduction du temps de réponse de 40%"]
            }
        }

class Project(BaseModel):
    """Modèle pour un projet"""
    name: str = Field(..., description="Nom du projet")
    description: Optional[str] = Field(None, description="Description du projet")
    technologies: Optional[List[str]] = Field(default_factory=list, description="Technologies utilisées")
    role: Optional[str] = Field(None, description="Rôle dans le projet")
    url: Optional[str] = Field(None, description="URL du projet (GitHub, démo, etc.)")

    class Config:
        json_schema_extra = {
            "example": {
                "name": "Plateforme E-commerce",
                "description": "Développement d'une plateforme de vente en ligne",
                "technologies": ["React", "Node.js", "MongoDB"],
                "role": "Lead Developer",
                "url": "https://github.com/user/ecommerce"
            }
        }

class Language(BaseModel):
    """Modèle pour une langue"""
    language: str = Field(..., description="Nom de la langue")
    level: Optional[str] = Field(None, description="Niveau (ex: Courant, Bilingue, Notions)")

class ResumeData(BaseModel):
    """Modèle complet des données extraites du CV"""

    # Formations
    education: List[Education] = Field(default_factory=list, description="Formations académiques")

    # Expériences professionnelles
    work_experience: List[WorkExperience] = Field(default_factory=list, description="Expériences professionnelles")

    # Projets
    projects: List[Project] = Field(default_factory=list, description="Projets réalisés")

    # Compétences
    competences: List[str] = Field(default_factory=list, description="Compétences techniques")
    tools: List[str] = Field(default_factory=list, description="Outils et frameworks maîtrisés")
    soft_skills: List[str] = Field(default_factory=list, description="Compétences interpersonnelles")

    # Langues
    languages: List[Language] = Field(default_factory=list, description="Langues parlées")

    # Certifications
    certifications: List[str] = Field(default_factory=list, description="Certifications professionnelles")

    # Autres
    summary: Optional[str] = Field(None, description="Résumé professionnel")

    class Config:
        json_schema_extra = {
            "example": {
                "full_name": "Moussa Kane",
                "email": "moussa.kane@example.com",
                "competences": ["Python", "Java", "React"],
                "education": [
                    {
                        "degree": "Master",
                        "institution": "UCAD",
                        "year": "2023"
                    }
                ]
            }
        }

class JobOffer(BaseModel):
    """Modèle pour une offre d'emploi"""
    job_id: int = Field(..., description="ID de l'offre d'emploi")
    job_title: str = Field(..., description="Titre du poste")
    job_type: str = Field(..., description="Type de poste (FULL_TIME, PART_TIME, etc.)")
    contract_type: str = Field(..., description="Type de contrat (CDI, CDD, etc.)")
    description: str = Field(..., description="Description du poste")
    required_skills: List[str] = Field(default_factory=list, description="Compétences requises")
    education_level: str = Field(..., description="Niveau d'études requis")
    min_experience: int = Field(..., description="Années d'expérience minimales")

    class Config:
        json_schema_extra = {
            "example": {
                "job_id": 1,
                "job_title": "Développeur Java Senior",
                "job_type": "FULL_TIME",
                "contract_type": "CDI",
                "description": "Nous recherchons un développeur Java expérimenté...",
                "required_skills": ["Java", "Spring Boot", "PostgreSQL", "Docker"],
                "education_level": "Master en Informatique",
                "min_experience": 5
            }
        }

# ==================== MODÈLES DE SCORING ====================

class ScoringResult(BaseModel):
    """Modèle pour le résultat du scoring"""
    # Scores détaillés (0-100)
    score_global: float = Field(..., ge=0, le=100, description="Score global de pertinence")
    matching_competences: float = Field(..., ge=0, le=100, description="Score de correspondance des compétences")
    matching_experience: float = Field(..., ge=0, le=100, description="Score de correspondance de l'expérience")
    matching_diploma: float = Field(..., ge=0, le=100, description="Score de correspondance du diplôme")


    # Justification détaillée
    justification: str = Field(..., description="Justification détaillée du scoring en français")

    # Recommandation
    recommendation: str = Field(..., description="Recommandation finale (EXCELLENT, BON, MOYEN, FAIBLE)")

    # Analyse détaillée
    strengths: List[str] = Field(default_factory=list, description="Points forts du candidat")
    weaknesses: List[str] = Field(default_factory=list, description="Points faibles du candidat")
    missing_skills: List[str] = Field(default_factory=list, description="Compétences manquantes")

    @validator('recommendation')
    def validate_recommendation(cls, v):
        valid_recommendations = ['EXCELLENT', 'BON', 'MOYEN', 'FAIBLE']
        if v not in valid_recommendations:
            raise ValueError(f'Recommandation doit être parmi: {valid_recommendations}')
        return v

    class Config:
        json_schema_extra = {
            "example": {
                "score_global": 85.5,
                "matching_competences": 90,
                "matching_experience": 80,
                "matching_diploma": 85,
                "justification": "Candidat excellent avec une forte correspondance...",
                "recommendation": "EXCELLENT",
                "strengths": ["Maîtrise de Java", "Expérience en microservices"],
                "weaknesses": ["Manque d'expérience en Kubernetes"],
                "missing_skills": ["Kubernetes", "AWS"]
            }
        }

# ==================== MODÈLES DE REQUÊTES/RÉPONSES API ====================

class ParseCVRequest(BaseModel):
    """Requête pour parser un CV"""
    application_id: int = Field(..., description="ID de la candidature")
    cv_base64: str = Field(..., description="CV encodé en base64")
    filename: str = Field(..., description="Nom du fichier")

    class Config:
        json_schema_extra = {
            "example": {
                "application_id": 25,
                "cv_base64": "JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PC...",
                "filename": "cv_abdou_ndiaye.pdf"
            }
        }


class ParseCVResponse(BaseModel):
    """Réponse du parsing de CV"""
    success: bool = Field(..., description="Succès de l'opération")
    application_id: int = Field(..., description="ID de la candidature")
    parsed_data: Optional[ResumeData] = Field(None, description="Données extraites du CV")
    error_message: Optional[str] = Field(None, description="Message d'erreur si échec")
    processing_time: float = Field(..., description="Temps de traitement en secondes")

    class Config:
        json_schema_extra = {
            "example": {
                "success": True,
                "application_id": 25,
                "parsed_data": {"full_name": "Moussa Fall", "competences": ["Python"]},
                "error_message": None,
                "processing_time": 2.5
            }
        }

class ScoreCVRequest(BaseModel):
    """Requête pour scorer un CV"""
    application_id: int = Field(..., description="ID de la candidature")
    parsed_cv_data: ResumeData = Field(..., description="Données du CV parsé")
    job_offer: JobOffer = Field(..., description="Données de l'offre d'emploi")

    class Config:
        json_schema_extra = {
            "example": {
                "application_id": 25,
                "parsed_cv_data": {
                    "full_name": "Moussa Fall",
                    "competences": ["Python", "Java"]
                },
                "job_offer": {
                    "job_id": 1,
                    "job_title": "Développeur Python",
                    "required_skills": ["Python", "Django"]
                }
            }
        }

class ScoreCVResponse(BaseModel):
    """Réponse du scoring de CV"""
    success: bool = Field(..., description="Succès de l'opération")
    application_id: int = Field(..., description="ID de la candidature")
    scoring_result: Optional[ScoringResult] = Field(None, description="Résultat du scoring")
    error_message: Optional[str] = Field(None, description="Message d'erreur si échec")
    processing_time: float = Field(..., description="Temps de traitement en secondes")

    class Config:
        json_schema_extra = {
            "example": {
                "success": True,
                "application_id": 25,
                "scoring_result": {
                    "score_global": 85.5,
                    "matching_competences": 90,
                    "recommendation": "EXCELLENT"
                },
                "error_message": None,
                "processing_time": 3.2
            }
        }

class ProcessCVRequest(BaseModel):
    """Requête pour traiter un CV (parsing + scoring)"""
    application_id: int = Field(..., description="ID de la candidature")
    cv_base64: str = Field(..., description="CV encodé en base64")
    filename: str = Field(..., description="Nom du fichier")
    job_offer: JobOffer = Field(..., description="Données de l'offre d'emploi")
    callback_url: Optional[str] = Field(None, description="URL de callback pour le résultat")

    class Config:
        json_schema_extra = {
            "example": {
                "application_id": 25,
                "cv_base64": "JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PC...",
                "filename": "cv_fatima_dieye.pdf",
                "job_offer": {
                    "job_id": 1,
                    "job_title": "Développeur Python"
                },
                "callback_url": "http://localhost:8080/api/webhook/ia-result"
            }
        }

class ProcessCVResponse(BaseModel):
    """Réponse du traitement complet de CV"""
    success: bool = Field(..., description="Succès de l'opération")
    application_id: int = Field(..., description="ID de la candidature")
    parsed_data: Optional[ResumeData] = Field(None, description="Données extraites du CV")
    scoring_result: Optional[ScoringResult] = Field(None, description="Résultat du scoring")
    error_message: Optional[str] = Field(None, description="Message d'erreur si échec")
    total_processing_time: float = Field(..., description="Temps total de traitement en secondes")

    class Config:
        json_schema_extra = {
            "example": {
                "success": True,
                "application_id": 25,
                "parsed_data": {"full_name": "Fatima Dieye"},
                "scoring_result": {"score_global": 85.5},
                "error_message": None,
                "total_processing_time": 5.7
            }
        }

class HealthResponse(BaseModel):
    """Réponse du healthcheck"""
    status: str = Field(..., description="Statut du service")
    timestamp: datetime = Field(..., description="Timestamp")
    version: str = Field(..., description="Version de l'API")
    mistral_api_status: str = Field(..., description="Statut de l'API Mistral")