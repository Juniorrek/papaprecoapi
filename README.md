# papaprecoapi

API do projeto papapreco

## Configuration

No secret and no environment-specific value is committed. `application.yaml`
contains only placeholders; the real values come from the environment. A
placeholder without a default is required — the application refuses to start
rather than falling back to something wrong.

| Variable | Required | Description |
|---|---|---|
| `DB_URL` | yes | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/papapreco` |
| `DB_USERNAME` | yes | Database user |
| `DB_PASSWORD` | yes | Database password |
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

Three files are needed at runtime and none of them belongs in git. They live in
`secrets/`, which is gitignored:

| File | How to obtain |
|---|---|
| `secrets/jwt.rsa.priv` | `openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt.rsa.priv` |
| `secrets/jwt.rsa.pub` | `openssl rsa -pubout -in secrets/jwt.rsa.priv -out secrets/jwt.rsa.pub` |
| `secrets/firebase-service-account.json` | Firebase Console → Project settings → Service accounts → *Generate new private key* |

Replacing the JWT keypair invalidates every token already issued, so users are
signed out. Tokens expire after one hour regardless.

## Running locally

Requires a JDK 17 or newer and a reachable PostgreSQL.

```bash
cp .env.example .env        # then fill in the real values
set -a && . ./.env && set +a
./mvnw spring-boot:run
```

The API is then served under the `/papaprecoapi` context path, e.g.
<http://localhost:8080/papaprecoapi/produtos>.

The baseline schema is `src/main/java/br/com/papaprecoapi/outros/banco.sql`.
Hibernate does not create tables (`ddl-auto: none`), so it must be applied by
hand until migrations are introduced.

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