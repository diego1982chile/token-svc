# token-svc
Token-Svc is a proof of concept for identity, JWT issuance, a Vaadin administration UI, and email confirmation in Quarkus.

The current scope is single realm, single audience, and single client. `JWT_ISSUER` identifies the realm and `JWT_AUDIENCE` identifies the only supported audience. Roles are internal to this service, not reusable application roles for a broader microservice ecosystem.

See [docs/scope.md](docs/scope.md) for the project scope and [docs/onboarding.md](docs/onboarding.md) for notes about the onboarding flow and service boundaries.

## Running the Application

### 1. Developer Mode (Local)

This mode is useful for development, live reload, and debugging.  

**Prerequisites:**
- Java 21+
- Maven
- Node.js & npm (for Vaadin frontend)

**Steps:**

# Clone the repository
git clone <your-repo-url>
cd token-svc

# Build the project
mvn clean install

# Run the application in dev mode
mvn quarkus:dev

### Local Email Preview

Development mode sends email through Mailpit instead of using a mock mailbox. Start Mailpit before creating users:

```bash
docker compose up mailpit
```

Open the Mailpit UI at:

```text
http://localhost:8025
```

The application sends SMTP traffic to `localhost:1025` in the dev profile. Tests still use `quarkus.mailer.mock=true`.

### 2. Running in Container (Production Profile)

This mode runs the application using Docker, suitable for production or containerized environments.

Prerequisites:

Docker

Docker Compose

Steps:

# Build the application with production profile
mvn clean install -Pproduction -Dquarkus.profile=prod

# Start the application using Docker Compose
docker-compose up --build


The application will be available at http://localhost:8080 (or configured port in docker-compose.yml).

Stopping containers:

docker-compose down
