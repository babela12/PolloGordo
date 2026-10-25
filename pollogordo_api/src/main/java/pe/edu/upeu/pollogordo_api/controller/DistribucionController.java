package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.pollogordo_api.dto.DistribucionDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.IDistribucionService;

import java.util.List;

@RestController
@RequestMapping("/distribuciones")
public class DistribucionController {

    private final IDistribucionService distribucionService;

    public DistribucionController(IDistribucionService distribucionService) {
        this.distribucionService = distribucionService;
    }

    @GetMapping
    public ResponseEntity<List<DistribucionDTO>> listarTodo() {
        return ResponseEntity.ok(distribucionService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DistribucionDTO> buscarPorId(@PathVariable Long id) {
        return distribucionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DistribucionDTO> crear(
            @Valid @RequestBody DistribucionDTO dto) {
        return ResponseEntity.ok(distribucionService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DistribucionDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DistribucionDTO dto) {

        return distribucionService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        if (distribucionService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id);
    }
}