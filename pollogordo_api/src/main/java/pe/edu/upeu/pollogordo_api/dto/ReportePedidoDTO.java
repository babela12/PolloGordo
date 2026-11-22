
package pe.edu.upeu.pollogordo_api.dto;

import java.time.LocalDate;

public record ReportePedidoDTO(
        Long idPedido,
        String cliente,
        LocalDate fechaEvento,
        String horaEntrega,
        String direccionEvento,
        String estado,
        String confirmado,
        Double total
) {
}
