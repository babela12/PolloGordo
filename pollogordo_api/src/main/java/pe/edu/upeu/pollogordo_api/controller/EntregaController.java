
package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.pollogordo_api.dto.EntregaDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.IEntregaService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/entregas")
public class EntregaController {

    private final IEntregaService entregaService;

    public EntregaController(IEntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping
    public ResponseEntity<List<EntregaDTO>> listarTodo() {

        List<EntregaDTO> lista = entregaService.listarTodo();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntregaDTO> buscarPorId(@PathVariable Long id) {

        Optional<EntregaDTO> resultado = entregaService.buscarPorId(id);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<EntregaDTO> crear(
            @Valid @RequestBody EntregaDTO dto) {

        EntregaDTO entrega = entregaService.crear(dto);

        return ResponseEntity.ok(entrega);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntregaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EntregaDTO dto) {

        Optional<EntregaDTO> resultado = entregaService.actualizar(id, dto);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        boolean eliminado = entregaService.eliminar(id);

        if (eliminado) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id
        );
    }
}
