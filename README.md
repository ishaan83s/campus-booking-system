# Campus Booking System

Internal College Office Hours Booking System built using **Spring Boot**, following Clean Architecture principles, REST API design, and a collaborative Git workflow.

## Overview

Campus Booking System lets students book office hours with professors, manages waitlists when slots are full, and gives professors a dashboard to manage their availability  all backed by a JWT-authenticated Spring Boot API.

## Features

- 🎓 **Student Booking** — browse and book available office hour slots
- 🧑‍🏫 **Professor Dashboard** — manage availability and view upcoming bookings
- 🔐 **Authentication** — secure JWT-based login for students and professors
- ⏳ **Waitlist** — automatic waitlisting and promotion when slots free up
- ⚖️ **Conflict Resolution** — prevents double-booking and overlapping slots

## Tech Stack

**Backend**
- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL

**Frontend**
- HTML
- CSS
- Bootstrap
- JavaScript

## Repository Structure

```
backend/    # Spring Boot application (REST APIs, business logic, persistence)
frontend/   # HTML/CSS/JS client
docs/       # Project documentation (SSOT, architecture notes, specs)
postman/    # Postman collections for API testing
```

## Getting Started

### Prerequisites
- Java 17+
- Maven
- MySQL

### Backend Setup
```bash
cd backend
# configure your MySQL connection in application.properties
mvn spring-boot:run
```

### Frontend Setup
```bash
cd frontend
# open index.html in your browser, or serve with a static server
```

API collections for testing endpoints are available in the `postman/` directory.

## The Team

| Member            | Course               | Year |
|-------------------|----------------------|------|
| Ishaan Chavan     | Computer Engineering | TE   |
| Vivaan Chauhan | Computer Engineering | TE   |
| Hritik Chauhan     | Computer Engineering | TE   |
| Krish Choudhary  | Computer Engineering | TE   |

## License

MIT