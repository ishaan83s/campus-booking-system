# 🎓 Campus Booking System

A full-stack college office-hours booking platform that allows students to book appointments with professors, professors to manage office-hour availability, and administrators to monitor and manage the system.

The application provides JWT-based authentication, role-based authorization, slot management, booking and cancellation workflows, waitlists, and administrative reporting.

## 🚀 Live Demo

### Frontend
https://campus-booking-system-fxsr.onrender.com

### Backend API
https://campus-booking-backend-s6kf.onrender.com

### Backend Health Check
https://campus-booking-backend-s6kf.onrender.com/health

---

## ✨ Features

### 🎓 Student

- Register and log in securely
- Browse available professors
- View available office-hour slots
- Book available slots
- Automatically join a waitlist when slots are full
- View booking history
- Cancel bookings
- Leave waitlists
- Manage student profile
- View booking and waitlist status

### 🧑‍🏫 Professor

- Register and log in as a professor
- Create office-hour slots
- Define slot date, time, and capacity
- View upcoming slots
- Edit existing slots
- Cancel slots
- View student rosters
- View waitlisted students

### 🛡️ Administrator

- View system-wide statistics
- View total users, professors, and students
- Monitor active bookings and waitlists
- View slot utilization
- Activate or deactivate users
- Force-cancel bookings

### 🔐 Authentication & Security

- JWT-based authentication
- Password hashing using BCrypt
- Role-based authorization
- Stateless Spring Security configuration
- Protected API endpoints
- CORS configuration for local and production frontends
- Public production health endpoint

### ⏳ Booking & Waitlist System

- Prevents conflicting bookings
- Capacity-aware slot booking
- Automatic waitlisting for full slots
- Booking cancellation
- Waitlist management
- Professor slot management

---

## 🏗️ Architecture

```text
                         ┌──────────────────────┐
                         │       Users          │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │   React + Vite       │
                         │     Frontend         │
                         │      Render          │
                         └──────────┬───────────┘
                                    │ HTTPS / REST
                                    ▼
                         ┌──────────────────────┐
                         │   Spring Boot API    │
                         │       Docker         │
                         │       Render         │
                         └──────────┬───────────┘
                                    │
                              JPA / Hibernate
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │      Aiven MySQL     │
                         │      Production DB   │
                         └──────────────────────┘
```

---

## 🛠️ Tech Stack

### Frontend

- React
- Vite
- JavaScript
- Bootstrap
- Custom CSS

### Backend

- Java 17
- Spring Boot 4.1.0
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- JWT
- Maven

### Database

- MySQL 8
- Aiven Cloud MySQL
- JDBC
- Hibernate ORM

### Deployment

- Docker
- Render
- GitHub
- Aiven

### Monitoring

- UptimeRobot
- `/health` production health endpoint

---

## 📁 Repository Structure

```text
campus-booking-system/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── backend/
│   │   │   │       ├── admin/
│   │   │   │       ├── auth/
│   │   │   │       ├── booking/
│   │   │   │       ├── config/
│   │   │   │       ├── professor/
│   │   │   │       ├── security/
│   │   │   │       └── ...
│   │   │   └── resources/
│   │   │
│   │   ├── test/
│   │   │
│   │   └── pom.xml
│   │
│   └── Dockerfile
│
├── frontend/
│   ├── src/
│   │   ├── main.jsx
│   │   ├── styles.css
│   │   └── ...
│   │
│   ├── package.json
│   └── index.html
│
├── docs/
│   └── Project documentation
│
├── postman/
│   └── API collections
│
└── README.md
```

---

# ⚙️ Local Development

## Prerequisites

Make sure you have:

- Java 17
- Maven
- Node.js
- npm
- MySQL (if running a local database)

---

## 🔧 Backend

Navigate to the backend:

```bash
cd backend
```

Build the project:

```bash
mvn clean package
```

Run the application:

```bash
mvn spring-boot:run
```

The backend runs locally on:

```text
http://localhost:8080
```

### Environment Variables

