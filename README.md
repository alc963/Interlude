# Interlude

Interlude is a social music listening habit tracker.

## Getting Started

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

## Testing

> Requires `act` to be installed.

```bash
sh scripts/verify.sh
```

## Deployment

This application is built with deployment to a single VPS in mind. If you have all the environment variables set, and all the secrets set *(in GitHub)*, then the deploy.yml workflow automatically deploys the application.
Or if you use `.secrets` you can deploy manually using:
```bash
sh scripts/force-deploy.sh
```

## Built With
- Java Spring Boot - Backend Framework
- Postgres - Database Server
- TypeScript React - Frontend Web Framework

## Versioning
We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/alc963/Interlude/tags).

## Contributors
<a href="https://github.com/alc963/Interlude/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=alc963/Interlude" />
</a>

## License

This project is licensed under the GNU General Public License - see the [LICENSE.md](LICENSE.md) file for details