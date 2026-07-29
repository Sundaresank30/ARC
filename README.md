# ARC - Automated Manufacturing System

ARC is an end-to-end manufacturing management system built with a **Spring Boot** backend and a **React + Vite** frontend. It streamlines manufacturing workflows across line supervision (**Managers**) and shop-floor operations (**Operators**).

---

## 📁 Repository Structure

- [**`arc-frontend`**](arc-frontend/README.md): React 19 + TypeScript + Vite + Tailwind CSS frontend portal.
- [**`arc-backend`**](arc-backend/README.md): Spring Boot 3 + Java 21 + JPA/Hibernate + PostgreSQL REST API server.

---

## ⚡ Quick Start

### 1. Start Backend Server
```bash
cd arc-backend
mvn spring-boot:run
```
- API Base URL: `http://localhost:8080`
- Database: PostgreSQL / H2 In-Memory

### 2. Start Frontend App
```bash
cd arc-frontend
npm install
npm run dev
```
- Web Application: `http://localhost:3000`