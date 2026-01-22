# Smart Hospital Queue Management System

A comprehensive web-based application for efficient patient queue management in hospitals and clinics.

## 🏥 Overview

This system addresses common hospital challenges such as:
- Long waiting times
- Overcrowding in OPD areas
- Lack of transparency in queue status
- Inefficient patient flow across departments

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (React)                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐│
│  │  Patient    │ │   Token     │ │  Live Queue │ │   Staff     ││
│  │Registration │ │  Details    │ │   Board     │ │ Dashboard   ││
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘│
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ REST APIs (JSON)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Backend (Spring Boot)                         │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Controllers Layer                        ││
│  │  PatientController │ TokenController │ QueueController      ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Service Layer                            ││
│  │  PatientService │ TokenService │ QueueService │ StaffService││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Repository Layer                         ││
│  │  Spring Data JPA Repositories                               ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Security Layer                           ││
│  │  Spring Security (Basic Authentication)                     ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ JPA/Hibernate
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      MySQL Database                              │
│  patients │ doctors │ departments │ tokens │ queue_status │ staff│
└─────────────────────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
SmartQueue/
├── backend/                    # Spring Boot Application
│   ├── src/main/java/
│   │   └── com/hospital/queue/
│   │       ├── config/         # Security & App Configuration
│   │       ├── controller/     # REST Controllers
│   │       ├── dto/            # Data Transfer Objects
│   │       ├── entity/         # JPA Entities
│   │       ├── repository/     # Spring Data Repositories
│   │       ├── service/        # Business Logic
│   │       └── exception/      # Custom Exceptions
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/                   # React Application
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── styles/
│   └── package.json
└── database/                   # SQL Scripts
    └── schema.sql
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- Node.js 18+ and npm
- MySQL 8.0+

### Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE smart_queue;
```

2. Run the schema script:
```bash
mysql -u root -p smart_queue < database/schema.sql
```

### Backend Setup

1. Navigate to backend directory:
```bash
cd backend
```

2. Configure database connection in `src/main/resources/application.properties`

3. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

The backend will start at `http://localhost:8080`

### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm start
```

The frontend will start at `http://localhost:3000`

## 🔐 Default Credentials

| Role   | Username | Password  |
|--------|----------|-----------|
| Admin  | admin    | admin123  |
| Doctor | doctor1  | doctor123 |
| Staff  | staff1   | staff123  |

## 📋 API Endpoints

### Patient APIs
- `POST /api/patients` - Register new patient
- `GET /api/patients/{id}` - Get patient details
- `GET /api/patients/phone/{phone}` - Find patient by phone

### Token APIs
- `POST /api/tokens` - Generate new token
- `GET /api/tokens/{tokenNumber}` - Get token details
- `GET /api/tokens/patient/{patientId}` - Get patient's active tokens
- `GET /api/tokens/queue-position/{tokenNumber}` - Get queue position

### Queue APIs
- `GET /api/queue/department/{deptId}` - Get department queue
- `GET /api/queue/doctor/{doctorId}` - Get doctor's queue
- `GET /api/queue/current/{doctorId}` - Get current token being served
- `GET /api/queue/waiting-time/{tokenNumber}` - Get estimated waiting time

### Staff APIs (Authenticated)
- `POST /api/staff/call-next/{doctorId}` - Call next patient
- `POST /api/staff/start-consultation/{tokenId}` - Start consultation
- `POST /api/staff/end-consultation/{tokenId}` - End consultation
- `POST /api/staff/mark-priority/{tokenId}` - Mark as priority

### Department APIs
- `GET /api/departments` - Get all departments
- `GET /api/departments/{id}/doctors` - Get doctors by department

### Doctor APIs
- `GET /api/doctors` - Get all doctors
- `GET /api/doctors/{id}` - Get doctor details

## 🎯 Core Features

1. **Virtual Queue Management** - No physical waiting lines required
2. **Real-time Queue Status** - Live updates on queue position
3. **Dynamic Waiting Time** - Estimated wait based on consultation history
4. **Priority Handling** - Emergency, senior citizens, pregnant women
5. **Multi-department Support** - OPD, diagnostics, consultation rooms
6. **Staff Dashboard** - Complete patient flow control

## 📊 Database Schema

See `database/schema.sql` for the complete database design.

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA
- **Frontend**: React 18, Axios, React Router, CSS3
- **Database**: MySQL 8.0
- **Build Tools**: Maven, npm

## 📄 License

This project is licensed under the MIT License.
