
package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.pollogordo_api.dto.TrabajadorDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.ITrabajadorService;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/trabajadores")
public class TrabajadorController {

    private final ITrabajadorService trabajadorService;

    public TrabajadorController(ITrabajadorService trabajadorService) {
        this.trabajadorService = trabajadorService;
    }

    @GetMapping
    public ResponseEntity<List<TrabajadorDTO>> listarTodo() {

        List<TrabajadorDTO> lista = trabajadorService.listarTodo();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrabajadorDTO> buscarPorId(
            @PathVariable Long id) {

        Optional<TrabajadorDTO> resultado =
                trabajadorService.buscarPorId(id);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<TrabajadorDTO> crear(
            @Valid @RequestBody TrabajadorDTO dto) {

        TrabajadorDTO nuevo = trabajadorService.crear(dto);

        return ResponseEntity.ok(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrabajadorDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TrabajadorDTO dto) {

        Optional<TrabajadorDTO> resultado =
                trabajadorService.actualizar(id, dto);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        boolean eliminado = trabajadorService.eliminar(id);

        if (eliminado) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<TrabajadorDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {

        Optional<TrabajadorDTO> resultado =
                trabajadorService.cambiarEstado(id, estado);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
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
