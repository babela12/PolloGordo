
package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import pe.edu.upeu.pollogordo_api.dto.PagoDTO;
import pe.edu.upeu.pollogordo_api.dto.ResumenPagoDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.IPagoService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final IPagoService pagoService;

    public PagoController(IPagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public ResponseEntity<List<PagoDTO>> listarTodo() {
        return ResponseEntity.ok(pagoService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoDTO> buscarPorId(@PathVariable Long id) {
        return pagoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<List<PagoDTO>> listarPorPedido(
            @PathVariable Long idPedido) {

        return ResponseEntity.ok(
                pagoService.listarPorPedido(idPedido)
        );
    }

    @GetMapping("/pedido/{idPedido}/resumen")
    public ResponseEntity<ResumenPagoDTO> obtenerResumen(
            @PathVariable Long idPedido) {

        return ResponseEntity.ok(
                pagoService.obtenerResumen(idPedido)
        );
    }

    @PostMapping
    public ResponseEntity<PagoDTO> crear(
            @Valid @RequestBody PagoDTO dto) {

        return ResponseEntity.ok(pagoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PagoDTO dto) {

        return pagoService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        if (pagoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se encontró el pago ID: " + id
        );
    }

    @PostMapping(
            value = "/{id}/evidencia",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<PagoDTO> subirEvidencia(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo)
            throws IOException {

        return ResponseEntity.ok(
                pagoService.subirEvidencia(id, archivo)
        );
    }

    @GetMapping("/{id}/evidencia")
    public ResponseEntity<Resource> obtenerEvidencia(
            @PathVariable Long id) throws IOException {

        Path archivo = pagoService.obtenerEvidencia(id);

        String tipo = Files.probeContentType(archivo);

        if (tipo == null) {
            tipo = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        Resource recurso = new FileSystemResource(archivo);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(tipo))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                archivo.getFileName() + "\""
                )
                .header("X-Content-Type-Options", "nosniff")
                .body(recurso);
    }
}
