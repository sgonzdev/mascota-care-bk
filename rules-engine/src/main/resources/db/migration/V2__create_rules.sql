CREATE TABLE IF NOT EXISTS rules (
    id                          UUID PRIMARY KEY,
    condicion_sintoma           VARCHAR(200) NOT NULL,
    especie_aplica              VARCHAR(10)  NOT NULL,
    edad_min_meses              INTEGER      NOT NULL CHECK (edad_min_meses >= 0),
    edad_max_meses              INTEGER      NOT NULL CHECK (edad_max_meses >= edad_min_meses),
    nivel_urgencia_resultado    VARCHAR(10)  NOT NULL,
    accion_recomendada          TEXT         NOT NULL,
    prioridad                   INTEGER      NOT NULL CHECK (prioridad BETWEEN 1 AND 10),
    activa                      BOOLEAN      NOT NULL DEFAULT TRUE,
    creada_en                   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizada_en              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_rules_activa_prioridad ON rules(activa, prioridad);

INSERT INTO rules (id, condicion_sintoma, especie_aplica, edad_min_meses, edad_max_meses,
                   nivel_urgencia_resultado, accion_recomendada, prioridad, activa)
VALUES
  (gen_random_uuid(), 'diarrea_leve_sin_sangre', 'PERRO', 2, 240, 'BAJA',
   'Ayuno de 12h y dieta blanda. Mantener hidratación. Acudir al veterinario si persiste más de 24h.', 3, TRUE),
  (gen_random_uuid(), 'vomito_ocasional', 'TODAS', 0, 240, 'MEDIA',
   'Observar 24h, mantener hidratación. Consultar veterinario si vomita repetidamente.', 5, TRUE),
  (gen_random_uuid(), 'dificultad_respiratoria', 'TODAS', 0, 240, 'ALTA',
   'Derivación inmediata a clínica veterinaria. Es una emergencia.', 1, TRUE);
