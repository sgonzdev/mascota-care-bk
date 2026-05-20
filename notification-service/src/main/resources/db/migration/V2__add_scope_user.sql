-- V2: distinción entre notificaciones GLOBALES (para todos) y PERSONALES (a un usuario).
ALTER TABLE notifications
    ADD COLUMN scope VARCHAR(10) NOT NULL DEFAULT 'PERSONAL',
    ADD COLUMN id_usuario UUID NULL;

CREATE INDEX idx_notifications_scope_user
    ON notifications (scope, id_usuario);
