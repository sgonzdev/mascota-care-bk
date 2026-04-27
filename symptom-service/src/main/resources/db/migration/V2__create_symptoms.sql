CREATE TABLE IF NOT EXISTS symptoms (
    id                      UUID PRIMARY KEY,
    id_mascota              UUID         NOT NULL,
    descripcion_libre       TEXT         NOT NULL,
    codigos_normalizados    VARCHAR(500) NOT NULL,
    severidad_percibida     VARCHAR(10),
    fecha_reporte           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_symptoms_mascota ON symptoms(id_mascota, fecha_reporte DESC);
