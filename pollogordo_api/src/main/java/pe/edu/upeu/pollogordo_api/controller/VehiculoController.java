package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.pollogordo_api.dto.VehiculoDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.IVehiculoService;

import java.util.List;

@RestController
@RequestMapping("/vehiculos")
public class VehiculoController {

    private final IVehiculoService vehiculoService;

    public VehiculoController(IVehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public ResponseEntity<List<VehiculoDTO>> listarTodo() {
        return ResponseEntity.ok(vehiculoService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoDTO> buscarPorId(@PathVariable Long id) {
        return vehiculoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VehiculoDTO> crear(
            @Valid @RequestBody VehiculoDTO dto) {
        return ResponseEntity.ok(vehiculoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody VehiculoDTO dto) {

        return vehiculoService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        if (vehiculoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id);
    }
}