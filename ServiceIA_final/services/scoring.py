"""
Service de scoring de CV avec Mistral AI
"""
import json
import logging
import time

from mistralai import Mistral
from mistralai.extra import response_format_from_pydantic_model

from models import ResumeData, JobOffer, ScoreCVResponse, ScoringResult

logger = logging.getLogger(__name__)

class CVScoringService:
    """Service pour scorer les CV par rapport aux offres d'emploi"""

    def __init__(self, api_key: str):
        """
        Initialise le service de scoring

        Args:
            api_key: Clé API Mistral
        """
        self.client = Mistral(api_key=api_key)
        self.model = "magistral-small-latest"
        logger.info(f" CVScoringService initialisé avec le modèle {self.model}")

    def health_check(self) -> bool:
        """
        Vérifie que le service est opérationnel

        Returns:
            True si le service fonctionne
        """
        try:
            return self.client is not None
        except Exception as e:
            logger.error(f"Health check failed: {e}")
            return False

    def score_cv(self,
                 cv_data: ResumeData,
                 job_offer: JobOffer,
                 application_id: int)-> ScoreCVResponse:

        """
       Score un CV par rapport à une offre d'emploi

       Args:
           cv_data: Données du CV parsé
           job_offer: Offre d'emploi
           application_id: ID de la candidature

       Returns:
           ScoreCVResponse avec le scoring détaillé
       """

        start_time = time.time()

        try:
            logger.info(
                f"Scoring du CV pour candidature {application_id} "
                f"(Offre: {job_offer.job_title})"
            )
            # Créer le prompt pour le scoring
            prompt = self._create_scoring_prompt(cv_data, job_offer)

            # Appeler l'API Mistral
            logger.info(" Appel à l'API Mistral pour scoring...")
            chat_response = self.client.chat.complete(
                messages=[
                    {"role": "system", "content": "You are an AI recruiter Expert."},
                    {"role": "user", "content": prompt}
                ]
                ,
                model=self.model,
                response_format=response_format_from_pydantic_model(ScoringResult)
            )
            scoring_json = json.loads(chat_response.choices[0].message.content)
            logger.debug(f"Réponse brute de Mistral: {scoring_json}")

            # Créer l'objet ScoringResult
            scoring_result = self._create_scoring_result(scoring_json)

            processing_time = time.time() - start_time
            logger.info(
                f"Scoring réussi pour {application_id} "
                f"(Score global: {scoring_result.score_global:.1f}, "
                f"Recommandation: {scoring_result.recommendation}, "
                f"Temps: {processing_time:.2f}s)"
            )
            return ScoreCVResponse(
                success=True,
                application_id=application_id,
                scoring_result=scoring_result,
                processing_time=processing_time,
                error_message=None
            )
        except json.JSONDecodeError as e:
            processing_time = time.time() - start_time
            error_msg = f"Erreur de parsing JSON du scoring: {str(e)}"
            logger.error(f" {error_msg}")

            return ScoreCVResponse(
                success=False,
                application_id=application_id,
                scoring_result=None,
                processing_time=processing_time,
                error_message=error_msg
            )

        except Exception as e:
            processing_time = time.time() - start_time
            error_msg = f"Erreur lors du scoring: {str(e)}"
            logger.error(f" {error_msg}", exc_info=True)

            return ScoreCVResponse(
                success=False,
                application_id=application_id,
                scoring_result=None,
                processing_time=processing_time,
                error_message=error_msg
            )

    def _create_scoring_prompt(self,
                               cv_data: ResumeData,
                               job_offer: JobOffer)->str:
        """
       Créer le prompt pour le scoring

       Args:
           cv_data: Données du CV
           job_offer: Offre d'emploi

       Returns:
           Le prompt formaté
       """


        # === Extraction sélective des données importantes ===
        competences = ", ".join(cv_data.competences)
        tools = ", ".join(cv_data.tools)
        soft_skills = ", ".join(cv_data.soft_skills)
        certifications = ", ".join(cv_data.certifications)
        summary = cv_data.summary or "Non spécifié"

        education_str = "; ".join([
            f"{edu.degree} en {getattr(edu, 'field', '')} - {edu.institution} ({edu.year})"
            for edu in cv_data.education
        ])

        experience_str = "; ".join([
            f"{exp.position} chez {exp.company} ({exp.duration or 'durée non précisée'})"
            for exp in cv_data.work_experience
        ])

        projects_str = "; ".join([
            f"{proj.name}: {proj.description}..."
            for proj in cv_data.projects
        ])
        languages_str = ", ".join([
            f"{lang.language} ({lang.level})"
            for lang in cv_data.languages
        ])

        # === Informations de l’offre d’emploi ===
        job_title = job_offer.job_title or "Non spécifié"
        job_requirements = ", ".join(job_offer.required_skills or [])
        job_description = job_offer.description or "Non spécifié"
        job_experience = job_offer.min_experience or "Non précisé"
        job_education = job_offer.education_level or "Non précisé"


        return f"""Tu es un expert en recrutement. Analyse ce CV par rapport à cette offre d'emploi et fournis un scoring détaillé en français.

               === OFFRE D'EMPLOI ===
                - Poste : {job_title}
                - Compétences requises : {job_requirements}
                - Expérience minimale : {job_experience} ans
                - Niveau d'étude requis : {job_education}
                - Description du metier : {job_description}
                
                === CV DU CANDIDAT ===
                    Résumé : {summary}
                    Compétences techniques : {competences}
                    Outils : {tools}
                    Soft skills : {soft_skills}
                    Certifications : {certifications}
                    Formations : {education_str}
                    Expériences : {experience_str}
                    Projets récents : {projects_str}
                    Langues : {languages_str}
                                    
                === MISSION ===
                Analyse la correspondance entre le CV et l'offre d'emploi selon 3 critères principaux:
                1. **Correspondance des compétences** (0-100): Analyse des compétences techniques et outils
                2. **Correspondance de l'expérience** (0-100): Analyse des années d'expérience et pertinence des postes
                3. **Correspondance du diplôme** (0-100): Analyse du niveau d'études et domaine
                
                Calcule ensuite un **score global** pondéré:
                - Compétences: 40%
                - Expérience: 35%
                - Diplôme: 25%
                
               
                RÈGLES DE SCORING:
                - **EXCELLENT** (85-100): Profil idéal, correspondance exceptionnelle
                - **BON** (70-84): Très bon profil, forte correspondance
                - **MOYEN** (50-69): Profil acceptable, correspondance partielle
                - **FAIBLE** (0-49): Profil inadapté, faible correspondance
                
                ANALYSE DES COMPÉTENCES:
                - Compare les compétences requises avec competences, tools et soft_skills du CV
                - Bonus si compétences additionnelles pertinentes
                - Pénalité si compétences critiques manquantes
                
                ANALYSE DE L'EXPÉRIENCE:
                - Compare min_experience (années requises) avec work_experience du CV
                - Évalue la pertinence des postes occupés
                - Analyse la progression de carrière
                
                ANALYSE DU DIPLÔME:
                - Compare education_level requis avec education du CV
                - Évalue la pertinence du field_of_study
                - Considère les certifications pertinentes
                
                La justification doit être en français, professionnelle et factuelle.
                Les strengths et weaknesses doivent être spécifiques et basés sur le CV.
                Les missing_skills doivent lister les compétences requises mais absentes du CV.
                
                Réponds UNIQUEMENT avec le JSON, rien d'autre."""

    def _create_scoring_result(self,
                               scoring_json: dict)->ScoringResult:
        """
        Transforme le JSON de scoring en objet ScoringResult

        Args:
            scoring_json: Données JSON du scoring

        Returns:
            Objet ScoringResult
        """

        return ScoringResult(
            score_global=float(scoring_json.get("score_global", 0)),
            matching_competences=float(scoring_json.get("matching_competences", 0)),
            matching_experience=float(scoring_json.get("matching_experience", 0)),
            matching_diploma=float(scoring_json.get("matching_diploma", 0)),
            justification=scoring_json.get("justification", ""),
            recommendation=scoring_json.get("recommendation", ""),
            strengths=scoring_json.get("strengths", []),
            weaknesses=scoring_json.get("weaknesses", []),
            missing_skills=scoring_json.get("missing_skills", [])
        )