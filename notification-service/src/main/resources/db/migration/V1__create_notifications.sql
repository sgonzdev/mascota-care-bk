CREATE TABLE IF NOT EXISTS notifications (
    id              UUID PRIMARY KEY,
    destinatario    VARCHAR(200) NOT NULL,
    canal           VARCHAR(10)  NOT NULL,
    asunto          VARCHAR(200),
    contenido       TEXT         NOT NULL,
    estado          VARCHAR(10)  NOT NULL,
    intentos        INTEGER      NOT NULL DEFAULT 0,
    error_message   VARCHAR(500),
    creada_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    enviada_en      TIMESTAMPTZ
);

CREATE INDEX idx_notifications_estado ON notifications(estado, creada_en);
