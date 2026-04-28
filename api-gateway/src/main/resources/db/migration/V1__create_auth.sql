CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY,
    nombre          VARCHAR(80)  NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    telefono        VARCHAR(20),
    password_hash   VARCHAR(100) NOT NULL,
    rol             VARCHAR(10)  NOT NULL,
    fecha_registro  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(100) NOT NULL UNIQUE,
    expira_en   TIMESTAMPTZ  NOT NULL,
    revocado    BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_hash ON refresh_tokens(token_hash) WHERE revocado = FALSE;

-- Seed: admin / admin123 y dueno / dueno123 (BCrypt cost=10)
INSERT INTO users (id, nombre, email, telefono, password_hash, rol) VALUES
  (gen_random_uuid(), 'Admin Demo', 'admin@mascotacare.dev', '+34 600 000 001',
   '$2a$10$25PULQtR0LDYxnydDcX1Quw8tsg3hy28rargAJZmOEVgoSyi8hMDW', 'ADMIN'),
  (gen_random_uuid(), 'Dueño Demo', 'dueno@mascotacare.dev', '+34 600 000 002',
   '$2a$10$qw/NrzSaKT.CbXWzTxRhQunb4pgkojlRR7VnIiQhSPBFBrnpmT6.O', 'DUENO');
