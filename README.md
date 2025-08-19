# token-svc
Token-Svc can be used to authorize access to resources in a decoupled and reusable manner within a micro-service architecture. 
This tool allows to manage principals and levels of access for each of the resources in the micro-service environment.

## Running the Application

### 1. Developer Mode (Local)

This mode is useful for development, live reload, and debugging.  

**Prerequisites:**
- Java 17+
- Maven
- Node.js & npm (for Vaadin frontend)

**Steps:**
```bash
# Clone the repository
git clone <your-repo-url>
cd token-svc

# Build the project
mvn clean install

# Run the application in dev mode
mvn quarkus:dev

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
