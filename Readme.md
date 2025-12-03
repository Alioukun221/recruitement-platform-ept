Backend du Site de Recrutement - EPT
API backend pour la gestion du recrutement à l'École Polytechnique de Thiès (EPT), développée avec Spring Boot.
📋Table des matières

Vue d'ensemble
Fonctionnalités
Technologies utilisées
Prérequis
Installation
Configuration
Structure du projet
API Documentation
Authentification
Statuts

🎯 Vue d'ensemble
Cette application backend gère l'ensemble du processus de recrutement de l'EPT, depuis la publication des offres d'emploi jusqu'à l'évaluation des candidats par des commissions de recrutement. Le système intègre une fonctionnalité de scoring automatique des CV par intelligence artificielle.
✨ Fonctionnalités
Pour les RH

Création et gestion des offres d'emploi (brouillon, publication, suspension, archivage)
Gestion des candidatures avec filtrage par statut et score IA
Création et administration des commissions de recrutement
Dashboard analytique avec statistiques détaillées
Présélection automatique et manuelle des candidats
Suivi des évaluations des commissions

Pour les Membres de Commission

Consultation des candidatures présélectionnées
Évaluation multi-critères des candidats (compétences, expérience, diplôme, motivation, soft skills)
Ajout de commentaires détaillés
Consultation du CV et des informations des candidats

Pour les Candidats

Consultation des offres d'emploi disponibles
Recherche d'offres par mot-clé
Soumission de candidature avec CV
Suivi de l'historique des candidatures
Retrait de candidature (selon le statut)

Scoring IA

Analyse automatique des CV via webhook
Scoring multi-critères (compétences, expérience, diplôme, motivation)
Recommandations et justifications détaillées
Identification des forces et faiblesses

🛠 Technologies utilisées

Framework: Spring Boot 3.x
Sécurité: Spring Security avec JWT
Base de données: MySQL (Aiven Cloud)
ORM: Hibernate/JPA
Documentation API: OpenAPI 3.0 (Swagger)
Build: Maven
Java: 17+

📦 Prérequis

Java 17 ou supérieur
Maven 3.8+
MySQL 8.0+
Service IA externe pour le scoring des CV (optionnel)

🚀 Installation

Cloner le repository

bashgit clone <repository-url>
cd backend

Configurer les variables d'environnement

Créez un fichier application.properties dans src/main/resources/ :
propertiesspring.application.name=backend

# ===============================
# Database Configuration
# ===============================
spring.datasource.url=jdbc:mysql://[YOUR_DB_HOST]:[PORT]/[DATABASE_NAME]?ssl-mode=REQUIRED
spring.datasource.username=[YOUR_DB_USERNAME]
spring.datasource.password=[YOUR_DB_PASSWORD]
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ===============================
# JPA / Hibernate Configuration
# ===============================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# ===============================
# Spring Boot Server
# ===============================
server.port=8080
server.servlet.context-path=/api/v1
server.url=http://localhost:8080

# ===============================
# IA Service Configuration
# ===============================
ia.service.url=http://localhost:8000
ia.service.timeout=30000

# ===============================
# Auditing
# ===============================
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# ===============================
# Security Configuration
# ===============================
application.security.jwt.secret_key=[YOUR_JWT_SECRET_KEY]
application.security.jwt.expiration=86400000

# ===============================
# Logging
# ===============================
logging.level.org.springframework.security=DEBUG

# ===============================
# File Upload Configuration
# ===============================
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

Note importante: Remplacez les valeurs entre crochets par vos propres configurations. Ne commitez jamais ce fichier avec vos informations sensibles. Ajoutez application.properties à votre .gitignore.


Installer les dépendances

bashmvn clean install

Lancer l'application

bashmvn spring-boot:run


L'API sera accessible sur http://localhost:8080/api/v1

## ⚙ Configuration

### Base de données

- Configurez votre base de données MySQL
- Le schéma est créé automatiquement au démarrage grâce à spring.jpa.hibernate.ddl-auto=update
- Pour la production, utilisez validate ou none au lieu de update

