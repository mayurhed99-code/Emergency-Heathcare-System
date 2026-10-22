# Emergency Healthcare System

An emergency-care coordination application with a React web client and a Spring Boot REST API. The system supports public emergency requests and planned admissions, then gives hospital staff and responders role-specific tools to coordinate care.

## Features

- Submit an emergency request and receive a tracking code to follow its status.
- View nearby hospital candidates for an emergency type and location.
- Submit a planned admission request using an OTP-based verification flow.
- Let hospital users manage doctors, drivers, ambulances, beds, emergencies, admissions, and patient transfers.
- Give doctors an emergency case queue and treatment-status controls.
- Give drivers an assignment view.
- Provide administrators with hospital, emergency, admission, and transfer dashboards and reports.
- Issue JWTs for authenticated sessions and support email/SMS notification integrations. SMS defaults to simulation mode.

## Project layout

```text
.
├── emergency-healthcare-frontend/   # React 19 and Vite web application
│   └── src/
│       ├── components/              # Shared layout, dialogs, and role-specific UI
│       ├── contexts/                # Client-side application context
│       ├── pages/                   # Public, hospital, doctor, driver, and admin screens
│       ├── routes/                  # Client-side route and role protection
│       └── services/                # API client and feature-specific API calls
├── emergency-healthcare-platform/  # Spring Boot REST API
│   └── src/
│       ├── main/java/.../controller/ # HTTP endpoints
│       ├── main/java/.../service/    # Workflows and business logic
│       ├── main/java/.../entity/     # JPA entities and domain enums
│       ├── main/java/.../repository/ # Spring Data repositories
│       ├── main/java/.../security/   # JWT authentication
│       └── test/                     # Backend tests
└── README.md
```

## Technology

| Area | Stack |
| --- | --- |
| Frontend | React 19, React Router, Axios, Vite |
| Backend | Java 25, Spring Boot 3.5, Spring Web, Spring Security, Spring Data JPA, Bean Validation |
| Database | MySQL (development and default runtime configuration); H2 is included for tests |
| Build tools | npm and the Maven Wrapper |

## Requirements

- Java Development Kit (JDK) 25
- Node.js 20.19+ or 22.12+ and npm
- MySQL Server, unless configuring another compatible database

The backend Maven Wrapper downloads and uses the Maven version configured by the project; a separate Maven installation is not required.

## Run locally

Start MySQL and create a database/user, or use a MySQL account that can create the configured database. The default JDBC URL creates the `emergency_healthcare` database if it does not exist.

### 1. Configure and start the backend

In PowerShell:

```powershell
cd emergency-healthcare-platform
Copy-Item .env.example .env
```

Edit `.env` with your local database password and any notification settings you want to use. At minimum, configure `DB_USERNAME` and `DB_PASSWORD` for your MySQL instance. Set `JWT_SECRET` to a long, random value for any environment beyond local development. Keep `.env` private and do not commit it.

Start the API:

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens on `http://localhost:8080` by default. Spring reads the optional `.env` file from the backend working directory. Database connection settings, JPA behavior, mail, JWT, SMS, and the allowed frontend origin are configured in `src/main/resources/application.properties` and can be overridden with environment variables; see `.env.example` for the supported names.

### 2. Install and start the frontend

In a second terminal:

```powershell
cd emergency-healthcare-frontend
npm ci
npm run dev
```

Open the local URL printed by Vite (normally `http://localhost:5173`). The frontend uses `http://localhost:8080/api` as its default API base URL. To point it at another API, create `emergency-healthcare-frontend/.env.local` with:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api
```

The backend's `FRONTEND_ORIGIN` setting must match the frontend origin when running on a different host or port.

## Main screens and API areas

| Application area | Frontend route | API prefix |
| --- | --- | --- |
| Public home | `/` | — |
| Emergency request and tracking | `/emergency`, `/track` | `/api/public/emergencies` |
| Planned admission | `/admission` | `/api/public/admissions` |
| Sign-in | `/login` | `/api/auth` |
| Hospital operations and resources | `/hospital` | `/api/hospital`, `/api/hospital/resources` |
| Doctor case queue | `/doctor` | `/api/doctor` |
| Driver assignments | `/driver` | `/api/driver` |
| Administration and reports | `/admin` | `/api/admin` |

Hospital, doctor, driver, and admin routes require an authenticated user with an allowed role. Public emergency and admission endpoints are available without those dashboard roles. The API controllers and corresponding frontend service modules are the source of truth for request and response details.

## Notifications and verification

- Email delivery is configured with `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, and `SMTP_PASSWORD`. Use an email provider's application password where required.
- SMS settings are `SMS_BASE_URL`, `TRACCAR_TOKEN`, `SMS_SECRET_KEY`, and `SMS_SENDER_ID`. `SMS_MODE=SIMULATION` is the default; configure a real provider and its credentials before enabling live delivery.
- The admission OTP endpoint currently includes a `demoOtp` value in its response. This is for development/demo use and must not be treated as a production-safe verification mechanism.
- The default JPA setting is `update`, intended for local development. Choose and configure an appropriate schema migration and database policy before production deployment.

## Build, test, and lint

Run backend commands from `emergency-healthcare-platform`:

```powershell
.\mvnw.cmd clean test-compile
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

Run frontend commands from `emergency-healthcare-frontend`:

```powershell
npm run lint
npm run build
npm run preview
```

`npm run preview` serves the built frontend for local inspection; it is not a production web-server deployment configuration.

## Configuration reference

Backend configuration is loaded from environment variables, with optional `.env` file import:

| Variable | Purpose |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER` | Database connection |
| `JPA_DDL_AUTO`, `JPA_SHOW_SQL` | Hibernate schema and SQL logging behavior |
| `JWT_SECRET`, `JWT_EXPIRATION` | JWT signing secret and token lifetime in milliseconds |
| `FRONTEND_ORIGIN` | Allowed frontend origin |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` and `SMTP_*` options | Email delivery |
| `SMS_BASE_URL`, `TRACCAR_TOKEN`, `SMS_SECRET_KEY`, `SMS_SENDER_ID`, `SMS_MODE` | SMS integration |

The browser client accepts `VITE_API_BASE_URL` at build/dev-server startup. Vite variables are exposed to the client bundle; do not put secrets in frontend environment files.
