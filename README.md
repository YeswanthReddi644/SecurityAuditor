# SecureScan AI — Connected Frontend

React + Vite frontend connected to the Spring Boot SecureScan AI backend.

## Requirements
- Node.js 20+ (18+ may work)
- npm
- PostgreSQL and the backend environment variables configured
- GitHub App installation with access to the SecurityAuditor repository

## Run frontend

1. Copy `.env.example` to `.env` (default API URL is `http://localhost:8080`).
2. Install packages and run Vite:

```bash
npm install
npm run dev
```

3. Open the Vite URL printed in the terminal (normally `http://localhost:5173`).

## Backend integration

- Signup: `POST /api/v1/users`
- Login: `POST /api/v1/auth/login` (backend returns a JWT as plain text)
- Repositories: `GET/POST /api/v1/repositories`
- Start scan: `POST /api/v1/scans`
- Scan status: `GET /api/v1/scans/{scanId}`
- Scan history: `GET /api/v1/scans/history/{repositoryId}`
- Findings: `GET /api/v1/scans/{scanId}/findings`

JWT is stored in browser localStorage and sent as `Authorization: Bearer <token>` for protected endpoints. Sign out removes the token.

## Backend changes included

- CORS allows `http://localhost:5173` and `http://localhost:5174`.
- Added authenticated `GET /api/v1/repositories`, returning only the signed-in user's repositories.
- Semgrep workflow accepts `target_repo`, checks out that repository, and uses a scan-ID-specific workflow run name so the backend can match the run.

## Build

```bash
npm run build
npm run preview
```

## Important GitHub workflow note

The workflow checkout currently uses `github.token`. This works for public target repositories. For private target repositories, configure a GitHub App/PAT token with access to the target repository and use it for checkout. Keep tokens and API keys in backend environment variables or GitHub Actions secrets; do not place them in frontend code.