### JWT

- Générez une clé secrète forte pour application.security.jwt.secret_key
- L'expiration par défaut est de 24 heures (86400000 ms)
- Vous pouvez ajuster la durée selon vos besoins

### Upload de fichiers

- *Taille maximale des fichiers*: 5MB
- *Taille maximale des requêtes*: 5MB
- Ajustable selon vos besoins dans application.properties

### Description des modules

#### 📦 *auth* - Authentification
Gère l'inscription, la connexion et la génération de tokens JWT pour tous les types d'utilisateurs (Candidat, RH, Membre de Commission).

#### 📦 *candidat* - Gestion des candidats
Permet aux candidats de consulter les offres, postuler, suivre leurs candidatures et retirer leurs candidatures.

#### 📦 *commission* - Gestion des commissions
Permet aux membres de commission d'évaluer les candidats présélectionnés avec un système de notation multi-critères.

#### 📦 *config* - Configuration
Contient toutes les configurations de sécurité (JWT, CORS), OpenAPI/Swagger et autres configurations globales de l'application.

#### 📦 *dashboard* - Tableau de bord RH
Fournit des statistiques et des analytics détaillées sur les offres, candidatures et commissions pour les RH.

#### 📦 *entity* - Entités JPA
Définit toutes les entités de base de données avec leurs relations (User, JobOffer, Application, Commission, Evaluation, etc.).

#### 📦 *enums* - Énumérations
Regroupe toutes les énumérations utilisées dans l'application (statuts, types, rôles).

#### 📦 *exception* - Gestion des erreurs
Centralise la gestion des exceptions et fournit des réponses d'erreur standardisées.

#### 📦 *ia* - Intelligence Artificielle
Gère l'intégration avec le service externe d'IA pour le scoring automatique des CV via webhook.

#### 📦 *rh* - Ressources Humaines
Module principal pour les RH permettant de gérer les offres d'emploi, les candidatures et les commissions de recrutement.

## 📖 API Documentation

### Base URL

http://localhost:8080/api/v1
Principaux endpoints
Authentification

POST /auth/register/candidate - Inscription candidat
POST /auth/register/rh - Inscription RH
POST /auth/register/commission-member - Inscription membre de commission
POST /auth/authenticate - Connexion

Gestion des offres (RH)

GET /rh/job-offers - Liste des offres
POST /rh/create-job-offers - Créer une offre
GET /rh/job-offers/{id} - Détails d'une offre
PUT /rh/update-job-offers/{id} - Modifier une offre
DELETE /rh/{id} - Supprimer une offre
GET /rh/dashboard - Dashboard RH

Gestion des candidatures (RH)

GET /rh/applications/job-offer/{jobOfferId} - Liste des candidatures
GET /rh/applications/{applicationId} - Détails d'une candidature
PUT /rh/applications/{applicationId}/status - Changer le statut
POST /rh/applications/job-offer/{jobOfferId}/shortlist - Présélectionner
PUT /rh/applications/{applicationId}/reject - Rejeter
GET /rh/applications/job-offer/{jobOfferId}/stats - Statistiques
GET /rh/applications/job-offer/{jobOfferId}/by-score?minScore={score} - Filtrer par score

Gestion des commissions (RH)

GET /rh/commissions - Liste des commissions
POST /rh/commissions - Créer une commission
GET /rh/commissions/{commissionId} - Détails d'une commission
PUT /rh/commissions/{commissionId} - Modifier une commission
DELETE /rh/commissions/{commissionId} - Supprimer une commission
POST /rh/commissions/{commissionId}/members - Ajouter un membre
DELETE /rh/commissions/{commissionId}/members/{memberId} - Retirer un membre
PUT /rh/commissions/{commissionId}/president/{memberId} - Changer le président
GET /rh/commissions/get_all_commissions_members - Liste tous les membres

Évaluations (Membre de commission)

