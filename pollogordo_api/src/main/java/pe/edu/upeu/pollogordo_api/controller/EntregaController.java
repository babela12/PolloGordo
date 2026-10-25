package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.pollogordo_api.dto.EntregaDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.IEntregaService;

import java.util.List;

@RestController
@RequestMapping("/entregas")
public class EntregaController {

    private final IEntregaService entregaService;

    public EntregaController(IEntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping
    public ResponseEntity<List<EntregaDTO>> listarTodo() {
        return ResponseEntity.ok(entregaService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntregaDTO> buscarPorId(@PathVariable Long id) {
        return entregaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EntregaDTO> crear(
            @Valid @RequestBody EntregaDTO dto) {
        return ResponseEntity.ok(entregaService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntregaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EntregaDTO dto) {

        return entregaService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        if (entregaService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id);
    }
}