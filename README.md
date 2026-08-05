# papaprecoapi

API do projeto papapreco

## Configuration

No secret and no environment-specific value is committed. `application.yaml`
contains only placeholders; the real values come from the environment. A
placeholder without a default is required — the application refuses to start
rather than falling back to something wrong.

| Variable | Required | Description |
|---|---|---|
| `DB_NAME` | Compose only | Database Compose creates, and the one it points the API at. Default `papapreco` |
| `DB_URL` | yes | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/papapreco`. Compose overrides this for the API container, where the database is reachable as `db` rather than as localhost |
| `DB_USERNAME` | yes | Database user |
| `DB_PASSWORD` | yes | Database password |
| `FLYWAY_LOCATIONS` | no | Default `classpath:db/migration` — schema only. Add `,classpath:db/seed` for the demo dataset; see [Database schema](#database-schema) |
| `MAIL_USERNAME` | yes | Gmail account used to send verification mail |
| `MAIL_PASSWORD` | yes | Google **App Password**, not the account password |
| `MAIL_HOST` | no | Default `smtp.gmail.com` |
| `MAIL_PORT` | no | Default `465` |
| `JWT_PUBLIC_KEY_LOCATION` | no | Default `file:./secrets/jwt.rsa.pub` |
| `JWT_PRIVATE_KEY_LOCATION` | no | Default `file:./secrets/jwt.rsa.priv` |
| `FIREBASE_CREDENTIALS_LOCATION` | no | Default `file:./secrets/firebase-service-account.json` |
| `SERVER_PORT` | no | Default `8080` |
| `JPA_SHOW_SQL` | no | Default `false` |
| `LOG_LEVEL_JPA`, `LOG_LEVEL_SECURITY`, `LOG_LEVEL_SQL`, `LOG_LEVEL_SQL_PARAMS` | no | Default `INFO` |

Log levels default to `INFO` on purpose. `LOG_LEVEL_SQL_PARAMS=TRACE` prints
bound SQL parameters, which includes password hashes and verification codes —
useful while debugging, never in a deployed environment.

### Credential files

Three files are read at runtime and none of them belongs in git. They live in
`secrets/`, which is gitignored:

| File | Required | How to obtain |
|---|---|---|
| `secrets/jwt.rsa.priv` | yes | `openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt.rsa.priv` |
| `secrets/jwt.rsa.pub` | yes | `openssl rsa -pubout -in secrets/jwt.rsa.priv -out secrets/jwt.rsa.pub` |
| `secrets/firebase-service-account.json` | no | Firebase Console → Project settings → Service accounts → *Generate new private key* |

The JWT keypair is generated with the two commands above, so anyone can produce
one. The Firebase key cannot be generated — it is a server-side admin credential
that has to be downloaded by someone with access to the Firebase project — so it
is **optional**: without it the API starts, logs a warning, and runs with push
notifications disabled. Everything else works. `/notification/trigger-manual`
answers `503` in that state rather than claiming to have sent anything.

Replacing the JWT keypair invalidates every token already issued, so users are
signed out. Tokens expire after one hour regardless.

## Running with Docker Compose

The whole stack — API and PostgreSQL — from a clean checkout:

```bash
cp .env.example .env        # then fill in the real values
docker compose up --build
```

That is roughly 15 seconds from an empty volume to an API answering queries: the
database starts, Compose waits for it to be genuinely ready rather than merely
listening, Flyway creates the schema and loads the demo dataset, and the API
reports healthy. It is served under the `/papaprecoapi` context path:

```bash
curl http://localhost:8080/papaprecoapi/actuator/health
curl 'http://localhost:8080/papaprecoapi/produtos/ranking?palavra=cafe&latitude=-25.458162&longitude=-49.29&distancia=10&precoMin=0&precoMax=100'
```

The JWT keypair described above still has to exist in `secrets/` — those files
are bind-mounted into the container read-only and are never copied into the
image. The Firebase key is optional; without it the stack comes up with push
notifications disabled.

Both services publish on `127.0.0.1` only, so neither the database nor the
unencrypted API is reachable from outside the host. `docker compose down` stops
the stack and keeps the data; `docker compose down -v` also deletes the volume,
which is how you get a fresh seeded database.

This is the same file the deployment runs. What differs between a laptop and the
server is `.env`, not the compose file.

## Running without Docker

Requires a JDK 17 or newer and a reachable PostgreSQL. Convenient when running
the API from an IDE against the database container:

```bash
docker compose up -d db     # or point DB_URL at any other PostgreSQL
set -a && . ./.env && set +a
./mvnw spring-boot:run
```

`DB_URL` in `.env` points at `localhost:5432`, which is where the database
container publishes, so this works with no further changes.

## Database schema

Flyway owns the schema and applies it during startup, so nothing has to be run
by hand. Hibernate creates nothing and validates nothing (`ddl-auto: none`).

| Location | Contents | Applied |
|---|---|---|
| `src/main/resources/db/migration` | Schema: tables, indexes, views, the `haversine` function, the `pg_trgm` extension | Always |
| `src/main/resources/db/seed` | Demo dataset: 12 shops, 5 accounts, ~40 prices, votes and alerts | Only when `FLYWAY_LOCATIONS` includes it |

Migrations are immutable once applied — Flyway records a checksum and refuses to
start if a file it has already run has changed since. Corrections go in a new
`V2__...sql`, never as an edit to `V1`.

**Every seeded account has the password `demo1234`**, e.g.
`joao.silva@example.com`. `.env.example` enables the seed because a checkout
that starts empty is a checkout nobody can evaluate; a deployed environment
must set `FLYWAY_LOCATIONS=classpath:db/migration` and leave it out.

`spring.flyway.baseline-on-migrate` is on, so a database that predates Flyway —
one built by hand from the old `outros/banco.sql` — is stamped as already being
at `V1` and picks up `V2` onwards, rather than failing on a `CREATE TABLE` for a
table that is already there.

---

# Instalamento

## 1 - Instalar JDK17

## 2 - Instalar extensões IDE (VSCode)

- Spring initializr
- Spring Boot Dashboard
- Spring Boot Tools
- Spring Boot Extension Pack

## 3 - Instalar Postman para testes (Opcional)

# Iniciar pela extensão Spring Dashboard