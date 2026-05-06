-- Consultas: agregación persistida por cada triage UC2+UC3.
-- El frontend lista/edita estas filas a través de GET/PATCH /api/consultations.
CREATE TABLE IF NOT EXISTS consultas (
    id                    UUID PRIMARY KEY,
    id_mascota            UUID         NOT NULL,
    id_usuario            UUID         NOT NULL,
    fecha_hora            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    descripcion_sintomas  TEXT         NOT NULL,
    nivel_urgencia        VARCHAR(10)  NOT NULL,
    respuesta_generada    TEXT         NOT NULL,
    id_regla_aplicada     UUID,
    canal                 VARCHAR(10)  NOT NULL DEFAULT 'web',
    estado                VARCHAR(15)  NOT NULL DEFAULT 'activa',
    notas_internas        TEXT         NOT NULL DEFAULT '',
    actualizada_en        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_consultas_usuario ON consultas(id_usuario, fecha_hora DESC);
CREATE INDEX idx_consultas_mascota ON consultas(id_mascota, fecha_hora DESC);
CREATE INDEX idx_consultas_estado  ON consultas(estado);
