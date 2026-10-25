package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.pollogordo_api.dto.AsigTrabajadorDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.IAsigTrabajadorService;

import java.util.List;

@RestController
@RequestMapping("/asignaciones")
public class AsigTrabajadorController {

    private final IAsigTrabajadorService asigTrabajadorService;

    public AsigTrabajadorController(
            IAsigTrabajadorService asigTrabajadorService) {
        this.asigTrabajadorService = asigTrabajadorService;
    }

    @GetMapping
    public ResponseEntity<List<AsigTrabajadorDTO>> listarTodo() {
        return ResponseEntity.ok(asigTrabajadorService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsigTrabajadorDTO> buscarPorId(
            @PathVariable Long id) {

        return asigTrabajadorService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AsigTrabajadorDTO> crear(
            @Valid @RequestBody AsigTrabajadorDTO dto) {

        return ResponseEntity.ok(asigTrabajadorService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AsigTrabajadorDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AsigTrabajadorDTO dto) {

        return asigTrabajadorService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        if (asigTrabajadorService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id);
    }
}