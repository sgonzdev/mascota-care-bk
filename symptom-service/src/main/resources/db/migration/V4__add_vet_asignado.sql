-- V4: Asignación exclusiva de un caso a un veterinario (RF22).
ALTER TABLE consultas
    ADD COLUMN id_vet_asignado UUID NULL,
    ADD COLUMN asignada_en     TIMESTAMP WITH TIME ZONE NULL;

CREATE INDEX idx_consultas_vet ON consultas (id_vet_asignado);
