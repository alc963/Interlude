# Interlude

Personal listening habit tracker / music diary

### Local development

#### Prerequisites

- JDK 21
- Docker Desktop with the Linux engine running

#### First-time setup

Copy `.env.example` to `.env` and set the local PostgreSQL values. The `.env` file is ignored by Git and must not be committed.

```bash
cp .env.example .env
```

#### Start the backend locally

The launcher starts PostgreSQL in Docker, reads `.env`, sets `DB_HOST` and `DB_PORT` defaults to `localhost` and `5432`, and runs Spring Boot directly on the host.

```bash
./scripts/run-backend.sh
```

On Windows PowerShell, run the equivalent script:

```powershell
.\scripts\run-backend.ps1
```

The backend is available at `http://localhost:8080/`.

Stop the backend with `Ctrl+C`. When you are finished with the local environment, stop the Docker services:

```bash
docker compose down
```

#### Start the full Docker environment

To run PostgreSQL, the backend, and the frontend in Docker instead:

```bash
sh scripts/dev-env.sh
```

Access the Docker environment at:

- Frontend: `http://localhost:8081/`
- Backend: `http://localhost:8082/`
