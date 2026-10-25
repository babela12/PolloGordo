package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.pollogordo_api.dto.CargoDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.ICargoService;

import java.util.List;

@RestController
@RequestMapping("/cargos")
public class CargoController {

    private final ICargoService cargoService;

    public CargoController(ICargoService cargoService) {
        this.cargoService = cargoService;
    }

    @GetMapping
    public ResponseEntity<List<CargoDTO>> listarTodo() {
        return ResponseEntity.ok(cargoService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoDTO> buscarPorId(@PathVariable Long id) {
        return cargoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CargoDTO> crear(@Valid @RequestBody CargoDTO dto) {
        return ResponseEntity.ok(cargoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CargoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CargoDTO dto) {

        return cargoService.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        if (cargoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id);
    }
}