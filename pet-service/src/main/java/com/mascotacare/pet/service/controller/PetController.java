package com.mascotacare.pet.service.controller;

import com.mascotacare.pet.service.dto.PetRequest;
import com.mascotacare.pet.service.dto.PetResponse;
import com.mascotacare.pet.service.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Mascotas (UC1)", description = "CRUD del perfil de mascotas: especie, raza, edad, peso, sexo. "
        + "Validaciones de negocio en PetValidator (rangos por especie, coherencia edad/peso).")
public class PetController {

    private final PetService service;

    @PostMapping
    @Operation(summary = "Registrar una nueva mascota",
            description = "Crea el perfil con validación: nombre 2-80 chars, edad 0-360 meses, peso 0.1-200 kg. "
                    + "Devuelve la entidad con id generado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mascota creada"),
            @ApiResponse(responseCode = "400", description = "Validación fallida (Bean Validation o PetValidator)"),
            @ApiResponse(responseCode = "401", description = "JWT ausente o inválido")
    })
    public ResponseEntity<PetResponse> create(@Valid @RequestBody PetRequest req) {
        PetResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/pets/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar mascotas",
            description = "Sin parámetros devuelve todas. Con `idUsuario` filtra por dueño.")
    @ApiResponse(responseCode = "200", description = "Lista (puede ser vacía)")
    public List<PetResponse> list(
            @Parameter(description = "Filtrar por id del dueño (opcional)")
            @RequestParam(required = false) UUID idUsuario) {
        return idUsuario == null ? service.listAll() : service.listByUser(idUsuario);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una mascota por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mascota encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe la mascota"),
            @ApiResponse(responseCode = "400", description = "id no es UUID válido")
    })
    public PetResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar mascota existente",
            description = "Sustituye todos los campos. Mismas validaciones que el POST.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mascota actualizada"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "400", description = "Validación fallida")
    })
    public PetResponse update(@PathVariable UUID id, @Valid @RequestBody PetRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar mascota")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
