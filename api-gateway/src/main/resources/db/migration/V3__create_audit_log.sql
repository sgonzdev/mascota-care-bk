-- RF26 — Auditoría persistida. Traza cada acción crítica (login, claim,
-- mutaciones de reglas, anuncios, archivado, etc.) en una tabla consultable
-- por admin. Indexamos por (usuario, fecha) y (acción) para listados rápidos.

CREATE TABLE IF NOT EXISTS audit_log (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario   UUID NULL,                   -- null si la acción es del sistema
    email        VARCHAR(200) NULL,
    rol          VARCHAR(20)  NULL,
    accion       VARCHAR(80)  NOT NULL,        -- p.ej. AUTH_LOGIN, RULE_TOGGLE
    metodo       VARCHAR(10)  NULL,            -- GET / POST / PATCH ...
    path         VARCHAR(500) NULL,            -- ruta HTTP solicitada
    status_code  INTEGER      NULL,            -- código HTTP devuelto
    ip           VARCHAR(45)  NULL,
    user_agent   VARCHAR(300) NULL,
    detalle      TEXT         NULL,            -- payload mínimo o mensaje libre
    creado_en    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_usuario_fecha ON audit_log(id_usuario, creado_en DESC);
CREATE INDEX idx_audit_accion        ON audit_log(accion, creado_en DESC);
CREATE INDEX idx_audit_fecha         ON audit_log(creado_en DESC);
