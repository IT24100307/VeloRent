# VeloRent — Web-Based Car Rental System

VeloRent is a streamlined web application for managing vehicle rentals end‑to‑end.

## Introduction

The Web‑Based Car Rental System, developed as part of the SE2030 Software Engineering module, is a comprehensive platform designed to streamline the vehicle rental process through an efficient and user‑friendly web application. The project aims to automate and enhance the management of car rentals, replacing outdated manual processes such as Excel‑based tracking with a robust digital solution.

## Objectives

The primary objectives are to facilitate seamless car booking, efficient user and fleet management, insightful business analytics, and secure payment processing, all while ensuring a scalable and reliable system.

## Key Features

- Vehicle discovery and live availability
- Quick booking flow and rental history
- Fleet/owner dashboard and vehicle lifecycle tracking
- Role‑based access (Customer, Fleet Manager, Owner, Admin)
- Secure payments (cash/card) and receipts
- Reports and business insights

## Tech Stack

- Backend: Java 17, Spring Boot 3.x, Maven
- Frontend: Thymeleaf, Bootstrap, JavaScript
- Data: Relational database (configured via `application.properties`), SQL scripts under `src/main/resources/db`

## Quick Start (Windows)

Prerequisites: JDK 17+, Git, Maven (or Maven Wrapper).

1) Clone the repo
   - git clone https://github.com/IT24100307/VeloRent.git
   - cd VeloRent/Car-Rental-System

2) Run the app
   - .\mvnw.cmd spring-boot:run

App URL: http://localhost:8080 (API base: /api)

To change the port, set in `Car-Rental-System/src/main/resources/application.properties`:

```
server.port=9090
```

## Project Structure (brief)

- `Car-Rental-System/src/main/java/Group2/Car` — controllers, services, repositories, models
- `Car-Rental-System/src/main/resources/templates` — Thymeleaf pages
- `Car-Rental-System/src/main/resources/static` — CSS/JS/assets
- `Car-Rental-System/src/main/resources/db` — schema and seed SQL

## Team Members

- Jayanath P.A.P.R — Payment System Developer: Payment gateway integration, transaction processing, payment validation and security
- Bandara I.G.C — Lead Developer & Authentication Specialist: User login/register, forgotten password recovery, 2FA, Fleet Manager Dashboard (Part II)
- Liyanage J.A.K — Owner Management Developer: Owner portal, owner-related functionalities, owner data management
- Makshuma B.G.K.J. — Customer Experience Developer: Customer dashboard, profile management, customer analytics and reporting
- Rodrigo H.V.O. — Fleet Management Developer: Vehicle inventory, fleet operations, fleet analytics and reporting
- Dasanayake H.T.N. — System Administrator: System configuration, user management, administrative controls, system monitoring

## Contributing & License

Contributions are welcome via pull requests. This project is for academic purposes (SLIIT). See repository license or course guidelines where applicable.
