package com.mascotacare.attachment.service.controller;

import com.mascotacare.attachment.service.dto.AttachmentResponse;
import com.mascotacare.attachment.service.entity.Attachment;
import com.mascotacare.attachment.service.security.UserContext;
import com.mascotacare.attachment.service.service.AttachmentService;
import com.mascotacare.attachment.service.service.S3Storage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Tag(name = "Adjuntos de caso",
        description = "Documentos (PDF, Word), imágenes y otros archivos asociados a una consulta. "
                + "Persistidos en MinIO/S3; aquí solo metadata. Permisos: el uploader o admin pueden borrar.")
public class AttachmentController {

    private final AttachmentService service;
    private final S3Storage storage;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir un archivo a una consulta")
    public ResponseEntity<AttachmentResponse> upload(
            @RequestParam UUID idConsulta,
            @RequestParam("file") MultipartFile file) throws IOException {
        UUID user = currentUserId();
        AttachmentResponse out = service.upload(idConsulta, user, file);
        return ResponseEntity.created(URI.create("/api/attachments/" + out.id())).body(out);
    }

    @GetMapping("/by-consultation/{idConsulta}")
    @Operation(summary = "Listar adjuntos de una consulta (ordenados por fecha desc)")
    public List<AttachmentResponse> listByConsulta(@PathVariable UUID idConsulta) {
        return service.listByConsulta(idConsulta);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Descargar el binario de un adjunto",
            description = "Devuelve el archivo con su mime original. Para imágenes y PDFs el "
                    + "navegador puede mostrarlos inline (Content-Disposition: inline).")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID id) {
        Attachment a = service.findEntity(id);
        ResponseInputStream<GetObjectResponse> body = storage.download(a.getS3Key());
        String filename = URLEncoder.encode(a.getNombreArchivo(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        boolean inline = a.getMimeType().startsWith("image/")
                || a.getMimeType().equals("application/pdf");
        String disposition = (inline ? "inline" : "attachment")
                + "; filename=\"" + a.getNombreArchivo() + "\""
                + "; filename*=UTF-8''" + filename;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(MediaType.parseMediaType(a.getMimeType()))
                .contentLength(a.getSizeBytes())
                .body(new InputStreamResource(body));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un adjunto (solo uploader o admin)")
    public void delete(@PathVariable UUID id) {
        UserContext.User u = UserContext.get();
        try {
            service.delete(id, currentUserId(),
                    u != null && "ADMIN".equalsIgnoreCase(u.role()));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    private UUID currentUserId() {
        UserContext.User u = UserContext.get();
        if (u == null || u.id() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        try { return UUID.fromString(u.id()); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"); }
    }
}
