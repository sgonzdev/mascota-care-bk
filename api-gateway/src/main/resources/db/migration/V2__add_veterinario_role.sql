-- V2: Soporte para rol VETERINARIO (RF22).
-- Amplía la columna `rol` y agrega un usuario seed para pruebas.

ALTER TABLE users ALTER COLUMN rol TYPE VARCHAR(20);

-- Seed: vet@mascotacare.dev / vet123 (BCrypt cost=10)
INSERT INTO users (id, nombre, email, telefono, password_hash, rol)
VALUES (
    gen_random_uuid(),
    'Vet Demo',
    'vet@mascotacare.dev',
    '+34 600 000 003',
    '$2a$10$U8t8svjhBbSKUR6C59oH8.Y6W..MTZ2PMzxpVRugUqkYHWESjWdIC',
    'VETERINARIO'
)
ON CONFLICT (email) DO NOTHING;