GET /commission-member/my-commissions - Mes commissions
GET /commission-member/commissions/{commissionId}/applications - Candidatures à évaluer
GET /commission-member/commissions/{commissionId}/applications/{applicationId} - Détails candidature
POST /commission-member/commissions/{commissionId}/applications/{applicationId}/evaluate - Créer évaluation
PUT /commission-member/commissions/{commissionId}/applications/{applicationId}/evaluate - Modifier évaluation
GET /commission-member/commissions/{commissionId}/applications/{applicationId}/my-evaluation - Ma note
DELETE /commission-member/commissions/{commissionId}/applications/{applicationId}/my-evaluation - Supprimer évaluation
GET /commission-member/commissions/{commissionId}/applications/{applicationId}/cv - Télécharger CV

Candidats

GET /candidate/job-offers - Liste des offres disponibles
GET /candidate/job-offers/{jobOfferId} - Détails d'une offre
GET /candidate/job-offers/search?keyword={keyword} - Rechercher des offres
POST /candidate/job-offers/{jobOfferId}/apply - Postuler (multipart/form-data)
GET /candidate/my-applications - Mes candidatures
GET /candidate/my-applications/{applicationId} - Détails de ma candidature
DELETE /candidate/my-applications/{applicationId} - Retirer ma candidature

Webhook

POST /webhook/ia-result - Réception des résultats du scoring IA

🔐 Authentification
L'API utilise JWT (JSON Web Tokens) pour l'authentification.
Inscription et connexion

Inscription (exemple candidat)

bashPOST /auth/register/candidate
Content-Type: application/json

{
"firstName": "Jean",
"lastName": "Dupont",
"email": "jean.dupont@example.com",
"password": "MotDePasse123!",
"phoneNumber": "+221771234567",
"adress": "Dakar, Sénégal"
}

Connexion

bashPOST /auth/authenticate
Content-Type: application/json

{
"email": "jean.dupont@example.com",
"password": "MotDePasse123!"
}
Réponse:
json{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
"role": "CANDIDATE"
}

Utilisation du token

Pour les endpoints protégés, incluez le token dans le header:
bashAuthorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Types d'utilisateurs
L'API supporte trois types d'utilisateurs:

CANDIDATE: Candidat aux offres d'emploi
RH: Personnel des ressources humaines
COMMISSION_MEMBER: Membre d'une commission de recrutement

📊 Statuts
Statuts des offres d'emploi

DRAFT: Brouillon (non visible publiquement)
PUBLISHED: Publiée (visible aux candidats)
SUSPENDED: Suspendue temporairement
ARCHIVED: Archivée (fermée définitivement)

Types d'emploi

PER: Personnel d'Enseignement et de Recherche
PATS: Personnel Administratif, Technique et de Service
CONTRACTUEL: Contractuel

Types de contrat

CDD: Contrat à Durée Déterminée
CDI: Contrat à Durée Indéterminée
STAGE: Stage

Statuts des candidatures

DRAFT: Brouillon
SUBMITTED: Soumise (en attente de traitement)
UNDER_REVIEW: En cours d'examen par RH
AI_SCORED: Notée par l'IA
SHORTLISTED: Présélectionnée pour commission
INTERVIEW_SCHEDULED: Entretien programmé
INTERVIEW_COMPLETED: Entretien terminé
ACCEPTED: Acceptée
REJECTED: Rejetée
WITHDRAWN: Retirée par le candidat

Statuts des commissions

