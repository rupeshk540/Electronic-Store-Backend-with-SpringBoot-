# Zeptra — Backend

 Zeptra, a full-stack e-commerce platform with an AI-powered shopping assistant.

Live API:https://zeptra-app-latest.onrender.com
Frontend Repo: https://github.com/rupeshk540/Electronic_store-Using-React.
Live Demo:https://zeptra-app.netlify.app/

---

## Overview

This repository contains the backend API for Zeptra — handling authentication, product/category/collection management, cart and wishlist persistence, order processing, payments, and an AI-powered chatbot endpoint. It's built with Spring Boot, backed by MySQL, containerized with Docker, and deployed through a fully automated CI/CD pipeline.

## Features

--> RESTful API — Clean, resource-based endpoints for products, categories, collections, users, cart, wishlist, and orders
--> Authentication & Authorization — JWT-based auth, Spring Security, role-based access control, Google OAuth2 login
--> AI Shopping Assistant — Chatbot endpoint integrated with Google's Gemini API for conversational product recommendations
--> Payments — Razorpay integration for secure checkout
--> Image Management — Product, category, and profile image uploads via Cloudinary
--> Admin Operations — Endpoints for managing products, categories, collections, orders, and users

## Tech Stack

--> Language/Framework: Java 21, Spring Boot
--> Security: Spring Security, JWT
--> Data: Spring Data JPA / Hibernate, MySQL (hosted on Aiven)
--> Integrations: Cloudinary (images), Razorpay (payments), Google Gemini API (AI assistant)
--> Containerization: Docker (multi-stage build, JRE-Alpine runtime image)
--> CI/CD: GitHub Actions → Docker Hub → Render

## Architecture

```
┌──────────────┐      ┌────────────────────┐      ┌───────────────┐
│ React Client │─────▶│   Spring Boot API   │─────▶│  Aiven MySQL  │
└──────────────┘      │  (this repository)   │      └───────────────┘
                       └──────────┬───────────┘
                                  │
                   ┌──────────────┼──────────────┐
                   ▼              ▼               ▼
             Cloudinary      Razorpay        Gemini API
             (images)        (payments)      (AI chat)
```

## CI/CD Pipeline

Every push to `master` runs a fully automated pipeline via GitHub Actions:

```
Git Push
   │
   ▼
GitHub Actions
   │
   ├─ Build project (Maven)
   ├─ Build Docker image (multi-stage: JDK build → JRE-Alpine runtime)
   ├─ Push image to Docker Hub
   │     tagged  :latest  and  :<commit-sha>
   └─ Trigger Render deploy hook
          │
          ▼
   Render pulls latest image → live in production
```

The commit-SHA tag on every image provides an instant rollback path — Render can be pointed at any previous tag without rebuilding.

## Getting Started

### Prerequisites
- Java 21
- Maven (or use the included `mvnw` wrapper)
- Docker & Docker Compose
- MySQL (or use the provided Docker Compose setup)

### Run with Docker Compose (recommended)

Spins up the Spring Boot app and a MySQL container together:

```bash
git clone https://github.com/rupeshk540/Electronic-Store-Backend-with-SpringBoot-
cd your-backend-repo-name
docker-compose up --build
```

### Run Locally (without Docker)

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### Environment Variables

Configure the following (see `src/main/resources/application.properties`):

```
# Database
DB_URL=jdbc:mysql://localhost:3306/zeptra_store
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_DEFAULT_PASSWORD=your_default_password

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Razorpay
RAZORPAY_KEY_ID=your_key_id
RAZORPAY_KEY_SECRET=your_key_secret

# Gemini AI
GEMINI_API_KEY=your_gemini_key
```

**Note:** `application.properties` itself only contains `${ENV_VAR}` placeholders — no secrets are stored in the repository. All actual credentials are injected via environment variables at runtime (locally via `.env` / IDE run config, in production via Render's environment settings).


## Engineering Highlights

Real production issues diagnosed and resolved while building and deploying this service:

- **Connection pool race conditions** — Fixed Hikari/MySQL startup ordering in Docker Compose using healthchecks and `condition: service_healthy`
- **Managed database constraints** — Resolved schema creation failures caused by Aiven's `sql_require_primary_key` enforcement on JPA `@ElementCollection` fields, using `@OrderColumn` to give Hibernate a valid composite key
- **`List` vs `Set` in `@ManyToMany`** — Fixed a similar primary-key issue on a join table by switching a `List<Role>` to `Set<Role>`, since Hibernate can't safely assign a PK to a collection that may contain duplicates
- **Docker image size** — Cut image size significantly by switching the runtime base image from a full JDK to a JRE-only Alpine image, reducing deploy time
- **Environment variable propagation** — Traced and fixed a chain of issues where property names didn't match between `application.properties` and the hosting platform's environment configuration, including a `.gitignore` misconfiguration that stripped `application.properties` out of the CI build entirely

## Deployment

Deployed on **Render** as a Docker container, pulling directly from Docker Hub. Database is a managed MySQL instance on **Aiven**. Both are on free tiers suitable for demo/portfolio use — not intended for high-traffic production load.

## Contact

**Rupesh Kumar**
[GitHub] (https://github.com/rupeshk540) • [LinkedIn](https://linkedin.com/in/rupesh-kumarr)

Open to Full-Stack / Backend Developer roles 
