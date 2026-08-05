# syntax=docker/dockerfile:1

# Build and runtime are separate stages so the JDK, the Maven repository and the
# source tree never reach the published image — only a JRE and the artifact do.
#
# The Phase 1 instance is x86_64, the same architecture Intel and AMD desktops
# run, so a plain `docker build` on a development machine produces the image that
# runs on the server. No --platform flag and no cross-building.
#
# Both base images are multi-arch, so that stays true if the target ever moves to
# Graviton — the build would then need `--platform linux/arm64` explicitly, and
# an image built without it would fail to start with an exec format error.

# ---------------------------------------------------------------------------
# Stage 1 — build
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk AS build

WORKDIR /build

# Dependencies resolve from the POM alone, in their own layer. A change under
# src/ then rebuilds without re-resolving the dependency tree.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q dependency:go-offline

COPY src/ src/

# Tests are skipped here on purpose: there are none yet, and the ones planned in
# Phase 2 use Testcontainers, which needs a Docker daemon the build stage has no
# business talking to. CI runs them, not the image build.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q -DskipTests package

# The artifact carries its version in the filename; copy it to a stable one so
# the runtime stage does not have to know the version.
RUN cp target/papaprecoapi-*.war /build/app.war

# ---------------------------------------------------------------------------
# Stage 2 — runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre

# Unprivileged, at uid 1000. The secrets below are bind-mounted from the host
# with 0600 permissions, so the container user has to *be* their owner — and the
# first non-root account is 1000 on essentially every host this will run on
# (developer machines, ec2-user, ubuntu). Any other uid means chowning the keys
# on the host, which then locks the host user out of its own files.
#
# The base image parks a placeholder 'ubuntu' account on 1000; it has no purpose
# here and is removed to free the id. Override for a host that numbers
# differently: --build-arg APP_UID=1001
ARG APP_UID=1000
ARG APP_GID=1000
RUN userdel --remove ubuntu 2>/dev/null || true; \
    groupadd --gid ${APP_GID} app \
 && useradd --uid ${APP_UID} --gid app --no-create-home --shell /usr/sbin/nologin app

WORKDIR /app

COPY --from=build --chown=app:app /build/app.war app.war

# The three credential files are mounted at runtime, never copied in — baking
# them into a layer would put them in every registry that holds the image, which
# is what the Phase 0 rotation existed to undo.
#
# application.yaml resolves them from ./secrets by default, relative to this
# WORKDIR, so mounting the host directory at /app/secrets needs no further
# configuration:
#
#   docker run -v /opt/papapreco/secrets:/app/secrets:ro ...
#
# Override JWT_PUBLIC_KEY_LOCATION, JWT_PRIVATE_KEY_LOCATION and
# FIREBASE_CREDENTIALS_LOCATION to mount them elsewhere.

USER app

EXPOSE 8080

# MaxRAMPercentage rather than a fixed -Xmx, so the heap follows the container's
# memory limit instead of the host's total memory. Without it the JVM sizes
# itself against the whole machine and gets OOM-killed next to PostgreSQL on a
# 2 GB instance.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Boot is slow enough — Spring context, JPA, firebase-admin — that a short
# start-period would restart a container that was merely still starting.
# /actuator/health reports DOWN while the database is unreachable, so this is a
# readiness signal and not just a liveness one.
HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
    CMD curl -fsS "http://localhost:${SERVER_PORT:-8080}/papaprecoapi/actuator/health" || exit 1

# exec so the JVM is PID 1 and receives SIGTERM directly; without it the shell
# holds PID 1 and shutdown waits for the 10s kill timeout on every deploy.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.war"]
