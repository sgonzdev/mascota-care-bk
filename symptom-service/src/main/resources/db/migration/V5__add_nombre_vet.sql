-- V5: Guardar el nombre del veterinario asignado para mostrarlo sin
-- llamar al auth-service (RF22). Snapshot del nombre en el momento del claim.
ALTER TABLE consultas ADD COLUMN nombre_vet_asignado VARCHAR(80) NULL;
