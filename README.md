# FitCubes – Backend

Spring Boot 3.4 (Java 21) backend. PostgreSQL for data, Redis for cache/JWT blacklist, Liquibase for DB migrations.

## Requirements

- Docker + Docker Compose

## Run it (Docker)

```bash
git clone https://github.com/FitCubes/backend.git
cd backend
cp .env.sample .env   # fill in the values below
docker compose up --build
```

API runs at `http://localhost:8080`. DB migrations run automatically on startup.

### `.env` values to fill in

```
POSTGRES_DB=fitcubes_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_postgres_password

JWT_SECRET=at_least_32_characters_long
JWT_EXPIRATION=86400000        # optional, defaults to 24h

DB_HOST=localhost              # optional, default is fine locally
POSTGRES_PORT=5432
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=                # optional, empty by default
```

## Run without Docker

```bash
docker compose up postgres redis   # just the deps
./mvnw spring-boot:run
```
Env vars are read from `.env` in the repo root (or set them manually).

## Tests

```bash
./mvnw test
```