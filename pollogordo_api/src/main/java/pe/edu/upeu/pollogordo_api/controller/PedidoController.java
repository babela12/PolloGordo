package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.pollogordo_api.dto.ConsolidadoDTO;
import pe.edu.upeu.pollogordo_api.dto.PedidoDTO;
import pe.edu.upeu.pollogordo_api.dto.PedidoResponseDTO;
import pe.edu.upeu.pollogordo_api.services.IPedidoService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final IPedidoService pedidoService;

    public PedidoController(
            IPedidoService pedidoService) {

        this.pedidoService = pedidoService;
    }


    @PostMapping
    public ResponseEntity<PedidoResponseDTO> realizarPedido(
            @Valid @RequestBody PedidoDTO dto) {

        return ResponseEntity.ok(
                pedidoService.realizarPedido(dto)
        );
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodo() {

        return ResponseEntity.ok(
                pedidoService.listarTodo()
        );
    }

    @GetMapping("/{idPedido}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(
            @PathVariable Long idPedido) {

        return ResponseEntity.ok(
                pedidoService.buscarPorId(idPedido)
        );
    }

    @PutMapping("/{idPedido}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmarPedido(
            @PathVariable Long idPedido) {

        return ResponseEntity.ok(
                pedidoService.confirmarPedido(idPedido)
        );
    }

    @PutMapping("/{idPedido}")
    public ResponseEntity<PedidoResponseDTO> modificarPedido(
            @PathVariable Long idPedido,
            @Valid @RequestBody PedidoDTO dto) {

        return ResponseEntity.ok(
                pedidoService.modificarPedido(
                        idPedido,
                        dto
                )
        );
    }

    @GetMapping("/consolidado")
    public ResponseEntity<List<ConsolidadoDTO>> obtenerConsolidado(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {

        return ResponseEntity.ok(
                pedidoService.obtenerConsolidado(
                        fechaInicio,
                        fechaFin
                )
        );
    }
}