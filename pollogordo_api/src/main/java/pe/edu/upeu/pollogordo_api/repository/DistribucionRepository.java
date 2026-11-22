
package pe.edu.upeu.pollogordo_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.pollogordo_api.dto.ReporteInsumoDTO;
import java.time.LocalDate;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.pollogordo_api.entity.Distribucion;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.List;

@Repository
public interface DistribucionRepository
        extends JpaRepository<Distribucion, Long> {

    List<Distribucion> findByPedidoIdPedido(Long idPedido);

    // Ejecutar procedimiento almacenado P_REPORTE_INSUMOS
    @Procedure(name = "Distribucion.reporteInsumos")
    List<Object[]> ejecutarReporteInsumos(
            @Param("PFECHA_INICIO") java.sql.Date fechaInicio,
            @Param("PFECHA_FIN") java.sql.Date fechaFin,
            @Param("PSOLO_CONFIRMADOS") Integer soloConfirmados
    );

    @Query(value = """
            SELECT i.ID_INSUMO AS "idInsumo", i.NOMBRE AS "insumo", i.UNIDAD AS "unidad",
                   SUM(d.CANTIDAD) AS "cantidad", COUNT(DISTINCT p.ID_PEDIDO) AS "pedidos"
            FROM DISTRIBUCION d
            JOIN PEDIDO p ON p.ID_PEDIDO = d.ID_PEDIDO
            JOIN INSUMO i ON i.ID_INSUMO = d.ID_INSUMO
            WHERE p.FECHA_EVENTO >= :fechaInicio AND p.FECHA_EVENTO < :fechaFinExclusiva
              AND (:soloConfirmados = 0 OR p.CONFIRMADO = 'S')
            GROUP BY i.ID_INSUMO, i.NOMBRE, i.UNIDAD
            ORDER BY i.NOMBRE, i.ID_INSUMO
            """, nativeQuery = true)
    List<ReporteInsumoFila> consultarConsolidadoSQL(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFinExclusiva") LocalDate fechaFinExclusiva,
            @Param("soloConfirmados") int soloConfirmados);

    default List<ReporteInsumoDTO> consolidadoPorFechaEvento(
            LocalDate fechaInicio, LocalDate fechaFin, boolean soloConfirmados) {
        return consultarConsolidadoSQL(fechaInicio, fechaFin.plusDays(1), soloConfirmados ? 1 : 0)
                .stream().map(f -> new ReporteInsumoDTO(f.getIdInsumo(), f.getInsumo(),
                        f.getUnidad(), f.getCantidad(), f.getPedidos()))
                .toList();
    }
}
