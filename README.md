# VeloRent — Web-Based Car Rental System

VeloRent is a streamlined web application for managing vehicle rentals end‑to‑end.

## 📘 Introduction

The Web‑Based Car Rental System, developed as part of the SE2030 Software Engineering module, is a comprehensive platform designed to streamline the vehicle rental process through an efficient and user‑friendly web application. The project aims to automate and enhance the management of car rentals, replacing outdated manual processes such as Excel‑based tracking with a robust digital solution.

## 🎯 Objectives

The primary objectives are to facilitate seamless car booking, efficient user and fleet management, insightful business analytics, and secure payment processing, all while ensuring a scalable and reliable system.

## ✨ Key Features

- 🔎 Vehicle discovery and live availability
- ⚡ Quick booking flow and rental history
- 🧭 Fleet/owner dashboard and vehicle lifecycle tracking
- 🔐 Role‑based access (Customer, Fleet Manager, Owner, Admin)
- 💳 Secure payments (cash/card) and receipts
- 📊 Reports and business insights

## 🛠️ Tech Stack

- 🔧 Backend: Java 17, Spring Boot 3.x, Maven
- 🎨 Frontend: Thymeleaf, Bootstrap, JavaScript
- 🗄️ Data: Relational database (configured via `application.properties`), SQL scripts under `src/main/resources/db`

🧰 **Tech Stack**

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Backend | Spring Boot 3.5.5 |
| Build Tool | Maven |
| Database | Microsoft SQL Server |
| API Design | RESTful Web Services |
| Tools/IDE | IntelliJ IDEA |

## 🚀 Quick Start (Windows)

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

## 🧱 Project Structure (brief)

- 📁 `Car-Rental-System/src/main/java/Group2/Car` — controllers, services, repositories, models
- 🧩 `Car-Rental-System/src/main/resources/templates` — Thymeleaf pages
- 🎛️ `Car-Rental-System/src/main/resources/static` — CSS/JS/assets
- 🗂️ `Car-Rental-System/src/main/resources/db` — schema and seed SQL

## 🚗 Vehicle Rental Process

<details open>
<summary><strong>Quick steps</strong></summary>

1) Discover & Search
   - Browse available vehicles by type, brand, seats, transmission, or price.
   - Real-time availability is shown based on your selected dates.

2) Select Vehicle & View Details
   - See photos, specs, daily price, and any included packages or add-ons.
   - System calculates an estimated total based on duration and pricing rules.

3) Create Booking
   - Provide pickup/return dates and locations, driver details, and contact info.
   - Reservation is validated against overlapping bookings to prevent double-booking.

4) Payment
   - Pay by cash or card (per project scope). Card payments are processed securely; a receipt is generated.
   - On success, a booking reference is stored and shared with the user.

5) Handover & Usage
   - Vehicle is handed over at pickup time; optional checklist and condition photos may be recorded.
   - During rental, support is available via the Fleet team for extensions or issues.

6) Return & Close-out
   - Vehicle inspection on return; extra charges (fuel, damages, late fees) are calculated if applicable.
   - Final invoice and receipt are issued; rental status is updated, history is recorded.

7) Post-Rental
   - User can view rental history and download receipts.
   - Optional feedback can be submitted to improve service quality.

</details>

## 📦 Package Booking Process

<details open>
<summary><strong>Quick steps</strong></summary>

1) Explore Packages
   - Navigate to Packages and compare offerings (e.g., daily/weekly plans, mileage limits, bundled services).

2) Choose Package & Vehicle
   - Select a package and pair it with a suitable vehicle category or a specific vehicle when required.
   - Pricing adapts to package rules (fixed price, tiered duration, or mileage-based).

3) Configure Dates & Add-ons
   - Set start/end dates and choose any add-ons (GPS, child seat, insurance upgrades).
   - The system validates availability for the full package duration.

4) Confirm & Pay
   - Review a transparent cost breakdown; proceed with cash/card payment.
   - On success, a confirmation with the package details and booking ID is issued.

5) Manage Booking
   - Users can view/modify/cancel (subject to policy) from their dashboard.
   - Fleet Managers can adjust assignments if vehicles change due to maintenance.

</details>

## 👥 Roles and Main Functions

<details open>
<summary><strong>Owner</strong></summary>
- View portfolio performance: revenue, utilization, and active vehicles.
- Manage owned vehicles: add/update details, documents, availability windows.
- Review payouts, invoices, and settlement history.
- Monitor bookings associated with owned vehicles; approve or escalate issues.

</details>

<details open>
<summary><strong>Fleet Manager</strong></summary>
- Fleet operations dashboard: real-time availability, current rentals, returns due.
- Booking oversight: verify reservations, assign vehicles, handle extensions/cancellations.
- Maintenance & lifecycle: schedule services, mark vehicles as unavailable, track usage history.
- Package management: create/update packages, pricing, and promotions.
- Reports: export fleet usage, bookings, and maintenance logs (Excel/PDF where enabled).

</details>

<details open>
<summary><strong>System Administrator</strong></summary>
- User and role management: create/disable accounts, assign roles (Customer, Fleet Manager, Owner, Admin).
- Security & authentication: oversee login policies, 2FA, password resets.
- System configuration: environment settings, file upload limits, and integrations.
- Audit & monitoring: review logs, anomalous activities, and ensure data consistency.

</details>

## Team Members

| Team Member | Role | Key Contributions |
|-------------|------|-------------------|
| Jayanath P.A.P.R | Payment System Developer | Payment gateway integration, transaction processing, payment validation and security |
| Bandara I.G.C | Lead Developer & Authentication Specialist | User login/register, forgotten password recovery, 2FA, Fleet Manager Dashboard (Part II) |
| Liyanage J.A.K | Owner Management Developer | Owner portal, owner-related functionalities, owner data management |
| Makshuma B.G.K.J. | Customer Experience Developer | Customer dashboard, profile management, customer analytics and reporting |
| Rodrigo H.V.O. | Fleet Management Developer | Vehicle inventory, fleet operations, fleet analytics and reporting |
| Dasanayake H.T.N. | System Administrator | System configuration, user management, administrative controls, system monitoring |

## 🤝 Contributing & License

Contributions are welcome via pull requests. This project is for academic purposes (SLIIT). See repository license or course guidelines where applicable.
