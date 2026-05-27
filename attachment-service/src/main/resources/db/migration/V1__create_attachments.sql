-- Schema inicial de adjuntos de caso clínico.
-- El binario vive en MinIO/S3; aquí solo guardamos metadata + s3_key.

CREATE TABLE IF NOT EXISTS attachments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_consulta     UUID NOT NULL,
    id_usuario      UUID NOT NULL,                   -- quién subió
    nombre_archivo  VARCHAR(255) NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    size_bytes      BIGINT NOT NULL,
    s3_key          VARCHAR(500) NOT NULL UNIQUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_attachments_consulta ON attachments(id_consulta);
CREATE INDEX idx_attachments_usuario  ON attachments(id_usuario);
