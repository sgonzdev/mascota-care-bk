-- V3: Ampliar la columna `tipo` para soportar los nuevos enums largos
-- (ANSIEDAD_SEPARACION, EMERGENCIAS_HOGAR, PRIMEROS_AUXILIOS, etc).
ALTER TABLE guides ALTER COLUMN tipo TYPE VARCHAR(32);
