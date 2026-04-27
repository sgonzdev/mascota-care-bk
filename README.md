# MascotaCare — Backend

Implementación de los **7 microservicios** + API Gateway + Eureka definidos en la propuesta arquitectónica del proyecto.

## Stack
- Java 21, Spring Boot 3.5.13, Maven (Wrapper incluido — **no necesitas Maven instalado**)
- PostgreSQL 16 (1 BD por servicio · doc §4.4)
- Redis 7 (cache de reglas + métricas · doc §4.5)
- Spring Cloud Gateway · Spring Cloud Eureka · Flyway · Lombok

## Servicios y puertos

| Servicio              | Puerto host | BD              | Notas |
|-----------------------|:-----------:|------------------|-------|
| eureka-server         | 8761        | —                | Service discovery |
| api-gateway           | 8080        | —                | Punto único de entrada |
| pet-service           | 8081        | pet_db (5433)    | UC1 — perfil de mascotas |
| symptom-service       | 8082        | symptom_db (5434)| UC2 — síntomas |
| rules-engine          | 8083        | rules_db + Redis (5435) | UC3/UC8 — motor de triage |
| guide-service         | 8084        | guide_db (5436)  | UC4 — guías + LLM |
| followup-service      | 8085        | followup_db (5437)| UC6 — seguimiento |
| metrics-service       | 8086        | Redis            | UC7 — KPIs en tiempo real |
| notification-service  | 8087        | notification_db (5438) | UC5 — push/email/SMS |

## Arrancar todo con Docker

```bash
cd backend
docker compose up --build       # primera vez, ~5-8 min
docker compose up               # siguientes veces
```

Con la opción de pgAdmin (acceso visual a las 6 BDs):
```bash
docker compose --profile tools up
# pgAdmin: http://localhost:5050  (admin@mascotacare.dev / admin)
```

## Verificar que todo está vivo

```bash
docker compose ps                         # estado de los contenedores
curl http://localhost:8761                # consola de Eureka (verás los servicios registrados)
curl http://localhost:8080/actuator/health
```

## Apagar / reiniciar

```bash
docker compose down                       # apaga
docker compose down -v                    # apaga y BORRA volúmenes (BDs)
docker compose restart pet-service        # reinicia uno solo
docker compose logs -f pet-service        # ver logs en vivo
```

## Estructura

```
backend/
├── docker-compose.yml          ← orquestación de 18 contenedores
├── Dockerfile                  ← multi-stage reutilizable (1 imagen para todos)
├── .env                        ← variables compartidas
├── scripts/
│   └── init-services.sh        ← regenera los 9 proyectos desde Spring Initializr
├── eureka-server/              ← Service Discovery
├── api-gateway/                ← Spring Cloud Gateway + Auth (módulo)
├── pet-service/                ← Microservicios de negocio (capas tradicionales)
├── symptom-service/            │   controller → service → repository → entity
├── rules-engine/               │
├── guide-service/              │
├── followup-service/           │
├── metrics-service/            │
└── notification-service/       │
```

## Próximos pasos

1. ✅ **Fase 1**: Estructura + Docker (este commit)
2. ⏳ **Fase 2**: Eureka + Gateway + Pet Service end-to-end con migración Flyway
3. ⏳ **Fase 3**: Symptom + Rules Engine (flujo de triage)
4. ⏳ **Fase 4**: Guide + Follow-up + Metrics + Notification
5. ⏳ **Fase 5**: Conectar el frontend Angular al Gateway
