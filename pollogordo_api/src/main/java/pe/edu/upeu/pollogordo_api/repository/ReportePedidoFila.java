
package pe.edu.upeu.pollogordo_api.repository;

import java.time.LocalDate;

/** Columnas devueltas por el reporte SQL nativo de pedidos. */
public interface ReportePedidoFila {
    Long getIdPedido();
    String getCliente();
    LocalDate getFechaEvento();
    String getHoraEntrega();
    String getDireccionEvento();
    String getEstado();
    String getConfirmado();
    Double getTotal();
}
