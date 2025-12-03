Backend du Site de Recrutement - EPT
API backend pour la gestion du recrutement à l'École Polytechnique de Thiès (EPT), développée avec Spring Boot.
📋 Table des matières

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

## 📁 Structure du projet

backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ept/recrutement/
│   │   │       ├── auth/                    # Module d'authentification
│   │   │       │   ├── controller/
│   │   │       │   │   └── AuthController.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── LoginRequest.java
│   │   │       │   │   ├── RegistrationRequest.java
│   │   │       │   │   ├── RhRequest.java
│   │   │       │   │   ├── CommissionMemberRequest.java
│   │   │       │   │   └── AuthResponse.java
│   │   │       │   └── service/
│   │   │       │       └── AuthService.java
│   │   │       │
│   │   │       ├── candidat/                # Module candidat
│   │   │       │   ├── controller/
│   │   │       │   │   └── CandidateController.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── ApplicationSubmissionResponseDTO.java
│   │   │       │   │   ├── CandidateApplicationHistoryDTO.java
│   │   │       │   │   ├── CandidateApplicationDetailDTO.java
│   │   │       │   │   ├── PublicJobOfferListDTO.java
│   │   │       │   │   └── PublicJobOfferDetailDTO.java
│   │   │       │   ├── service/
│   │   │       │   │   └── CandidateService.java
│   │   │       │   └── repository/
│   │   │       │       └── ApplicationRepository.java
│   │   │       │
│   │   │       ├── commission/              # Module commission
│   │   │       │   ├── controller/
│   │   │       │   │   └── CommissionMemberController.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── MemberCommissionDTO.java
│   │   │       │   │   ├── CommissionApplicationListDTO.java
│   │   │       │   │   ├── CommissionApplicationDetailDTO.java
│   │   │       │   │   ├── CreateEvaluationDTO.java
│   │   │       │   │   ├── EvaluationResponseDTO.java
│   │   │       │   │   └── EvaluationAverageDTO.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── CommissionMemberService.java
│   │   │       │   │   └── EvaluationService.java
│   │   │       │   └── repository/
│   │   │       │       ├── CommissionRepository.java
│   │   │       │       └── EvaluationRepository.java
│   │   │       │
│   │   │       ├── config/                  # Configuration globale
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   ├── OpenApiConfig.java
│   │   │       │   └── WebConfig.java
│   │   │       │
│   │   │       ├── dashboard/               # Module dashboard RH
│   │   │       │   ├── controller/
│   │   │       │   │   └── DashboardController.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── RHDashboardDTO.java
│   │   │       │   │   ├── DashboardOverviewDTO.java
│   │   │       │   │   ├── JobOfferStatsDTO.java
│   │   │       │   │   ├── ApplicationStatsDTO.java
│   │   │       │   │   ├── CommissionStatsDTO.java
│   │   │       │   │   ├── TopJobOfferDTO.java
│   │   │       │   │   ├── RecentActivityDTO.java
│   │   │       │   │   └── DashboardAlertDTO.java
│   │   │       │   └── service/
│   │   │       │       └── DashboardService.java
│   │   │       │
│   │   │       ├── entity/                  # Entités JPA
│   │   │       │   ├── User.java
│   │   │       │   ├── Candidate.java
│   │   │       │   ├── RH.java
│   │   │       │   ├── CommissionMemberUser.java
│   │   │       │   ├── JobOffer.java
│   │   │       │   ├── Application.java
│   │   │       │   ├── Commission.java
│   │   │       │   ├── CommissionMember.java
│   │   │       │   ├── Evaluation.java
│   │   │       │   └── Token.java
│   │   │       │
│   │   │       ├── enums/                   # Énumérations
│   │   │       │   ├── Role.java
│   │   │       │   ├── JobType.java
│   │   │       │   ├── TypeContrat.java
│   │   │       │   ├── JobStatus.java
│   │   │       │   ├── ApplicationStatus.java
│   │   │       │   └── CommissionStatus.java
│   │   │       │
│   │   │       ├── exception/               # Gestion des exceptions
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   ├── UnauthorizedException.java
│   │   │       │   ├── BadRequestException.java
│   │   │       │   └── ErrorResponse.java
│   │   │       │
│   │   │       ├── ia/                      # Module IA
│   │   │       │   ├── controller/
│   │   │       │   │   └── WebhookController.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── ProcessCVResponseDTO.java
│   │   │       │   │   └── IAScoringResultDTO.java
│   │   │       │   └── service/
│   │   │       │       └── IAService.java
│   │   │       │
│   │   │       ├── rh/                      # Module RH
│   │   │       │   ├── controller/
│   │   │       │   │   ├── JobOfferController.java
│   │   │       │   │   ├── ApplicationController.java
│   │   │       │   │   └── CommissionController.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── CreateJobOfferDTO.java
│   │   │       │   │   ├── UpdateJobOfferDTO.java
│   │   │       │   │   ├── JobOfferResponseDTO.java
│   │   │       │   │   ├── JobOfferListDTO.java
│   │   │       │   │   ├── ApplicationDetailDTO.java
│   │   │       │   │   ├── ApplicationListDTO.java
│   │   │       │   │   ├── UpdateApplicationStatusDTO.java
│   │   │       │   │   ├── ShortlistApplicationsDTO.java
│   │   │       │   │   ├── CreateCommissionDTO.java
│   │   │       │   │   ├── UpdateCommissionDTO.java
│   │   │       │   │   ├── CommissionResponseDTO.java
│   │   │       │   │   ├── CommissionListDTO.java
│   │   │       │   │   ├── AddCommissionMemberDTO.java
│   │   │       │   │   └── CommissionMemberResponseDTO.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── JobOfferService.java
│   │   │       │   │   ├── ApplicationService.java
│   │   │       │   │   └── CommissionService.java
│   │   │       │   └── repository/
│   │   │       │       ├── JobOfferRepository.java
│   │   │       │       └── CommissionMemberRepository.java
│   │   │       │
│   │   │       └── RecrutementApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │
│   └── test/
│       └── java/
│           └── com/ept/recrutement/
│               ├── auth/
│               ├── candidat/
│               ├── commission/
│               ├── rh/
│               └── ia/
│
├── pom.xml
└── README.md


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