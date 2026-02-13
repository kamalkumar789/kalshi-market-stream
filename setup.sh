#!/usr/bin/env bash
set -euo pipefail

APP_NAME="kalshi-market-stream"
APP_USER="kalshiapp"
APP_DIR="/opt/${APP_NAME}"
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"

# Jar is inside the project's target/ folder (run this script from project root)
JAR_SOURCE="./target/kalshi-market-stream-0.0.1-SNAPSHOT.jar"
JAR_TARGET="${APP_DIR}/kalshi-market-stream-0.0.1-SNAPSHOT.jar"

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

echo "=== Install PostgreSQL ==="
apt-get install -y postgresql postgresql-contrib
systemctl enable postgresql
systemctl start postgresql

echo "=== Configure database ==="
# Set password for postgres user
sudo -u postgres psql -v ON_ERROR_STOP=1 -c "ALTER USER ${DB_USER} WITH PASSWORD '${DB_PASS}';"

# Create DB if not exists (CREATE DATABASE cannot run inside DO/transaction)
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

echo "=== Copy jar from project target/ ==="
if [[ ! -f "${JAR_SOURCE}" ]]; then
  echo "❌ Jar not found at: ${JAR_SOURCE}"
  echo "Run this script from the project root (where pom.xml exists) and ensure the jar is built."
  echo "Try: ls -la target/"
  exit 1
fi

cp "${JAR_SOURCE}" "${JAR_TARGET}"
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

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable "${APP_NAME}"
systemctl restart "${APP_NAME}"

echo ""
echo "✅ Setup complete"
echo "systemctl status ${APP_NAME}"
echo "journalctl -u ${APP_NAME} -f"