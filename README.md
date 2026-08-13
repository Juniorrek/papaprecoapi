# papaprecoapi

The backend for **[PapaPreco](https://github.com/Juniorrek/papapreco)**, a
collaborative price-comparison app. A Java 17 / Spring Boot REST API that stores
crowd-sourced product prices, answers ranked searches by name, price range and
distance, parses Brazilian NFC-e receipts scraped from SEFAZ, and issues JWTs
for authentication.

## Requirements

- Docker and Docker Compose, or, to run without them, a JDK 17+ and a reachable
  PostgreSQL
- A JWT signing keypair in `secrets/` (two `openssl` commands, below)

## Setup

```bash
git clone https://github.com/Juniorrek/papaprecoapi.git
cd papaprecoapi
cp .env.example .env        # then fill in the real values
```

`.env.example` lists the seven variables you need to set: the database name,
URL, user and password; the Flyway locations; and the Gmail account and **App
Password** used to send verification mail. Everything else has a working default
See [docs/CONFIGURATION.md](docs/CONFIGURATION.md) for the full list.

Generate the JWT keypair, which is required and is not in git:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out secrets/jwt.rsa.priv
openssl rsa -pubout -in secrets/jwt.rsa.priv -out secrets/jwt.rsa.pub
```

Push notifications additionally need `secrets/firebase-service-account.json`,
which cannot be generated. It is downloaded by someone with access to the
Firebase project, and it is **optional**: without it the API starts, logs a warning,
and runs with notifications disabled. Everything else works.

## Running

```bash
docker compose up --build
```

It is
served under the `/papaprecoapi` context path:

```bash
curl http://localhost:8080/papaprecoapi/actuator/health
curl 'http://localhost:8080/papaprecoapi/produtos/ranking?palavra=cafe&latitude=-25.458162&longitude=-49.29&distancia=10&precoMin=0&precoMax=100'
```

### Without Docker

Convenient when running the API from an IDE against the database container:

```bash
docker compose up -d db     # or point DB_URL at any other PostgreSQL
set -a && . ./.env && set +a
./mvnw spring-boot:run
```

`DB_URL` in `.env` points at `localhost:5432`, which is where the database
container publishes, so this works with no further changes.

## The demo dataset

Flyway owns the schema and applies it at startup, so there is nothing to run by hand.
`.env.example` also enables a demo dataset (12 shops, 5 accounts, ~40 prices).

**Every seeded account has the password `demo1234`.** A deployed environment
must therefore set `FLYWAY_LOCATIONS=classpath:db/migration` and leave the seed
out.

## More

- [docs/CONFIGURATION.md](docs/CONFIGURATION.md): full environment variable
  reference, credential files, and how the schema and migrations are managed
