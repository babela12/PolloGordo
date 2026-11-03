
package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import pe.edu.upeu.pollogordo_api.dto.InsumoDTO;
import pe.edu.upeu.pollogordo_api.services.IFileStorageService;
import pe.edu.upeu.pollogordo_api.services.IInsumoService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        List<InsumoDTO> lista = insumoService.listarTodo();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsumoDTO> buscarPorId(@PathVariable Long id) {

        Optional<InsumoDTO> insumo = insumoService.buscarPorId(id);

        if (insumo.isPresent()) {
            return ResponseEntity.ok(insumo.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InsumoDTO> crear(
            @Valid @ModelAttribute InsumoDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        String imagen = null;

        if (file != null && !file.isEmpty()) {
            imagen = storageService.store(file);
        }

        InsumoDTO insumo = new InsumoDTO(
                null,
                dto.nombre(),
                dto.unidad(),
                dto.perecible(),
                dto.controlaStock(),
                dto.cantidadStock(),
                dto.estado(),
                imagen
        );

        InsumoDTO nuevo = insumoService.crear(insumo);

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<InsumoDTO> actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute InsumoDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Optional<InsumoDTO> resultado = insumoService.buscarPorId(id);

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        InsumoDTO actual = resultado.get();

        String imagen = actual.imagen();

        if (file != null && !file.isEmpty()) {
            imagen = storageService.store(file);
        }

        InsumoDTO insumo = new InsumoDTO(
                id,
                dto.nombre(),
                dto.unidad(),
                dto.perecible(),
                dto.controlaStock(),
                dto.cantidadStock(),
                dto.estado(),
                imagen
        );

        Optional<InsumoDTO> actualizado =
                insumoService.actualizar(id, insumo);

        if (actualizado.isPresent()) {
            return ResponseEntity.ok(actualizado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        boolean eliminado = insumoService.eliminar(id);

        if (eliminado) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(
            IllegalArgumentException ex) {

        return ResponseEntity
                .badRequest()
                .body(Map.of("mensaje", ex.getMessage()));
    }
}
