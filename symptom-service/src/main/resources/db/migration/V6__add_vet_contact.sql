-- V6: Snapshot del email y teléfono del vet asignado para que el dueño pueda
-- contactarlo sin acoplar este servicio al auth-service.
ALTER TABLE consultas
    ADD COLUMN email_vet_asignado    VARCHAR(200) NULL,
    ADD COLUMN telefono_vet_asignado VARCHAR(20)  NULL;
