#!/usr/bin/env bash
# Genera los 9 proyectos Spring Boot usando la API de Spring Initializr.
# Ejecutar desde la raíz del backend: bash scripts/init-services.sh

set -euo pipefail

INITIALIZR="https://start.spring.io/starter.zip"
BOOT_VERSION="3.5.13"
JAVA_VERSION="21"
GROUP="com.mascotacare"
TYPE="maven-project"
LANG="java"
PACKAGING="jar"

# Dependencias base que comparten los servicios de negocio
BASE_BIZ="web,data-jpa,postgresql,validation,lombok,flyway,actuator,cloud-eureka"
# Servicios con cache Redis
WITH_REDIS="${BASE_BIZ},data-redis"

# microservicio | dependencias | puerto
declare -A SERVICES=(
  [eureka-server]="cloud-eureka-server,actuator,lombok|8761"
  [api-gateway]="cloud-gateway-reactive,cloud-eureka,actuator,lombok|8080"
  [pet-service]="${BASE_BIZ}|8081"
  [symptom-service]="${BASE_BIZ}|8082"
  [rules-engine]="${WITH_REDIS}|8083"
  [guide-service]="${BASE_BIZ},webflux|8084"
  [followup-service]="${BASE_BIZ}|8085"
  [metrics-service]="${WITH_REDIS}|8086"
  [notification-service]="web,validation,lombok,actuator,cloud-eureka,mail|8087"
)

for svc in "${!SERVICES[@]}"; do
  IFS='|' read -r deps port <<< "${SERVICES[$svc]}"
  pkg=$(echo "$svc" | tr '-' '.')
  artifact="$svc"

  if [ -f "$svc/pom.xml" ]; then
    echo "▸ $svc ya existe, salto"
    continue
  fi

  echo "▸ Generando $svc (puerto $port)"
  curl -fsSL -o "/tmp/${svc}.zip" "$INITIALIZR" \
    --data-urlencode "type=${TYPE}" \
    --data-urlencode "language=${LANG}" \
    --data-urlencode "bootVersion=${BOOT_VERSION}" \
    --data-urlencode "javaVersion=${JAVA_VERSION}" \
    --data-urlencode "packaging=${PACKAGING}" \
    --data-urlencode "groupId=${GROUP}" \
    --data-urlencode "artifactId=${artifact}" \
    --data-urlencode "name=${artifact}" \
    --data-urlencode "packageName=${GROUP}.${pkg}" \
    --data-urlencode "dependencies=${deps}"

  unzip -q -o "/tmp/${svc}.zip" -d "$svc"
  rm "/tmp/${svc}.zip"
done

echo "✅ 9 proyectos generados"
