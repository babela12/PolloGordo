
package pe.edu.upeu.pollogordo_api.repository;

/** Columnas devueltas por el consolidado SQL nativo de insumos. */
public interface ReporteInsumoFila {
    Long getIdInsumo();
    String getInsumo();
    String getUnidad();
    Double getCantidad();
    Long getPedidos();
}
