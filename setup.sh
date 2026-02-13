#!/usr/bin/env bash
set -euo pipefail

APP_NAME="kalshi-market-stream"
APP_USER="kalshiapp"
APP_DIR="/opt/${APP_NAME}"
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"

# ---- Build/runtime config ----
# Option A: build from a local project folder already on the server
PROJECT_DIR="/opt/${APP_NAME}-src"

# Option B (optional): clone from git, then build
# If you use this, set GIT_REPO and optionally GIT_REF
GIT_REPO=""          # e.g. "https://github.com/you/kalshi-market-stream.git"
GIT_REF="main"       # e.g. "main" or a tag/commit

# Your jar name (as produced by your build)
JAR_NAME="kalshi-market-stream-0.0.1-SNAPSHOT.jar"
JAR_TARGET="${APP_DIR}/${JAR_NAME}"

# --- DB config (must match your application.properties) ---
DB_NAME="kalshi_db"
DB_USER="postgres"
DB_PASS="kamal"

echo "=== Update packages ==="
apt-get update -y

echo "=== Install Java 23 ==="
apt-get install -y wget gpg apt-transport-https ca-certificates lsb-release

wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public \
  | gpg --dearmor \
  | tee /etc/apt/trusted.gpg.d/adoptium.gpg >/dev/null

echo "deb https://packages.adoptium.net/artifactory/deb $(. /etc/os-release; echo "$VERSION_CODENAME") main" \
  | tee /etc/apt/sources.list.d/adoptium.list >/dev/null

apt-get update -y
apt-get install -y temurin-23-jdk

java -version

echo "=== Install build tools (Maven, Git) ==="
apt-get install -y maven git

echo "=== Install PostgreSQL ==="
apt-get install -y postgresql postgresql-contrib
systemctl enable postgresql
systemctl start postgresql

echo "=== Configure database ==="
sudo -u postgres psql -v ON_ERROR_STOP=1 -c "ALTER USER ${DB_USER} WITH PASSWORD '${DB_PASS}';"

if ! sudo -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'" | grep -q 1; then
  sudo -u postgres createdb -O "${DB_USER}" "${DB_NAME}"
  echo "Created database: ${DB_NAME}"
else
  echo "Database already exists: ${DB_NAME}"
fi

echo "=== Create app user ==="
if ! id -u "${APP_USER}" >/dev/null 2>&1; then
  useradd --system --home "${APP_DIR}" --shell /usr/sbin/nologin "${APP_USER}"
fi

mkdir -p "${APP_DIR}"

echo "=== Prepare source code ==="
if [[ -n "${GIT_REPO}" ]]; then
  echo "Using Git repo: ${GIT_REPO}"
  if [[ -d "${PROJECT_DIR}/.git" ]]; then
    echo "Repo already exists, pulling latest..."
    git -C "${PROJECT_DIR}" fetch --all --prune
    git -C "${PROJECT_DIR}" checkout "${GIT_REF}"
    git -C "${PROJECT_DIR}" pull
  else
    rm -rf "${PROJECT_DIR}"
    git clone "${GIT_REPO}" "${PROJECT_DIR}"
    git -C "${PROJECT_DIR}" checkout "${GIT_REF}"
  fi
else
  echo "Using existing project directory: ${PROJECT_DIR}"
  if [[ ! -f "${PROJECT_DIR}/pom.xml" ]]; then
    echo "❌ pom.xml not found in ${PROJECT_DIR}"
    echo "Set PROJECT_DIR to the folder containing pom.xml, or set GIT_REPO to clone."
    exit 1
  fi
fi

echo "=== Build jar at runtime (server) ==="
cd "${PROJECT_DIR}"

# Clean + build. Skip tests to speed up; remove -DskipTests if you want tests.
mvn -DskipTests clean package

# Find the built jar (avoid picking *original*.jar if you use spring-boot repackage)
JAR_BUILT_PATH="$(ls -1 target/*.jar | grep -v 'original-' | head -n 1 || true)"

if [[ -z "${JAR_BUILT_PATH}" || ! -f "${JAR_BUILT_PATH}" ]]; then
  echo "❌ Built jar not found in target/"
  echo "Contents of target/:"
  ls -la target/ || true
  exit 1
fi

echo "Built jar: ${JAR_BUILT_PATH}"

echo "=== Deploy jar to app directory ==="
cp "${JAR_BUILT_PATH}" "${JAR_TARGET}"
chown -R "${APP_USER}:${APP_USER}" "${APP_DIR}"
chmod 750 "${JAR_TARGET}"

echo "=== Create systemd service ==="
cat > "${SERVICE_FILE}" <<EOF
[Unit]
Description=${APP_NAME}
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=simple
User=${APP_USER}
WorkingDirectory=${APP_DIR}
ExecStart=/usr/bin/java -jar ${JAR_TARGET}
Restart=always
RestartSec=3

# Optional: keep logs in journal, and set some limits
# StandardOutput=journal
# StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable "${APP_NAME}"
systemctl restart "${APP_NAME}"

echo ""
echo "✅ Setup complete"
echo "Check logs: journalctl -u ${APP_NAME} -f"
