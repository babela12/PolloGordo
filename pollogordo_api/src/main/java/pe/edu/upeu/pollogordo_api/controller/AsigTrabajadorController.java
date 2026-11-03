
package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.pollogordo_api.dto.AsigTrabajadorDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.IAsigTrabajadorService;

import java.util.List;
import java.util.Optional;

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

        List<AsigTrabajadorDTO> lista = asigTrabajadorService.listarTodo();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsigTrabajadorDTO> buscarPorId(
            @PathVariable Long id) {

        Optional<AsigTrabajadorDTO> resultado =
                asigTrabajadorService.buscarPorId(id);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<AsigTrabajadorDTO> crear(
            @Valid @RequestBody AsigTrabajadorDTO dto) {

        AsigTrabajadorDTO asignacion = asigTrabajadorService.crear(dto);

        return ResponseEntity.ok(asignacion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AsigTrabajadorDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AsigTrabajadorDTO dto) {

        Optional<AsigTrabajadorDTO> resultado =
                asigTrabajadorService.actualizar(id, dto);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        boolean eliminado = asigTrabajadorService.eliminar(id);

        if (eliminado) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id
        );
    }
}
