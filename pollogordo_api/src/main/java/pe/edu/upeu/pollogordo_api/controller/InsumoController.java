
package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.pollogordo_api.dto.InsumoDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.IFileStorageService;
import pe.edu.upeu.pollogordo_api.services.IInsumoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/insumos")
public class InsumoController {

    private final IInsumoService insumoService;
    private final IFileStorageService storageService;

    public InsumoController(
            IInsumoService insumoService,
            IFileStorageService storageService) {
        this.insumoService = insumoService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<List<InsumoDTO>> listarTodo() {
        return ResponseEntity.ok(insumoService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsumoDTO> buscarPorId(
            @PathVariable Long id) {

        return insumoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREAR INSUMO CON IMAGEN
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<InsumoDTO> crear(
            @ModelAttribute
            @org.springdoc.core.annotations.ParameterObject
            @Valid InsumoDTO dto,

            @RequestPart(value = "file", required = false)
            MultipartFile file) {

        String nombreImagen = null;

        if (file != null && !file.isEmpty()) {
            nombreImagen = storageService.store(file);
        }

        InsumoDTO dtoParaCrear = new InsumoDTO(
                null,
                dto.nombre(),
                dto.unidad(),
                dto.perecible(),
                dto.controlaStock(),
                dto.cantidadStock(),
                dto.estado(),
                nombreImagen
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(insumoService.crear(dtoParaCrear));
    }

    // ACTUALIZAR INSUMO CON IMAGEN
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<InsumoDTO> actualizar(
            @PathVariable Long id,

            @ModelAttribute
            @org.springdoc.core.annotations.ParameterObject
            @Valid InsumoDTO dto,

            @RequestPart(value = "file", required = false)
            MultipartFile file) {

        InsumoDTO insumoActual = insumoService.buscarPorId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Insumo no encontrado"));

        String nombreImagen = insumoActual.imagen();

        if (file != null && !file.isEmpty()) {
            nombreImagen = storageService.store(file);
        }

        InsumoDTO dtoParaActualizar = new InsumoDTO(
                id,
                dto.nombre(),
                dto.unidad(),
                dto.perecible(),
                dto.controlaStock(),
                dto.cantidadStock(),
                dto.estado(),
                nombreImagen
        );

        return insumoService.actualizar(id, dtoParaActualizar)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        if (insumoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(
            IllegalArgumentException ex) {

        return ResponseEntity
                .badRequest()
                .body(Map.of("mensaje", ex.getMessage()));
    }
}
