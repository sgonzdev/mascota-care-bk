-- V3: catálogo extendido de reglas de triage.
-- Las prioridades menores ganan (1 = mayor urgencia).
-- ALTA: emergencias, MEDIA: requieren atención, BAJA: observación en casa.

INSERT INTO rules (id, condicion_sintoma, especie_aplica, edad_min_meses, edad_max_meses,
                   nivel_urgencia_resultado, accion_recomendada, prioridad, activa,
                   creada_en, actualizada_en)
VALUES
    -- ALTAS: emergencias absolutas
    (gen_random_uuid(), 'convulsiones', 'TODAS', 0, 360, 'ALTA',
     'Emergencia veterinaria inmediata. Acude a la clínica más cercana sin demora.',
     1, true, now(), now()),

    (gen_random_uuid(), 'sangrado_abundante_hemorragia', 'TODAS', 0, 360, 'ALTA',
     'Hemorragia activa. Comprime con paño limpio y acude a urgencias veterinarias YA.',
     1, true, now(), now()),

    (gen_random_uuid(), 'envenenamiento_intoxicacion', 'TODAS', 0, 360, 'ALTA',
     'Sospecha de intoxicación. NO induzcas el vómito. Lleva al veterinario inmediatamente '
     || 'con la muestra de lo ingerido.',
     1, true, now(), now()),

    (gen_random_uuid(), 'desmayo_perdida_conciencia', 'TODAS', 0, 360, 'ALTA',
     'Pérdida de consciencia. Manténlo abrigado y acude a urgencias veterinarias.',
     1, true, now(), now()),

    (gen_random_uuid(), 'diarrea_con_sangre', 'TODAS', 0, 360, 'ALTA',
     'Diarrea con sangre puede indicar parvovirus u otras causas graves. '
     || 'Aísla a la mascota y acude al veterinario hoy mismo.',
     2, true, now(), now()),

    (gen_random_uuid(), 'vomito_persistente_repetido', 'TODAS', 0, 360, 'ALTA',
     'Vómitos repetidos (más de 3 en 12h) pueden causar deshidratación severa. '
     || 'Acude a la clínica veterinaria hoy.',
     2, true, now(), now()),

    -- MEDIAS: requieren atención en 24-48h
    (gen_random_uuid(), 'fiebre_alta', 'TODAS', 0, 360, 'MEDIA',
     'Posible fiebre. Observa otras señales y consulta al veterinario en 24h. '
     || 'Mantén a la mascota en lugar fresco y con agua disponible.',
     4, true, now(), now()),

    (gen_random_uuid(), 'letargo_decaimiento', 'TODAS', 0, 360, 'MEDIA',
     'Decaimiento persistente. Vigila apetito, hidratación y comportamiento. '
     || 'Consulta veterinaria si dura más de 24h.',
     5, true, now(), now()),

    (gen_random_uuid(), 'perdida_apetito_anorexia', 'TODAS', 0, 360, 'MEDIA',
     'Falta de apetito de más de 24h. Ofrece agua y comida apetecible; consulta veterinaria '
     || 'si persiste 48h o aparecen otros síntomas.',
     5, true, now(), now()),

    (gen_random_uuid(), 'cojera_dificultad_caminar', 'TODAS', 0, 360, 'MEDIA',
     'Cojera o dificultad al caminar. Restringe ejercicio y consulta al veterinario '
     || 'para evaluación traumatológica.',
     5, true, now(), now()),

    (gen_random_uuid(), 'tos_persistente', 'TODAS', 0, 360, 'MEDIA',
     'Tos que dura más de 24h. Aisla a la mascota de otros animales y consulta veterinario '
     || '(puede ser tos de las perreras o problema cardíaco).',
     5, true, now(), now()),

    (gen_random_uuid(), 'estornudos_secrecion_nasal', 'TODAS', 0, 360, 'MEDIA',
     'Síntomas respiratorios. Mantén ambiente templado y consulta al veterinario '
     || 'si persisten más de 48h.',
     6, true, now(), now()),

    (gen_random_uuid(), 'ojos_rojos_lagrimeo', 'TODAS', 0, 360, 'MEDIA',
     'Conjuntivitis o irritación ocular. Limpia con suero fisiológico y consulta al '
     || 'veterinario si no mejora en 48h.',
     6, true, now(), now()),

    -- BAJAS: observación domiciliaria
    (gen_random_uuid(), 'rascado_picazon_leve', 'TODAS', 0, 360, 'BAJA',
     'Picor leve. Revisa la piel buscando pulgas, garrapatas o irritación. '
     || 'Si persiste más de 5 días, consulta al veterinario.',
     7, true, now(), now()),

    (gen_random_uuid(), 'caida_pelo_alopecia', 'TODAS', 0, 360, 'BAJA',
     'Caída de pelo localizada. Documenta con fotos la evolución y consulta veterinaria '
     || 'en visita rutinaria si se extiende.',
     7, true, now(), now()),

    (gen_random_uuid(), 'mal_aliento_halitosis', 'TODAS', 0, 360, 'BAJA',
     'Mal aliento puede indicar sarro o problemas digestivos. Agenda revisión dental '
     || 'en consulta rutinaria.',
     8, true, now(), now()),

    (gen_random_uuid(), 'sed_excesiva_polidipsia', 'TODAS', 0, 360, 'MEDIA',
     'Sed excesiva puede indicar diabetes o problemas renales. Mide cuánta agua bebe '
     || 'al día y agenda consulta veterinaria.',
     5, true, now(), now()),

    (gen_random_uuid(), 'orina_frecuente_cambio_color', 'TODAS', 0, 360, 'MEDIA',
     'Cambios urinarios. Recoge una muestra fresca y consulta veterinaria en 24-48h.',
     5, true, now(), now()),

    -- Reglas específicas por edad
    (gen_random_uuid(), 'cachorro_diarrea', 'TODAS', 0, 6, 'ALTA',
     'Cachorros con diarrea se deshidratan rápidamente. Consulta veterinaria HOY '
     || 'descartar parvovirus, giardia u otras causas.',
     2, true, now(), now()),

    (gen_random_uuid(), 'cachorro_no_come', 'TODAS', 0, 6, 'ALTA',
     'Cachorro sin apetito >12h es preocupante. Acude al veterinario hoy mismo.',
     2, true, now(), now()),

    (gen_random_uuid(), 'senior_letargo_perdida_peso', 'TODAS', 84, 360, 'MEDIA',
     'En mascotas mayores, decaimiento o pérdida de peso requiere evaluación '
     || 'veterinaria completa con análisis.',
     4, true, now(), now());
