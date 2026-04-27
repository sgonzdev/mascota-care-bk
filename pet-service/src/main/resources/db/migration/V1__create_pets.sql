CREATE TABLE IF NOT EXISTS pets (
    id              UUID PRIMARY KEY,
    id_usuario      UUID         NOT NULL,
    nombre          VARCHAR(80)  NOT NULL,
    especie         VARCHAR(10)  NOT NULL,
    raza            VARCHAR(80)  NOT NULL,
    edad_meses      INTEGER      NOT NULL CHECK (edad_meses >= 0 AND edad_meses <= 360),
    peso_kg         DOUBLE PRECISION NOT NULL CHECK (peso_kg > 0),
    sexo            VARCHAR(10)  NOT NULL,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_pets_usuario ON pets(id_usuario);
