CREATE TABLE IF NOT EXISTS guides (
    id                  UUID PRIMARY KEY,
    id_mascota          UUID,
    tipo                VARCHAR(20)  NOT NULL,
    contenido_html      TEXT         NOT NULL,
    fuente              VARCHAR(10)  NOT NULL,
    fecha_generacion    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_guides_mascota ON guides(id_mascota, fecha_generacion DESC);
