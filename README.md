<div align="center">

<h1><br><strong> CertifyMe - Backend</strong></br></h1>

<p>
  <strong>Enterprise-level Certification Tracking Platform</strong><br/>
  Built with Spring Boot 4 · PostgreSQL · JWT · SendGrid · Docker
</p>

<p>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/PostgreSQL-Supabase-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/JWT-Secured-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT"/>
  <img src="https://img.shields.io/badge/SendGrid-Email-1A82E2?style=for-the-badge&logo=sendgrid&logoColor=white" alt="SendGrid"/>
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/Deployed_on-Render-46E3B7?style=for-the-badge&logo=render&logoColor=white" alt="Render"/>
  <img src="https://img.shields.io/badge/Users-4000%2B-FF6B6B?style=for-the-badge&logo=users&logoColor=white" alt="4000+ Users"/>
</p>

<p>
  <a href="https://certifymeonline.vercel.app" target="_blank">🌐 Live Demo</a> ·
  <a href="https://github.com/lalithdev/CertifyMe-Certification-Tracking-Platform" target="_blank">📦 Frontend Repo</a>
</p>

<p>
  <a href="#-quick-start">Quick Start</a> ·
  <a href="#-api-reference">API Reference</a> ·
  <a href="#-architecture">Architecture</a> ·
  <a href="#-project-structure">Project Structure</a> ·
  <a href="#-docker-deployment">Docker</a>
</p>

</div>

---

## 📑 Table of Contents

