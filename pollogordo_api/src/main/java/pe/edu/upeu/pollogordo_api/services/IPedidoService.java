package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.ConsolidadoDTO;
import pe.edu.upeu.pollogordo_api.dto.PedidoDTO;
import pe.edu.upeu.pollogordo_api.dto.PedidoResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface IPedidoService {

    PedidoResponseDTO realizarPedido(PedidoDTO dto);

    PedidoResponseDTO confirmarPedido(Long idPedido);

    List<PedidoResponseDTO> listarTodo();

    PedidoResponseDTO buscarPorId(Long idPedido);

    PedidoResponseDTO modificarPedido(
            Long idPedido,
            PedidoDTO dto
    );

    // Bueno en esta parte lo asemos para obtener el consolidado por rango de fechas
    List<ConsolidadoDTO> obtenerConsolidado(
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
}