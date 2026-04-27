CREATE TABLE IF NOT EXISTS followups (
    id                  UUID PRIMARY KEY,
    id_consulta         UUID         NOT NULL,
    id_mascota          UUID         NOT NULL,
    estado              VARCHAR(12)  NOT NULL,
    observaciones       TEXT,
    alerta_enviada      BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_seguimiento   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_followups_mascota ON followups(id_mascota, fecha_seguimiento DESC);
CREATE INDEX idx_followups_pending ON followups(estado, alerta_enviada, fecha_seguimiento) WHERE alerta_enviada = FALSE;
