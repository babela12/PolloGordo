
package pe.edu.upeu.pollogordo_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.upeu.pollogordo_api.entity.Pago;

import java.util.List;
import java.util.Map;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Listar todos los pagos de un pedido
    List<Pago> findByPedido_IdPedidoOrderByFechaPagoDescIdPagoDesc(
            Long idPedido
    );

    // Calcular el total abonado de un pedido
    // Solo se consideran pagos registrados o verificados
    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM Pago p
        WHERE p.pedido.idPedido = :idPedido
        AND UPPER(p.estado) IN ('REGISTRADO', 'VERIFICADO')
        """)
    Double totalAbonadoPorPedido(
            @Param("idPedido") Long idPedido
    );

    // Calcular pagos pendientes de verificación
    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM Pago p
        WHERE p.pedido.idPedido = :idPedido
        AND UPPER(p.estado) = 'PENDIENTE_VERIFICACION'
        """)
    Double totalPendienteVerificacion(
            @Param("idPedido") Long idPedido
    );

    // Reporte de pagos por método
    // Solo incluye pagos registrados o verificados
    @Query(value = """
        SELECT METODO AS "metodo",
               COUNT(*) AS "cantidad",
               SUM(MONTO) AS "total"
        FROM PAGO
        WHERE UPPER(ESTADO) IN ('REGISTRADO', 'VERIFICADO')
        GROUP BY METODO
        ORDER BY METODO
        """, nativeQuery = true)
    List<Map<String, Object>> reportePorMetodo();

}
