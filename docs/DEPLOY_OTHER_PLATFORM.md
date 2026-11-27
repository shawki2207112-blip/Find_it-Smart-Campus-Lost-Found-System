# Bonus: second deployment platform - Fly.io

This guide and `fly.toml` (project root) are provided in the zip. **The team must perform the actual deployment**;
nothing here has been deployed or tested against a live Fly.io account.

## Prerequisites

* Fly.io account and the `flyctl` CLI (`fly auth login`)
* The repository checked out with the `Dockerfile` (the same image Render uses)

## Steps

1. Choose a unique app name and edit `app = "findit-REPLACE-ME"` in `fly.toml`.
2. Create the app without deploying (keeps our `fly.toml`):
   ```bash
   fly launch --no-deploy --copy-config --name <your-app-name>
   ```
3. Create a Postgres database and attach it. Attaching sets the secret `DATABASE_URL`
   (`postgres://user:pass@host.flycast:5432/db?sslmode=disable`), which `DatabaseUrlConfig` already understands:
   ```bash
   fly postgres create --name <your-app-name>-db
   fly postgres attach <your-app-name>-db --app <your-app-name>
   ```
4. Set the secrets (they are not stored in the repository):
   ```bash
   fly secrets set JWT_SECRET="$(openssl rand -base64 48)" \
       ADMIN_EMAIL=<admin e-mail> ADMIN_PASSWORD=<choose> \
       SECURITY_EMAIL=<security e-mail> SECURITY_PASSWORD=<choose> --app <your-app-name>
   ```
5. Deploy and open:
   ```bash
   fly deploy --app <your-app-name>
   fly open --app <your-app-name>
   ```
6. Verify: `https://<your-app-name>.fly.dev/actuator/health` returns `{"status":"UP"}`; log in with the seeded admin.

## Notes

* `fly.toml` sets `PORT=8080`, which the application honours (`server.port=${PORT:8080}`), and health-checks
  `/actuator/health`. The 512 MB machine is the practical minimum for a Spring Boot JVM.
* `auto_stop_machines = "stop"` lets the machine sleep when idle (first request after sleeping is slow). Set
  `min_machines_running = 1` for always-on.
* To deploy from CI instead of the command line, create a deploy token (`fly tokens create deploy`), store it as the
  GitHub secret `FLY_API_TOKEN` and add a job running `flyctl deploy --remote-only`. This is optional and not part of
  the supplied workflow, because the brief wants GitHub Actions to deploy only to Render.
* Alternative: Railway can build the same `Dockerfile`; set the same environment variables and the Railway Postgres
  `DATABASE_URL`.
