
package pe.edu.upeu.pollogordo_api.dto;

import java.time.LocalDate;
import java.util.List;

public record ReporteEventosDTO(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean soloConfirmados,
        List<ReportePedidoDTO> pedidos,
        List<ReporteInsumoDTO> insumos,
        Long totalPollos
) {
}