ACTIVE: Active (en cours d'évaluation)
CLOSED: Fermée (évaluations terminées)
ARCHIVED: Archivée

🎯 Exemples d'utilisation
Créer une offre d'emploi
bashPOST /rh/create-job-offers
Authorization: Bearer [TOKEN_RH]
Content-Type: application/json

{
"jobTitle": "Enseignant-Chercheur en Informatique",
"jobType": "PER",
"typeContrat": "CDI",
"description": "Nous recherchons un enseignant-chercheur...",
"requiredSkills": "Machine Learning, Python, Java",
"niveauEtudeRequis": "Doctorat",
"experienceMin": 3,
"dateLimite": "2024-12-31T23:59:59"
}
Postuler à une offre
bashPOST /candidate/job-offers/{jobOfferId}/apply
Authorization: Bearer [TOKEN_CANDIDATE]
Content-Type: multipart/form-data

Form Data:
- cv: [fichier PDF]
- firstName: Jean
- lastName: Dupont
- email: jean.dupont@example.com
- phoneNumber: +221771234567
- highestDegree: Master
- majorField: Informatique
- motivationEcole: Je souhaite rejoindre l'EPT car...
- motivationPosition: Ce poste correspond à...
  Évaluer un candidat
  bashPOST /commission-member/commissions/{commissionId}/applications/{applicationId}/evaluate
  Authorization: Bearer [TOKEN_COMMISSION]
  Content-Type: application/json

{
"competenceScore": 4,
"experienceScore": 3,
"diplomaScore": 5,
"motivationScore": 4,
"softSkillsScore": 4,
"comment": "Excellent profil technique avec une bonne expérience..."
}
🤝 Contribution
Pour contribuer au projet:

Créez une branche pour votre fonctionnalité
Committez vos changements
Poussez vers la branche
Ouvrez une Pull Request

📝 License
MIT License
👨‍💻 Auteurs

Mouhamadou Aliou BA - bmouhamadoualiou@ept.sn
Fa Syaka Diouf -  fsd@ept.sn


Note: Ce projet est développé dans le cadre du système de recrutement de l'École Polytechnique de Thiès.


# 🤖 Service IA de Recrutement

Service d'intelligence artificielle pour l'analyse et le scoring automatique de CV utilisant Mistral AI.

## 📋 Description

Ce service FastAPI offre des capacités d'analyse de CV par IA :
- Parsing de CV : Extraction structurée des informations (expériences, compétences, formations, etc.)
- Scoring automatique : Évaluation de la pertinence d'un candidat par rapport à une offre d'emploi
- Traitement complet : Pipeline intégré parsing + scoring avec callback asynchrone

## 🚀 Fonctionnalités

### Parsing de CV
- Extraction automatique des données structurées depuis des CV PDF
- Support de l'OCR pour les documents scannés via Mistral AI
- Analyse des sections : expériences, formations, compétences, projets, langues, certifications

### Scoring Intelligent
- Évaluation multi-critères (compétences, expérience, diplôme)
- Score global de pertinence (0-100)
- Recommandations automatiques (EXCELLENT, BON, MOYEN, FAIBLE)
- Analyse détaillée des forces, faiblesses et compétences manquantes

### Intégration
- API REST complète avec documentation OpenAPI
- Support des callbacks asynchrones pour intégration avec Spring Boot
- Traitement en arrière-plan avec BackgroundTasks

## 🛠 Technologies

- Framework : FastAPI 0.115.0
- Serveur : Uvicorn 0.32.0
- IA : Mistral AI 1.9.10
- Validation : Pydantic 2.10.3
- HTTP Async : httpx >= 0.28.1
- Retry Logic : Tenacity 9.0.0
- Configuration : python-dotenv 1.0.1
- Logging : python-json-logger 3.1.0

## 📦 Installation

### Prérequis
- Python 3.9+
- Clé API Mistral AI

### Configuration

1. Cloner le projet
   bash
   git clone <repository-url>
   cd ia-service


2. Créer un environnement virtuel
   bash
   python -m venv venv
   source venv/bin/activate  # Linux/Mac
   venv\Scripts\activate     # Windows


3. Installer les dépendances
   bash
   pip install -r requirements.txt


4. Configurer les variables d'environnement

Créer un fichier .env à la racine :
env
MISTRAL_API_KEY=your_mistral_api_key_here


## 🎯 Utilisation

### Démarrage du service

bash
# Mode développement (avec rechargement automatique)
uvicorn main:app --reload --host 0.0.0.0 --port 8000

# Mode production
uvicorn main:app --host 0.0.0.0 --port 8000 --workers 4


Le service sera accessible sur http://localhost:8000

### Documentation API

Une fois le service démarré, accédez à :
- Swagger UI : http://localhost:8000/docs
- ReDoc : http://localhost:8000/redoc

## 📡 Endpoints

### Root
http
GET /

Informations sur le service et ses endpoints.

### Health Check
http
GET /health

Vérification de l'état du service et de l'API Mistral.

Réponse :
json
{
"status": "healthy",
"timestamp": "2024-12-03T10:30:00",
"version": "1.0.0",
"mistral_api_status": "OCR: OK, Chat: OK"
}


### Traitement Complet (Parsing + Scoring)
http
POST /api/ia/process-cv


Requête :
json
{
"application_id": 25,
"cv_base64": "JVBERi0xLjQKJeLjz9MK...",
"filename": "cv_candidat.pdf",
"job_offer": {
"job_id": 1,
"job_title": "Développeur Full Stack",
"job_type": "FULL_TIME",
"contract_type": "CDI",
"description": "Nous recherchons un développeur...",
"required_skills": ["Python", "FastAPI", "React", "PostgreSQL"],
"education_level": "Master en Informatique",
"min_experience": 3
},
"callback_url": "http://localhost:8080/api/webhook/ia-result"
}


Réponse :
json
{
"success": true,
"application_id": 25,
"scoring_result": {
"score_global": 85.5,
"matching_competences": 90.0,
"matching_experience": 85.0,
"matching_diploma": 80.0,
"justification": "Le candidat présente un excellent profil...",
"recommendation": "EXCELLENT",
"strengths": [
"Solide expérience en développement web",
"Maîtrise des technologies requises"
],
"weaknesses": [
"Peu d'expérience en gestion d'équipe"
],
"missing_skills": ["Kubernetes"]
},
"error_message": null,
"total_processing_time": 5.7
}


## 🏗 Architecture
ia-service/
├── main.py              # Point d'entrée FastAPI
├── models.py            # Modèles Pydantic
├── services/
│   ├── _init_.py
│   ├── cv_parsing.py    # Service de parsing
│   └── cv_scoring.py    # Service de scoring
├── requirements.txt
├── .env
├── .gitignore
└── README.md





## 📊 Modèles de Données

### ResumeData
Données structurées extraites du CV :
- Formations (diplômes, établissements, années)
- Expériences professionnelles (postes, entreprises, durées, réalisations)
- Projets (nom, description, technologies, rôle)
- Compétences techniques et soft skills
- Langues et certifications
- Résumé professionnel

### ScoringResult
Résultat de l'évaluation :
- Scores détaillés (compétences, expérience, diplôme)
- Score global (0-100)
- Recommandation (EXCELLENT/BON/MOYEN/FAIBLE)
- Analyse : forces, faiblesses, compétences manquantes

## 🔒 Sécurité

- ✅ Validation stricte des données avec Pydantic
- ✅ Gestion sécurisée des clés API via variables d'environnement
- ✅ CORS configuré (à restreindre en production)
- ✅ Gestion des erreurs centralisée
- ✅ Logging des opérations sensibles

## 📈 Performance

- Parsing : ~2-4 secondes par CV
- Scoring : ~2-3 secondes par évaluation
- Traitement complet : ~5-7 secondes

Temps variable selon la complexité du CV et la charge de l'API Mistral

## 🐛 Dépannage

### Erreur : "MISTRAL_API_KEY non trouvée"
➡ Vérifier que le fichier .env existe et contient la clé API

### Erreur : "Service unhealthy"
➡ Vérifier la connexion à l'API Mistral et la validité de la clé

### Timeout lors du traitement
➡ Augmenter le timeout dans la configuration httpx (défaut: 30s)

## 📝 Licence

Ce projet est sous licence MIT.

## 👥 Contribution

Les contributions sont les bienvenues ! Merci de :
1. Forker le projet
2. Créer une branche (git checkout -b feature/amelioration)
3. Commiter les changements (git commit -m 'Ajout fonctionnalité')
4. Pousser la branche (git push origin feature/amelioration)
5. Ouvrir une Pull Request


---

Version : 1.0.0  
Dernière mise à jour : Décembre 2025
