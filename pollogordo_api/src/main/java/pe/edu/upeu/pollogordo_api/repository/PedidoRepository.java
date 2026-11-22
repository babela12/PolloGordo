
package pe.edu.upeu.pollogordo_api.repository;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.upeu.pollogordo_api.dto.ReportePedidoDTO;
import pe.edu.upeu.pollogordo_api.entity.Pedido;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Buscar pedidos por cliente
    List<Pedido> findByClienteIdCliente(Long idCliente);

    // Buscar pedidos por estado
    List<Pedido> findByEstado(String estado);

    // Bloquear el pedido mientras se registra o modifica un pago
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM Pedido p
            WHERE p.idPedido = :idPedido
            """)
    Optional<Pedido> buscarPorIdConBloqueo(
            @Param("idPedido") Long idPedido
    );

    // Ejecutar procedimiento almacenado P_REPORTE_PEDIDOS
    @Procedure(name = "Pedido.reportePedidos")
    List<Object[]> ejecutarReportePedidos(
            @Param("PFECHA_INICIO") java.sql.Date fechaInicio,
            @Param("PFECHA_FIN") java.sql.Date fechaFin,
            @Param("PSOLO_CONFIRMADOS") Integer soloConfirmados
    );

    // Reporte de pedidos agrupados por estado
    @Query(value = """
            SELECT ESTADO AS "estado",
                   COUNT(*) AS "cantidad"
            FROM PEDIDO
            GROUP BY ESTADO
            ORDER BY ESTADO
            """, nativeQuery = true)
    List<Map<String, Object>> reportePorEstado();

    // Consultar pedidos por fecha del evento
    @Query(value = """
            SELECT p.ID_PEDIDO AS "idPedido",
                   TRIM(
                       COALESCE(persona.NOMBRE, '') || ' ' ||
                       COALESCE(persona.APELLIDOS, '')
                   ) AS "cliente",
                   p.FECHA_EVENTO AS "fechaEvento",
                   p.HORA_ENTREGA AS "horaEntrega",
                   p.DIRECCION_EVENTO AS "direccionEvento",
                   p.ESTADO AS "estado",
                   p.CONFIRMADO AS "confirmado",
                   COALESCE(SUM(d.SUBTOTAL), 0) AS "total"
            FROM PEDIDO p
            JOIN CLIENTE c
                ON c.ID_CLIENTE = p.ID_CLIENTE
            JOIN PERSONA persona
                ON persona.ID_PERSONA = c.ID_PERSONA
            LEFT JOIN DETALLE_PEDIDO d
                ON d.ID_PEDIDO = p.ID_PEDIDO
            WHERE p.FECHA_EVENTO >= :fechaInicio
              AND p.FECHA_EVENTO < :fechaFinExclusiva
              AND (
                  :soloConfirmados = 0
                  OR p.CONFIRMADO = 'S'
              )
            GROUP BY
                p.ID_PEDIDO,
                persona.NOMBRE,
                persona.APELLIDOS,
                p.FECHA_EVENTO,
                p.HORA_ENTREGA,
                p.DIRECCION_EVENTO,
                p.ESTADO,
                p.CONFIRMADO
            ORDER BY
                p.FECHA_EVENTO,
                p.HORA_ENTREGA,
                p.ID_PEDIDO
            """, nativeQuery = true)
    List<ReportePedidoFila> consultarReporteSQL(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFinExclusiva") LocalDate fechaFinExclusiva,
            @Param("soloConfirmados") int soloConfirmados
    );

    // Convertir el resultado SQL a ReportePedidoDTO
    default List<ReportePedidoDTO> reportePorFechaEvento(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            boolean soloConfirmados) {

        return consultarReporteSQL(
                fechaInicio,
                fechaFin.plusDays(1),
                soloConfirmados ? 1 : 0
        ).stream().map(f -> new ReportePedidoDTO(
                f.getIdPedido(),
                f.getCliente(),
                f.getFechaEvento(),
                f.getHoraEntrega(),
                f.getDireccionEvento(),
                f.getEstado(),
                f.getConfirmado(),
                f.getTotal()
        )).toList();
    }
}
