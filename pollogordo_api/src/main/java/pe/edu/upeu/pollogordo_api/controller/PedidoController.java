
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

    public PedidoController(IPedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> realizarPedido(
            @Valid @RequestBody PedidoDTO dto) {

        PedidoResponseDTO pedido = pedidoService.realizarPedido(dto);

        return ResponseEntity.ok(pedido);
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodo() {

        List<PedidoResponseDTO> lista = pedidoService.listarTodo();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idPedido}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(
            @PathVariable Long idPedido) {

        PedidoResponseDTO pedido = pedidoService.buscarPorId(idPedido);

        return ResponseEntity.ok(pedido);
    }

    @PutMapping("/{idPedido}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmarPedido(
            @PathVariable Long idPedido) {

        PedidoResponseDTO pedido = pedidoService.confirmarPedido(idPedido);

        return ResponseEntity.ok(pedido);
    }

    @PutMapping("/{idPedido}")
    public ResponseEntity<PedidoResponseDTO> modificarPedido(
            @PathVariable Long idPedido,
            @Valid @RequestBody PedidoDTO dto) {

        PedidoResponseDTO pedido =
                pedidoService.modificarPedido(idPedido, dto);

        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/consolidado")
    public ResponseEntity<List<ConsolidadoDTO>> obtenerConsolidado(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {

        List<ConsolidadoDTO> consolidado =
                pedidoService.obtenerConsolidado(fechaInicio, fechaFin);

        return ResponseEntity.ok(consolidado);
    }
}
