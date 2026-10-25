
package pe.edu.upeu.pollogordo_api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PedidoResponseDTO(

        Long idPedido,
        String cliente,
        Double total,
        String estado,
        LocalDate fecha,

        LocalDate fechaEvento,
        LocalTime horaEntrega,
        String direccionEvento,
        String telefonoContacto,

        String confirmado,
        LocalDate fechaConfirmacion,

        List<DetalleResponseDTO> productos

) {
}