The backend supports environment-based configuration:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
APP_JWT_SECRET
APP_JWT_EXPIRATION_SECONDS
PORT
```

Example:

```bash
export DB_URL="jdbc:mysql://localhost:3306/office_hours_booking"
export DB_USERNAME="root"
export DB_PASSWORD="your-password"
export APP_JWT_SECRET="your-secret"
export APP_JWT_EXPIRATION_SECONDS="86400"
```

> Never commit production database credentials or JWT secrets to GitHub.

---

## 💻 Frontend

Navigate to the frontend:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The Vite development server will provide a local URL, typically:

```text
http://localhost:5173
```

### Frontend Environment Variable

The frontend uses:

```text
VITE_API_URL
```

For local development:

```text
VITE_API_URL=http://localhost:8080
```

For production:

```text
VITE_API_URL=https://campus-booking-backend-s6kf.onrender.com
```

---

# 🐳 Docker

The backend can be built and run using Docker.

From the `backend/` directory:

```bash
docker build -t campus-booking-backend .
```

Run:

```bash
docker run --rm \
  -p 8080:8080 \
  -e PORT=8080 \
  -e DB_URL="your-db-url" \
  -e DB_USERNAME="your-db-username" \
  -e DB_PASSWORD="your-db-password" \
  -e APP_JWT_SECRET="your-jwt-secret" \
  -e APP_JWT_EXPIRATION_SECONDS=86400 \
  campus-booking-backend
```

---

# 🌐 Production Deployment

The production deployment uses the following architecture:

```text
GitHub
   │
   ├── main branch
   │
   ▼
Render
   │
   ├── Frontend → React/Vite Static Site
   │
   └── Backend → Docker Web Service
                    │
                    ▼
                 Aiven
                 MySQL
```

### Backend

Hosted on Render using Docker.

Production API:

```text
https://campus-booking-backend-s6kf.onrender.com
```

### Frontend

Hosted on Render as a static site.

Production frontend:

```text
https://campus-booking-system-fxsr.onrender.com
```

### Database

Production MySQL database is hosted on Aiven.

The backend connects to Aiven using JDBC with SSL enabled.

---

# ❤️ Health Monitoring

The backend exposes a public health endpoint:

```http
GET /health
```

Production:

```text
https://campus-booking-backend-s6kf.onrender.com/health
```

Expected response:

```json
{
  "status": "UP"
}
```

This endpoint can be monitored using UptimeRobot to detect backend availability issues.

---

# 🔌 API Overview

## Authentication

```http
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
```

## Student

```http
GET  /api/professors
GET  /api/professors/{id}/slots

POST /api/bookings
GET  /api/bookings/me
DELETE /api/bookings/{id}

DELETE /api/bookings/waitlist/{id}

GET  /api/students/me
PUT  /api/students/me
```

## Professor

```http
GET    /api/professors/slots/me
POST   /api/professors/slots
PUT    /api/professors/slots/{id}
DELETE /api/professors/slots/{id}

GET /api/professors/slots/{id}/bookings
```

## Administration

```http
GET   /api/admin/reports
GET   /api/admin/users

PATCH /api/admin/users/{id}/activate
PATCH /api/admin/users/{id}/deactivate

DELETE /api/admin/bookings/{id}
```

> Refer to the `postman/` directory for API testing collections.

---

# 🔒 Security

The application uses:

- Spring Security
- JWT authentication
- BCrypt password hashing
- Stateless authentication
- Role-based authorization
- CORS protection
- Environment-based secrets

Production secrets are stored as environment variables on Render and are **not committed to the repository**.

Never commit:

```text
DB_PASSWORD
APP_JWT_SECRET
```

or other production credentials.

---

# 🧪 Testing

Backend compilation:

```bash
cd backend
mvn clean package -DskipTests
```

Frontend production build:

```bash
cd frontend
npm run build
```

Docker build:

```bash
cd backend
docker build -t campus-booking-backend .
```

Production health check:

```bash
curl -i https://campus-booking-backend-s6kf.onrender.com/health
```

---

# 👥 Team

| Member | Course | Year |
|---|---|---|
| Ishaan Chavan | Computer Engineering | TE |
| Vivaan Chauhan | Computer Engineering | TE |
| Hritik Chauhan | Computer Engineering | TE |
| Krish Choudhary | Computer Engineering | TE |

---

# 📄 License

MIT License
