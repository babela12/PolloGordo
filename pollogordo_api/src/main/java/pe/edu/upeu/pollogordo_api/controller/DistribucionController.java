
package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.pollogordo_api.dto.DistribucionDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.IDistribucionService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/distribuciones")
public class DistribucionController {

    private final IDistribucionService distribucionService;

    public DistribucionController(IDistribucionService distribucionService) {
        this.distribucionService = distribucionService;
    }

    @GetMapping
    public ResponseEntity<List<DistribucionDTO>> listarTodo() {

        List<DistribucionDTO> lista = distribucionService.listarTodo();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DistribucionDTO> buscarPorId(@PathVariable Long id) {

        Optional<DistribucionDTO> resultado =
                distribucionService.buscarPorId(id);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<DistribucionDTO> crear(
            @Valid @RequestBody DistribucionDTO dto) {

        DistribucionDTO distribucion = distribucionService.crear(dto);

        return ResponseEntity.ok(distribucion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DistribucionDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DistribucionDTO dto) {

        Optional<DistribucionDTO> resultado =
                distribucionService.actualizar(id, dto);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        boolean eliminado = distribucionService.eliminar(id);

        if (eliminado) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id
        );
    }
}