1. [Project Overview](#-project-overview)
2. [Tech Stack](#-tech-stack)
3. [Architecture & Working Flow](#-architecture--working-flow)
4. [Database Schema](#-database-schema)
5. [Project Structure](#-project-structure)
6. [Prerequisites](#-prerequisites)
7. [Quick Start (Local Setup)](#-quick-start-local-setup)
8. [Environment Variables Reference](#-environment-variables-reference)
9. [API Reference](#-api-reference)
10. [Security Model](#-security-model)
11. [Email & Notification System](#-email--notification-system)
12. [Scheduled Jobs](#-scheduled-jobs)
13. [Exception Handling](#-exception-handling)
14. [Docker Deployment](#-docker-deployment)
15. [Production Deployment (Render)](#-production-deployment-render)
16. [Testing](#-testing)
17. [Common Issues & Troubleshooting](#-common-issues--troubleshooting)
18. [Contributing](#-contributing)

---

## 🎯 Project Overview

**CertifyMe** is a full-stack certification tracking and lifecycle management platform designed for academic and corporate environments. The backend is a stateless RESTful API that serves two user roles:

> 🚀 **Live at scale** — currently powering **4,000+ student users** and **10+ faculty/admin accounts**, managing thousands of certification records with automated expiry tracking and renewal workflows.

<div align="center">

| 👨‍🎓 Students | 👨‍🏫 Faculty / Admins | 🏅 Certifications Tracked | ⏰ Daily Reminders |
|:---:|:---:|:---:|:---:|
| **4,000+** | **10+** | **Thousands** | **Automated at 09:00 AM** |

</div>

| Role | Description |
|---|---|
| **STUDENT** | Registers, views, and manages their own certifications; submits renewal requests |
| **ADMIN** | Manages all users and certifications; approves/rejects renewals; sends reminders; exports reports |

### ✨ Core Features

- 🔐 **JWT-based stateless authentication** with BCrypt password hashing
- 📧 **OTP-based 2-Factor Authentication** for Admin login via SendGrid
- 🏅 **Full certification CRUD** with status tracking (Active / Expiring Soon / Expired)
- 🔄 **Renewal workflow** — Student requests, Admin approves/rejects with remarks
- 🔔 **In-app notification system** with read/unread states
- ⏰ **Automated daily scheduler** — sends expiry reminders for certifications expiring within 30 days
- 📊 **Dashboard statistics** — total, active, expiring, expired, pending renewal counts
- 📥 **Excel export** for certifications (per-user or global for Admin)
- 🛡️ **Global exception handling** with structured JSON error responses
- 🐳 **Dockerized** with multi-stage build for production

---

## 🛠 Tech Stack

| Category | Technology | Version | Purpose |
|---|---|---|---|
| **Language** | Java | 17 (LTS) | Core programming language |
| **Framework** | Spring Boot | 4.0.5 | Application framework |
| **Security** | Spring Security | *(managed)* | Authentication & authorization |
| **JWT** | JJWT (io.jsonwebtoken) | 0.12.6 | Token generation & validation |
| **Database** | PostgreSQL | *(Supabase)* | Primary relational database |
| **ORM** | Spring Data JPA / Hibernate | *(managed)* | Database abstraction & ORM |
| **DB Driver** | PostgreSQL Driver | *(managed)* | JDBC connectivity |
| **Email** | SendGrid Java SDK | 4.10.3 | Transactional email (OTP & notifications) |
| **Excel** | Apache POI (poi-ooxml) | 5.2.5 | Certification report generation |
| **Validation** | Spring Validation (Hibernate Validator) | *(managed)* | Request body validation |
| **Build Tool** | Apache Maven | 3.9.6 | Dependency management & build |
| **Boilerplate** | Lombok | 1.18.44 | Reduces boilerplate (getters, builders, logs) |
| **Containerization** | Docker | *(latest)* | Containerized deployments |
| **Hosting** | Render | — | Cloud PaaS deployment |
| **Cloud DB** | Supabase (PostgreSQL) | — | Managed database with connection pooler |

---

## 🏛 Architecture & Working Flow

### System Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                        FRONTEND (React)                          │
│            http://localhost:5173  |  Vercel Production           │
└─────────────────────────┬────────────────────────────────────────┘
                          │  HTTPS  (Axios + JWT Bearer Token)
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND (Port 8080)               │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐     │
│  │              Spring Security Filter Chain                │     │
│  │  ┌──────────────────────────────────────────────────┐   │     │
│  │  │  JwtAuthenticationFilter                          │   │     │
│  │  │  1. Extract Bearer token from Authorization header│   │     │
│  │  │  2. Validate & decode JWT via JwtService          │   │     │
│  │  │  3. Load UserDetails from DB                      │   │     │
│  │  │  4. Set SecurityContextHolder                     │   │     │
│  │  └────────────────────┬─────────────────────────────┘   │     │
│  └───────────────────────┼─────────────────────────────────┘     │
│                          │                                         │
│  ┌───────────────────────▼──────────────────────────────────┐    │
│  │                 REST Controllers (Layer 1)                 │    │
│  │   AuthController  │  CertificationController              │    │
│  │   UserController  │  NotificationController               │    │
│  │   StudentNotificationController                           │    │
│  └───────────────────────┬──────────────────────────────────┘    │
│                          │                                         │
│  ┌───────────────────────▼──────────────────────────────────┐    │
│  │                   Service Layer (Layer 2)                  │    │
│  │  AuthService │ CertificationService │ NotificationService  │    │
│  │  UserService │ EmailService                                │    │
│  └───────────────────────┬──────────────────────────────────┘    │
│                          │                                         │
│  ┌───────────────────────▼──────────────────────────────────┐    │
│  │                Repository Layer (Layer 3)                  │    │
│  │  Spring Data JPA Repositories                              │    │
│  └───────────────────────┬──────────────────────────────────┘    │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│               PostgreSQL on Supabase (Connection Pooler)          │
│               aws-1-ap-southeast-1.pooler.supabase.com:5432       │
└──────────────────────────────────────────────────────────────────┘
                          │
          ┌───────────────┴───────────────┐
          ▼                               ▼
   ┌─────────────┐               ┌────────────────┐
   │  SendGrid   │               │  ReminderScheduler │
   │  Email API  │               │  (Cron @09:00 AM)  │
   └─────────────┘               └────────────────────┘
```

---

### 🔄 Complete A-to-Z Request Flow

Below is the step-by-step lifecycle of a request — using **"Student adds a Certification"** as the example:

```
Step 1: FRONTEND
  User fills form → handleSubmit() validates → certificationApi.create(userId, data)
  → Axios picks up JWT from localStorage → Attaches "Authorization: Bearer <token>"
  → POST https://api.certifyme.com/api/certifications/user/{userId}

Step 2: SPRING SECURITY FILTER (JwtAuthenticationFilter)
  → Intercepts every request
  → Extracts token from "Authorization" header
  → JwtService.validateToken() → decodes claims → extracts email
  → CustomUserDetailsService.loadUserByUsername(email) → fetches User from DB
  → Sets Authentication in SecurityContextHolder
  → Request proceeds to the Controller

Step 3: CONTROLLER (CertificationController)
  → @PostMapping("/user/{userId}") receives request
  → @Valid validates @RequestBody CertificationRequestDTO
    (blank checks, date format, required fields)
  → Delegates to CertificationService.saveCertification(userId, request)

Step 4: SERVICE (CertificationService)
  → Fetches User entity from UserRepository by userId
  → CertificationMapper.toEntity(request) converts DTO → Entity
  → Sets user on certification entity
  → CertificationRepository.save(cert) persists to PostgreSQL
  → NotificationService.createNotification(...) fires an in-app notification
  → CertificationMapper.toResponseDTO(cert) converts Entity → DTO

Step 5: RESPONSE
  → Controller returns ResponseEntity<CertificationResponseDTO> with HTTP 200
  → Frontend Axios receives response → updates state → shows success toast
```

---

### 🔐 Authentication Flow — Admin (OTP 2FA)

```
[Admin Login Attempt]
        │
        ▼
POST /api/auth/login  (email + password, no OTP)
        │
        ├──► AuthenticationManager validates credentials
        │
        ├──► User role = ADMIN → OTP required
        │
        ├──► OtpUtils.generateOTP() → hash with SHA-256 → store in DB
        │
        ├──► emailService.sendOtpEmail(email, firstName, otp)
        │         └─ SendGrid API → delivers OTP to email
        │
        └──► Response: { otpRequired: true, remainingValiditySeconds: 120 }

[OTP Submission]
        │
POST /api/auth/login  (email + password + otp)
        │
        ├──► Validate OTP hash match (SHA-256 comparison)
        ├──► Check OTP not expired (2-minute window)
        ├──► Check attempt count < 3 (else lock 5 minutes)
        │
        └──► Valid → Clear OTP → JwtService.generateToken(user)
                   → Response: { token: "eyJ...", user: {...} }
```

---

### 🔐 Authentication Flow — Student (Direct JWT)

```
POST /api/auth/login  (email + password)
        │
        ├──► AuthenticationManager validates credentials
        ├──► Role = STUDENT → No OTP required
        └──► JwtService.generateToken(user) → Response: { token, user }
```

---

### 🔄 Renewal Workflow

```
STUDENT                    BACKEND                        ADMIN
   │                          │                              │
   │── PUT /certifications     │                              │
   │    /{id}/renewal          │                              │
   │    { action: "REQUEST" }─►│                              │
   │                          │── cert.renewalStatus = PENDING│
   │                          │── notify student             │
   │◄── 200 OK (PENDING)       │                              │
   │                          │                              │
   │                          │◄─── GET /certifications/renewals
   │                          │     (Admin sees pending list) │
   │                          │                              │
   │                          │◄─── PUT /certifications/{id}/renewal
   │                          │     { action: "APPROVE" }    │
   │                          │── cert.renewalStatus = APPROVED
   │                          │── notify student             │
   │◄── notification: Approved │                              │
```

---

## 🗄 Database Schema

The application uses PostgreSQL with JPA/Hibernate managing the DDL (`spring.jpa.hibernate.ddl-auto=update`).

### Entity Relationship Overview

```
┌─────────────────────┐         ┌──────────────────────────┐
│       users         │         │      certification        │
├─────────────────────┤         ├──────────────────────────┤
│ id (PK)             │──────┐  │ id (PK)                  │
│ first_name          │      └─►│ user_id (FK → users.id)  │
│ middle_name         │         │ title                     │
│ last_name           │         │ issuer                    │
│ email (UNIQUE)      │         │ credential_id             │
│ password (BCrypt)   │         │ url                       │
│ role (ADMIN/STUDENT)│         │ remarks                   │
│ age                 │         │ renewal_status            │
│ gender              │         │ issue_date                │
│ country             │         │ expiry_date               │
│ student_id          │         │ requested_on              │
│ verification_code   │         │ approved_on               │
│ otp_created_at      │         │ rejected_on               │
│ otp_attempts        │         │ last_reminder_sent        │
│ otp_locked_until    │         │ created_at                │
│ created_at          │         └──────────────────────────┘
└─────────────────────┘
          │
          │ One-to-Many
          ▼
┌─────────────────────┐         ┌──────────────────────────┐
│    notification     │         │   password_reset_token   │
├─────────────────────┤         ├──────────────────────────┤
│ id (PK)             │         │ id (PK)                  │
│ user_id (FK)        │         │ email                    │
│ certification_id    │         │ otp                      │
│ title               │         │ expires_at               │
│ message             │         │ is_used                  │
│ type                │         └──────────────────────────┘
│ is_read             │
│ created_at          │
└─────────────────────┘
```

### Enums

| Enum | Values | Usage |
|---|---|---|
| `Role` | `STUDENT`, `ADMIN` | User role-based access control |
| `RenewalStatus` | `NONE`, `PENDING`, `APPROVED`, `REJECTED` | Certification renewal workflow state |
| `CertificationStatus` | `ACTIVE`, `EXPIRING_SOON`, `EXPIRED` | Computed at runtime from `expiryDate` (not persisted) |
| `RenewalAction` | `REQUEST`, `APPROVE`, `REJECT` | Actions allowed in the renewal PUT endpoint |

---

## 📁 Project Structure

```
certifyme/
├── src/
│   ├── main/
│   │   ├── java/com/certifyme/app/
│   │   │   │
│   │   │   ├── CertifymeApplication.java         # Spring Boot entry point
│   │   │   │
│   │   │   ├── config/                           # (Reserved for future config beans)
│   │   │   │
│   │   │   ├── controller/                       # REST Controllers — Layer 1
│   │   │   │   ├── AuthController.java           # /api/auth/** — Login, register, OTP, password
│   │   │   │   ├── CertificationController.java  # /api/certifications/** — CRUD, export, stats
│   │   │   │   ├── NotificationController.java   # /api/notifications/** — Admin notification ops
│   │   │   │   ├── StudentNotificationController.java # /api/student/** — Student's own notifications
│   │   │   │   ├── UserController.java           # /api/users/** — User management (Admin)
│   │   │   │   └── HomeController.java           # / — Health check endpoint
│   │   │   │
│   │   │   ├── dto/                              # Data Transfer Objects (API contracts)
│   │   │   │   ├── LoginRequestDTO.java
│   │   │   │   ├── RegisterRequestDTO.java
│   │   │   │   ├── AuthResponseDTO.java
│   │   │   │   ├── CertificationRequestDTO.java
│   │   │   │   ├── CertificationResponseDTO.java
│   │   │   │   ├── CertificationRenewalDTO.java
│   │   │   │   ├── DashboardStatsDTO.java
│   │   │   │   ├── NotificationResponseDTO.java
│   │   │   │   ├── PagedResponseDTO.java          # Generic paginated wrapper
│   │   │   │   ├── UserResponseDTO.java
│   │   │   │   ├── ApiErrorResponseDTO.java       # Standardized error response shape
│   │   │   │   ├── ForgotPasswordRequestDTO.java
│   │   │   │   ├── VerifyOtpRequestDTO.java
│   │   │   │   ├── ResetPasswordRequestDTO.java
│   │   │   │   ├── ChangePasswordRequestDTO.java
│   │   │   │   ├── ResendOtpRequestDTO.java
│   │   │   │   └── RenewalRequestDTO.java
│   │   │   │
│   │   │   ├── exception/                        # Error handling
│   │   │   │   ├── GlobalExceptionHandler.java   # @RestControllerAdvice — all exception mapping
│   │   │   │   ├── ResourceNotFoundException.java # → 404
│   │   │   │   ├── BadRequestException.java       # → 400
│   │   │   │   ├── UnauthorizedException.java     # → 401
│   │   │   │   └── DuplicateResourceException.java # → 409
│   │   │   │
│   │   │   ├── mapper/                           # Entity ↔ DTO conversion
│   │   │   │   ├── CertificationMapper.java
│   │   │   │   ├── NotificationMapper.java
│   │   │   │   └── UserMapper.java
│   │   │   │
│   │   │   ├── model/                            # JPA Entities (database tables)
│   │   │   │   ├── User.java                     # users table; implements UserDetails
│   │   │   │   ├── Certification.java            # certification table
│   │   │   │   ├── Notification.java             # notification table
│   │   │   │   ├── PasswordResetToken.java        # password_reset_token table
│   │   │   │   ├── Role.java                     # Enum: STUDENT, ADMIN
│   │   │   │   ├── RenewalStatus.java             # Enum: NONE, PENDING, APPROVED, REJECTED
│   │   │   │   ├── RenewalAction.java             # Enum: REQUEST, APPROVE, REJECT
│   │   │   │   ├── CertificationStatus.java       # Enum: ACTIVE, EXPIRING_SOON, EXPIRED (computed)
│   │   │   │   └── RenewalStatusConverter.java    # JPA AttributeConverter for RenewalStatus
│   │   │   │
│   │   │   ├── repository/                       # Spring Data JPA Repositories — Layer 3
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CertificationRepository.java  # Custom JPQL for reminders, expiry queries
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   └── PasswordResetTokenRepository.java
│   │   │   │
│   │   │   ├── scheduler/                        # Background scheduled tasks
│   │   │   │   └── ReminderScheduler.java        # Cron: sends expiry reminders at 09:00 AM daily
│   │   │   │
│   │   │   ├── security/                         # JWT & Spring Security
│   │   │   │   ├── SecurityConfig.java           # Filter chain, CORS, endpoint rules
│   │   │   │   ├── JwtService.java               # Token generation, validation, claims extraction
│   │   │   │   ├── JwtAuthenticationFilter.java  # Per-request token interceptor
│   │   │   │   └── CustomUserDetailsService.java # Loads User by email from DB
│   │   │   │
│   │   │   ├── service/                          # Business Logic — Layer 2
│   │   │   │   ├── AuthService.java              # Register, login, OTP, password management
│   │   │   │   ├── CertificationService.java     # CRUD, renewal workflow, stats, Excel export
│   │   │   │   ├── NotificationService.java      # Create, read, mark, delete notifications
│   │   │   │   ├── UserService.java              # User fetch operations
│   │   │   │   ├── EmailService.java             # SendGrid integration (OTP, expiry, renewal emails)
│   │   │   │   └── DataMigrationComponent.java   # One-time data migration utility
│   │   │   │
│   │   │   └── util/                             # Reusable utility classes
│   │   │       ├── DateUtil.java                 # Computes CertificationStatus from expiryDate
│   │   │       ├── ExcelExportUtil.java           # Apache POI — generates .xlsx reports
│   │   │       └── OtpUtils.java                 # OTP generation and SHA-256 hashing
│   │   │
│   │   └── resources/
│   │       └── application.properties            # All config (reads from .env / system env)
│   │
│   └── test/                                     # Unit & integration tests
│
├── .env                                          # Local environment variables (DO NOT COMMIT)
├── .gitignore
├── Dockerfile                                    # Multi-stage Docker build
├── mvnw / mvnw.cmd                               # Maven wrapper scripts
└── pom.xml                                       # Project dependencies & build config
```

---

## ✅ Prerequisites

Before running this project locally, ensure the following are installed:

| Tool | Minimum Version | Download |
|---|---|---|
| **Java JDK** | 17 (LTS) | [Adoptium / Oracle](https://adoptium.net/) |
| **Maven** | 3.9+ (or use `mvnw`) | [maven.apache.org](https://maven.apache.org/) |
| **PostgreSQL** | 14+ (or use Supabase) | [postgresql.org](https://www.postgresql.org/) |
| **Git** | Any | [git-scm.com](https://git-scm.com/) |
| **Docker** *(optional)* | 24+ | [docker.com](https://www.docker.com/) |

> **Tip**: You do **not** need a local PostgreSQL installation if you use the provided Supabase connection details. The `.env` already points to the cloud database.

---

## 🚀 Quick Start (Local Setup)

Follow these steps **exactly** to get the backend running on your machine in under 5 minutes.

### Step 1 — Clone the Repository

```bash
git clone <your-backend-repo-url>
cd certifyme
```

### Step 2 — Set Up Environment Variables

The application reads all secrets from a `.env` file in the project root. Create it if it doesn't exist:

```bash
# Windows (PowerShell)
Copy-Item .env.example .env   # if example file exists

# Or create manually — see the section below for all variables
```

Fill in the `.env` with the following values (see [Environment Variables Reference](#-environment-variables-reference)):

```env
# ============================================================
#  CertifyMe — Environment Variables
#  DO NOT commit this file with real secrets!
# ============================================================

# Database (Supabase PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=your_supabase_username
SPRING_DATASOURCE_PASSWORD=your_supabase_password

# Server
PORT=8080
SPRING_PROFILES_ACTIVE=prod

# JWT Secret (64-char hex string)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# SendGrid
SENDGRID_API_KEY=SG.your_sendgrid_api_key_here
APP_MAIL_FROM=verify.certifyme@gmail.com
```

> ⚠️ **Security Warning**: The `.env` file is already listed in `.gitignore`. **Never** push real secrets to source control.

### Step 3 — Verify Java Version

```bash
java -version
# Expected: openjdk version "17.x.x" or higher
```

If Java 17 is not your default, set `JAVA_HOME`:

```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

### Step 4 — Build the Project

```bash
# Using Maven Wrapper (no Maven installation required)
./mvnw clean install -DskipTests

# Windows
.\mvnw.cmd clean install -DskipTests
```

This will:
1. Download all dependencies from Maven Central
2. Compile the Java source files
3. Package the application into a JAR at `target/certifyme-0.0.1-SNAPSHOT.jar`

### Step 5 — Run the Application

```bash
# Using Maven Wrapper
./mvnw spring-boot:run

# OR run the JAR directly
java -jar target/certifyme-0.0.1-SNAPSHOT.jar

# Windows
.\mvnw.cmd spring-boot:run
```

### Step 6 — Verify the Server is Running

```bash
# Check health endpoint
curl http://localhost:8080/

# Or test the API
curl http://localhost:8080/api/auth/test
# Expected response: "Backend is running correctly!"
```

You should see in the console:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v4.0.5)

...
Started CertifymeApplication in X.XXX seconds (process running for X.XXX)
```

---

## ⚙️ Environment Variables Reference

All environment variables are loaded from `.env` at startup (via `spring.config.import=optional:file:.env[.properties]`). On cloud deployments (Render), these are injected as system environment variables directly.

| Variable | Required | Default | Description |
|---|---|---|---|
| `SPRING_DATASOURCE_URL` | ✅ Yes | — | Full JDBC connection string for PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | ✅ Yes | — | Database username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ Yes | — | Database password |
| `PORT` | ⚪ Optional | `8080` | HTTP port the server listens on |
| `SPRING_PROFILES_ACTIVE` | ⚪ Optional | — | Active Spring profile (`prod`, `dev`) |
| `JWT_SECRET` | ✅ Yes | fallback (dev only) | 64-char hex string for JWT signing (HS256) |
| `SENDGRID_API_KEY` | ✅ Yes | — | SendGrid API key starting with `SG.` |
| `APP_MAIL_FROM` | ✅ Yes | — | Sender email address for transactional emails |

> **JWT Token Expiry**: Tokens are valid for **24 hours** (`app.jwt.expiration-ms=86400000`).

> **OTP Validity**: Admin OTPs expire in **2 minutes**. Password reset OTPs expire in **10 minutes**.

---

## 📡 API Reference

**Base URL (Local):** `http://localhost:8080`  
**Base URL (Production):** `https://certifyme-backend.onrender.com`

> **Authentication**: All endpoints except `/api/auth/**` and `/api/public/**` require the HTTP header:
> ```
> Authorization: Bearer <your-jwt-token>
> ```

---

### 🔐 Auth Endpoints — `/api/auth`

| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | ❌ | Register a new user (Student or Admin) |
| `POST` | `/api/auth/login` | ❌ | Login — returns JWT or triggers Admin OTP flow |
| `POST` | `/api/auth/resend-otp` | ❌ | Resend Admin OTP (30s cooldown enforced) |
| `POST` | `/api/auth/forgot-password` | ❌ | Initiate password reset — sends OTP via email |
| `POST` | `/api/auth/verify-otp` | ❌ | Verify reset OTP — returns session JWT |
| `POST` | `/api/auth/verify-reset-otp` | ❌ | Alias for `verify-otp` |
| `POST` | `/api/auth/reset-password` | ❌ | Reset password using email + OTP + newPassword |
| `POST` | `/api/auth/change-password` | ✅ JWT | Change password (authenticated user, current password required) |
| `GET`  | `/api/auth/test` | ❌ | Health/smoke test endpoint |

#### `POST /api/auth/register`
```json
// Request Body
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "role": "STUDENT",
  "age": 22,
  "gender": "Male",
  "country": "India",
  "studentId": "STU2024001"
}

// Response 200
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "role": "STUDENT"
  }
}
```

#### `POST /api/auth/login` (Student)
```json
// Request
{ "email": "john.doe@example.com", "password": "SecurePass123!" }

// Response 200
{ "token": "eyJ...", "user": { ... } }
```

#### `POST /api/auth/login` (Admin — Step 1: Trigger OTP)
```json
// Request (no OTP field)
{ "email": "admin@certifyme.com", "password": "AdminPass!" }

// Response 200 — OTP sent to admin's email
{
  "otpRequired": true,
  "remainingValiditySeconds": 120,
  "resendCooldownSeconds": 30
}
```

#### `POST /api/auth/login` (Admin — Step 2: Submit OTP)
```json
// Request
{ "email": "admin@certifyme.com", "password": "AdminPass!", "otp": "483921" }

// Response 200
{ "token": "eyJ...", "user": { "id": 2, "role": "ADMIN", ... } }
```

---

### 🏅 Certification Endpoints — `/api/certifications`

All endpoints require a valid JWT (`Authorization: Bearer <token>`).

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| `POST` | `/api/certifications/user/{userId}` | ALL | Add a new certification for a user |
| `GET` | `/api/certifications/user/{userId}` | ALL | Get all certifications for a user (list) |
| `GET` | `/api/certifications/user/{userId}/paged` | ALL | Paginated certifications for a user |
| `GET` | `/api/certifications` | ADMIN | Get all certifications (paginated) |
| `GET` | `/api/certifications/all` | ADMIN | Get all certifications (list, backward compat.) |
| `PUT` | `/api/certifications/{id}` | ALL | Update a certification by ID |
| `DELETE` | `/api/certifications/{id}` | ALL | Delete a certification by ID |
| `PUT` | `/api/certifications/{id}/renewal` | STUDENT/ADMIN | Request / Approve / Reject renewal |
| `PUT` | `/api/certifications/{id}/remind` | ADMIN | Manually send reminder for a certification |
| `GET` | `/api/certifications/expiring` | ALL | Get certifications expiring within 30 days |
| `GET` | `/api/certifications/expired` | ALL | Get all expired certifications |
| `GET` | `/api/certifications/renewals` | ADMIN | Get all pending renewal requests |
| `GET` | `/api/certifications/stats/{userId}` | ALL | Get dashboard stats for a user |
| `GET` | `/api/certifications/export` | ALL | Export all certifications as `.xlsx` |
| `GET` | `/api/certifications/export/{userId}` | ALL | Export a specific user's certifications as `.xlsx` |

#### `POST /api/certifications/user/{userId}`
```json
// Request Body
{
  "title": "AWS Certified Solutions Architect",
  "issuer": "Amazon Web Services",
  "credentialId": "AWS-SAA-12345",
  "url": "https://aws.amazon.com/verification/12345",
  "issueDate": "2024-01-15T00:00:00",
  "expiryDate": "2027-01-15T00:00:00",
  "remarks": "Passed with score 890/1000"
}

// Response 200
{
  "id": 42,
  "title": "AWS Certified Solutions Architect",
  "issuer": "Amazon Web Services",
  "credentialId": "AWS-SAA-12345",
  "certificationStatus": "ACTIVE",
  "renewalStatus": "NONE",
  "issueDate": "2024-01-15T00:00:00",
  "expiryDate": "2027-01-15T00:00:00",
  "createdAt": "2026-07-14T13:00:00"
}
```

#### `PUT /api/certifications/{id}/renewal` — Student Requests
```json
// Request Body (Student)
{ "action": "REQUEST" }

// Request Body (Admin — Approve)
{ "action": "APPROVE" }

// Request Body (Admin — Reject)
{ "action": "REJECT", "remarks": "Certificate document not uploaded" }
```

#### `GET /api/certifications/stats/{userId}` — Dashboard Statistics
```json
// Response 200
{
  "totalCertifications": 12,
  "activeCertifications": 8,
  "expiringSoon": 2,
  "expiredCertifications": 2,
  "pendingRenewals": 1,
  "completionPercentage": 66.67
}
```

---

### 👤 User Endpoints — `/api/users`

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| `GET` | `/api/users/{id}` | ALL | Get a user by ID |
| `GET` | `/api/users` | ADMIN | Get all users |
| `GET` | `/api/users/students` | ADMIN | Get all students (paginated) |

---

### 🔔 Notification Endpoints

#### Admin Notification Controller — `/api/notifications`

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| `GET` | `/api/notifications/user/{userId}` | ALL | Get paginated notifications for a user |
| `GET` | `/api/notifications/user/{userId}/unread` | ALL | Get unread notification count |
| `PUT` | `/api/notifications/{id}/read` | ALL | Mark a single notification as read |
| `PUT` | `/api/notifications/user/{userId}/read-all` | ALL | Mark all notifications as read |
| `DELETE` | `/api/notifications/{id}` | ALL | Delete a notification |

#### Student Notification Controller — `/api/student`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/student/notifications` | Get student's own notifications (extracts userId from JWT) |
| `GET` | `/api/student/notifications/unread-count` | Get student's unread count (from JWT) |

---

### 📄 Paginated Response Shape

All paginated endpoints return the following wrapper:

```json
{
  "content": [ { ... }, { ... } ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 47,
  "totalPages": 3,
  "last": false
}
```

---

### ❌ Error Response Shape

All errors return a consistent `ApiErrorResponseDTO`:

```json
{
  "timestamp": "2026-07-14T13:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid OTP",
  "path": "/api/auth/login"
}
```

| HTTP Status | Exception | Trigger |
|---|---|---|
| `400` | `BadRequestException` | Invalid request data |
| `400` | `MethodArgumentNotValidException` | Bean Validation failure |
| `401` | `UnauthorizedException` | Invalid credentials, expired/invalid JWT |
| `404` | `ResourceNotFoundException` | Entity not found |
| `405` | `HttpRequestMethodNotSupportedException` | Wrong HTTP method |
| `409` | `DuplicateResourceException` | Email already in use |
| `503` | `EmailDeliveryException` | SendGrid API failure |
| `500` | `Exception` | Unhandled server error |

---

## 🛡️ Security Model

### JWT Configuration

- **Algorithm**: HMAC-SHA256 (HS256)
- **Expiry**: 24 hours (`86400000` ms)
- **Secret**: 64-character hex string (set via `JWT_SECRET` env var)
- **Storage**: Frontend stores in `localStorage`; sent via `Authorization: Bearer` header

### CORS Configuration

The backend explicitly allows the following origins:

```
http://localhost:*                     (all local development ports)
https://certifyme-*.vercel.app         (Vercel preview deployments)
https://certifyme*.vercel.app          (Vercel production deployment)
```

Allowed methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`

### Password Security

- Passwords are hashed with **BCrypt** before storage
- OTPs are hashed with **SHA-256** before storage (plaintext never persisted)
- Admin OTP attempts are tracked and **locked for 5 minutes** after 3 failed attempts
- Admin OTPs have a **30-second resend cooldown**

### Endpoint Security Rules

| Path Pattern | Rule |
|---|---|
| `/` | Public |
| `/api/auth/**` | Public (except `/api/auth/change-password`) |
| `/api/auth/change-password` | Authenticated |
| `/api/public/**` | Public |
| All other `/api/**` | Authenticated (JWT required) |
| `hasRole('ADMIN')` endpoints | Requires `ROLE_ADMIN` in JWT claims |

---

## 📧 Email & Notification System

### SendGrid Integration

CertifyMe uses the **official SendGrid Java SDK** (not SMTP) for transactional emails.

| Email Type | Trigger | Recipient |
|---|---|---|
| Admin OTP | Admin login attempt | Admin |
| Password Reset OTP | `/api/auth/forgot-password` | Any user |
| Renewal Requested | Student submits renewal | Student (confirmation) |
| Renewal Approved | Admin approves | Student |
| Renewal Rejected | Admin rejects | Student |
| Expiry Reminder | Scheduler (09:00 AM daily) | User whose cert expires in ≤30 days |

### In-App Notification Types

| Type | Trigger |
|---|---|
| `SYSTEM` | Certification added or updated |
| `RENEWAL_UPDATE` | Renewal status changed (Requested / Approved / Rejected) |
| `REMINDER` | Expiry reminder from scheduler or manual admin action |

---

## ⏰ Scheduled Jobs

The `ReminderScheduler` runs background tasks via Spring's `@Scheduled` annotation.

| Job | Cron Expression | Description |
|---|---|---|
| `sendExpiryReminders()` | `0 0 9 * * *` (09:00 AM daily) | Finds all certifications expiring within 30 days that haven't been reminded today; creates in-app notifications; updates `last_reminder_sent` |
| `cleanupOldNotifications()` | `0 0 0 * * *` (12:00 AM daily) | Intended for cleanup of read notifications older than 90 days |

---

## 🐳 Docker Deployment

The project includes a production-ready **multi-stage Dockerfile**.

### Build the Docker Image

```bash
docker build -t certifyme-backend:latest .
```

### Run the Container

```bash
docker run -d \
  --name certifyme-backend \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://..." \
  -e SPRING_DATASOURCE_USERNAME="your_user" \
  -e SPRING_DATASOURCE_PASSWORD="your_password" \
  -e JWT_SECRET="your_jwt_secret" \
  -e SENDGRID_API_KEY="SG.your_key" \
  -e APP_MAIL_FROM="verify.certifyme@gmail.com" \
  certifyme-backend:latest
```

### Using Docker Compose (with local PostgreSQL)

Create a `docker-compose.yml`:

```yaml
version: '3.8'

services:
  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: certifyme
      POSTGRES_USER: certifyme_user
      POSTGRES_PASSWORD: certifyme_pass
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/certifyme
      SPRING_DATASOURCE_USERNAME: certifyme_user
      SPRING_DATASOURCE_PASSWORD: certifyme_pass
      JWT_SECRET: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
      SENDGRID_API_KEY: SG.your_key_here
      APP_MAIL_FROM: verify.certifyme@gmail.com
    depends_on:
      - db

volumes:
  pgdata:
```

```bash
docker-compose up --build
```

### Dockerfile Explanation

```dockerfile
# Stage 1: Build — uses Maven + JDK to compile and package
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime — uses lightweight JDK Alpine image
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

The multi-stage build ensures the final image only contains the runtime, **not** the build tools — resulting in a significantly smaller image (~200 MB vs ~700 MB).

---

## 🌐 Production Deployment (Render)

The backend is deployed on **Render** as a Docker Web Service.

### Steps to Deploy on Render

1. **Create a Web Service** on [render.com](https://render.com)
2. Connect your GitHub repository
3. Set **Environment** to `Docker`
4. Set the **Start Command** (Render handles it automatically via `ENTRYPOINT` in Dockerfile)
5. Add all environment variables in Render's **Environment** tab:

```
SPRING_DATASOURCE_URL   = jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME = postgres.xxx
SPRING_DATASOURCE_PASSWORD = ***
JWT_SECRET              = 404E635...
SENDGRID_API_KEY        = SG.xxx
APP_MAIL_FROM           = verify.certifyme@gmail.com
PORT                    = 8080
```

6. Click **Deploy**

### Health Check

Render auto-detects the service is healthy when `/` returns HTTP 200.

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Windows
.\mvnw.cmd test
```

> **Note**: The JVM flag `-XX:+EnableDynamicAgentLoading -Xshare:off` is pre-configured in `maven-surefire-plugin` for compatibility with Java 17.

### Manual API Testing with curl

```bash
# 1. Register a student
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@test.com",
    "password": "TestPass123!",
    "role": "STUDENT"
  }'

# 2. Login and capture token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "jane.smith@test.com", "password": "TestPass123!"}' \
  | python -c "import sys, json; print(json.load(sys.stdin)['token'])")

# 3. Add a certification (replace {userId} with actual ID)
curl -X POST http://localhost:8080/api/certifications/user/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Google Cloud Associate",
    "issuer": "Google",
    "issueDate": "2024-06-01T00:00:00",
    "expiryDate": "2026-06-01T00:00:00"
  }'

# 4. Get dashboard stats
curl http://localhost:8080/api/certifications/stats/1 \
  -H "Authorization: Bearer $TOKEN"

# 5. Export certifications as Excel
curl -O -J http://localhost:8080/api/certifications/export/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔧 Common Issues & Troubleshooting

### ❌ `Unable to acquire JDBC Connection`

**Cause**: Database is unreachable.  
**Fix**:
- Verify `SPRING_DATASOURCE_URL`, `USERNAME`, and `PASSWORD` are set correctly in `.env`
- Ensure `sslmode=require` is in the URL when using Supabase
- Check Supabase is active and the Connection Pooler URL is used (not direct connection)

### ❌ `JWT signature does not match`

**Cause**: `JWT_SECRET` changed between sessions, or is different between environments.  
**Fix**: Make sure `JWT_SECRET` is identical in your `.env` and production environment. Existing tokens from a previous secret are now invalid — users must log in again.

### ❌ `Email delivery failed` (503)

**Cause**: `SENDGRID_API_KEY` is invalid, expired, or missing.  
**Fix**: Verify the API key at [app.sendgrid.com](https://app.sendgrid.com). Ensure `APP_MAIL_FROM` matches a verified Sender Identity in SendGrid.

### ❌ `Port 8080 already in use`

**Fix**:
```bash
# Windows — find and kill the process on port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### ❌ `Java version mismatch` — `UnsupportedClassVersionError`

**Cause**: Application compiled with Java 17 but running with a lower version.  
**Fix**: Set `JAVA_HOME` to JDK 17 and ensure `java -version` shows 17.x.

### ❌ `CORS policy` error from frontend

**Cause**: Frontend origin not in the allowed list.  
**Fix**: Add your frontend's URL to `corsConfigurationSource()` in `SecurityConfig.java`, or use `http://localhost:*` for local development.

---

## 🤝 Contributing

1. **Fork** the repository
2. **Create a branch**: `git checkout -b feature/your-feature-name`
3. **Make changes** following the existing package structure and layered architecture
4. **Test your changes**: `./mvnw test`
5. **Commit**: `git commit -m "feat: add <description>"`
6. **Push**: `git push origin feature/your-feature-name`
7. **Open a Pull Request** with a clear description

### Code Conventions

- Follow the **Controller → Service → Repository** layered architecture
- Use **DTOs** for all API request/response bodies — never expose entities directly
- Use **Lombok** annotations (`@Slf4j`, `@Builder`, `@Getter`, `@Setter`) appropriately
- Add `@Valid` on all `@RequestBody` parameters in controllers
- Throw specific domain exceptions (`ResourceNotFoundException`, `UnauthorizedException`, etc.) — never generic `RuntimeException`
- Log meaningful messages using `log.info()` / `log.error()` at service level

---

## 📄 License

This project was developed as part of an academic Full-Stack Application Development (FSAD) course project.

---

<div align="center">
  <p>
    Built with ❤️ using <strong>Spring Boot</strong>, <strong>PostgreSQL</strong>, and <strong>SendGrid</strong>
  </p>
  <p>
    <a href="https://certifymeonline.vercel.app" target="_blank"><strong>🌐 Live Demo — certifymeonline.vercel.app</strong></a>
  </p>
  <p>
    <a href="https://github.com/lalithdev/CertifyMe-Certification-Tracking-Platform" target="_blank">📦 Frontend Repository</a>
  </p>
</div>
