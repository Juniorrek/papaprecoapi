# papaprecoapi: configuration reference

The full environment variable list and the details of how the schema is managed.
Only the variables in [`.env.example`](../.env.example) have to be set to run
the project; see [../README.md](../README.md). Everything else here has a
working default and is documented for when you need to change it.

No secret and no environment-specific value is committed. `application.yaml`
contains only placeholders; the real values come from the environment. A
placeholder without a default is required: the application refuses to start
rather than falling back to something wrong.

## Environment variables

### Required

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/papapreco`. Compose overrides this for the API container, where the database is reachable as `db` rather than as localhost |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `MAIL_USERNAME` | Gmail account used to send verification mail |
| `MAIL_PASSWORD` | Google **App Password**, not the account password |

### Set in Compose, or commonly changed

| Variable | Default | Description |
|---|---|---|
| `DB_NAME` | `papapreco` | Database Compose creates, and the one it points the API at |
| `FLYWAY_LOCATIONS` | `classpath:db/migration` | Schema only. Add `,classpath:db/seed` for the demo dataset. See [Database schema](#database-schema) |

### Optional, with defaults

Nothing needs to set these.

| Variable | Default | Description |
|---|---|---|
| `MAIL_HOST` | `smtp.gmail.com` | |
| `MAIL_PORT` | `465` | |
| `JWT_PUBLIC_KEY_LOCATION` | `file:./secrets/jwt.rsa.pub` | |
| `JWT_PRIVATE_KEY_LOCATION` | `file:./secrets/jwt.rsa.priv` | |
| `FIREBASE_CREDENTIALS_LOCATION` | `file:./secrets/firebase-service-account.json` | |
| `SERVER_PORT` | `8080` | |
| `JPA_SHOW_SQL` | `false` | |
| `LOG_LEVEL_JPA`, `LOG_LEVEL_SECURITY`, `LOG_LEVEL_SQL`, `LOG_LEVEL_SQL_PARAMS` | `INFO` | |

Log levels default to `INFO` on purpose. **`LOG_LEVEL_SQL_PARAMS=TRACE` prints
bound SQL parameters, which includes password hashes and verification codes**.
Useful while debugging, never in a deployed environment.

## Database schema

Flyway owns the schema and applies it during startup, so nothing has to be run
by hand. Hibernate creates nothing and validates nothing (`ddl-auto: none`).

| Location | Contents | Applied |
|---|---|---|
| `src/main/resources/db/migration` | Schema: tables, indexes, views, the `haversine` function, the `pg_trgm` extension | Always |
| `src/main/resources/db/seed` | Demo dataset: 12 shops, 5 accounts, ~40 prices, votes and alerts | Only when `FLYWAY_LOCATIONS` includes it |

**Every seeded account has the password `demo1234`**, e.g.
`joao.silva@example.com`. `.env.example` enables the seed because a checkout
that starts empty is a checkout nobody can evaluate; **a deployed environment
must set `FLYWAY_LOCATIONS=classpath:db/migration` and leave the seed out.**

Migrations are immutable once applied: Flyway records a checksum and refuses to
start if a file it has already run has changed since. Corrections go in a new
`V2__...sql`, never as an edit to `V1`.

`spring.flyway.baseline-on-migrate` is on, so a database that predates Flyway
(one built by hand from the old `outros/banco.sql`) is stamped as already being
at `V1` and picks up `V2` onwards, rather than failing on a `CREATE TABLE` for a
table that is already there.

## Credential files

Three files are read at runtime and none of them belongs in git. They live in
`secrets/`, which is gitignored:

| File | Required | How to obtain |
|---|---|---|
| `secrets/jwt.rsa.priv` | yes | `openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt.rsa.priv` |
| `secrets/jwt.rsa.pub` | yes | `openssl rsa -pubout -in secrets/jwt.rsa.priv -out secrets/jwt.rsa.pub` |
| `secrets/firebase-service-account.json` | no | Firebase Console → Project settings → Service accounts → *Generate new private key* |

The JWT keypair is generated with the two commands above, so anyone can produce
one. The Firebase key cannot be generated. It is a server-side admin credential
that has to be downloaded by someone with access to the Firebase project, so it
is **optional**: without it the API starts, logs a warning, and runs with push
notifications disabled. Everything else works. `/notification/trigger-manual`
answers `503` in that state rather than claiming to have sent anything.

Replacing the JWT keypair invalidates every token already issued, so users are
signed out. Tokens expire after one hour regardless.
