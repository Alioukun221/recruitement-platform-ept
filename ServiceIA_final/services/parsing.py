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

    def parse_cv(self, cv_base64: str, filename: str, application_id: int, save_folder="save_cvs") -> ParseCVResponse:
        start_time = time.time()
        try:


            os.makedirs(save_folder, exist_ok=True)
            pdf_path = os.path.join(save_folder, filename)

            pdf_bytes = base64.b64decode(cv_base64)
            with open(pdf_path, "wb") as f:
                f.write(pdf_bytes)


            document_url = f"data:application/pdf;base64,{cv_base64}"

            response = self.client.ocr.process(
                model=self.model,
                document=DocumentURLChunk(document_url=document_url),
                document_annotation_format=response_format_from_pydantic_model(ResumeData),
            )

            if not response.document_annotation:
                raise ValueError("OCR n'a renvoyé aucune annotation")

            response_dict = json.loads(response.document_annotation)


            try:
                parsed = ResumeData(**response_dict)
            except Exception as e:
                raise ValueError(f"Erreur validation ResumeData : {e}")

            processing_time = time.time() - start_time


            return ParseCVResponse(
                success=True,
                application_id=application_id,
                parsed_data=parsed,
                processing_time=processing_time,
                error_message=None
            )

        except Exception as e:
            processing_time = time.time() - start_time
            return ParseCVResponse(
                success=False,
                application_id=application_id,
                parsed_data=None,
                processing_time=processing_time,
                error_message=str(e)
            )








