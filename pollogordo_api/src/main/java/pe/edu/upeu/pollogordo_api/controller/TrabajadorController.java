package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.pollogordo_api.dto.TrabajadorDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.ITrabajadorService;

import java.util.List;

@RestController
@RequestMapping("/trabajadores")
public class TrabajadorController {

    private final ITrabajadorService trabajadorService;

    public TrabajadorController(ITrabajadorService trabajadorService) {
        this.trabajadorService = trabajadorService;
    }

    @GetMapping
    public ResponseEntity<List<TrabajadorDTO>> listarTodo() {
        return ResponseEntity.ok(trabajadorService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrabajadorDTO> buscarPorId(@PathVariable Long id) {
        return trabajadorService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TrabajadorDTO> crear(
            @Valid @RequestBody TrabajadorDTO dto) {
        return ResponseEntity.ok(trabajadorService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrabajadorDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TrabajadorDTO dto) {

        return trabajadorService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        if (trabajadorService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id);
    }
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TrabajadorDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {

        return trabajadorService.cambiarEstado(id, estado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}