
package pe.edu.upeu.pollogordo_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.upeu.pollogordo_api.entity.DetallePedido;

import java.util.List;

@Repository
public interface DetallePedidoRepository
        extends JpaRepository<DetallePedido, Long> {

    // Listar los detalles de un pedido
    List<DetallePedido> findByPedidoIdPedido(Long idPedido);

    // Verificar si un producto tiene pedidos asociados
    boolean existsByProductoIdProducto(Long idProducto);

    // Calcular el total de un pedido
    @Query("""
        SELECT COALESCE(SUM(d.subtotal), 0)
        FROM DetallePedido d
        WHERE d.pedido.idPedido = :idPedido
        """)
    Double calcularTotalPedido(
            @Param("idPedido") Long idPedido
    );

    // Sumar pollos de los pedidos incluidos en el reporte
    @Query("""
        SELECT COALESCE(SUM(d.cantidadPollos), 0)
        FROM DetallePedido d
        WHERE d.pedido.idPedido IN :idsPedidos
        """)
    Long calcularPollosPorPedidos(
            @Param("idsPedidos") List<Long> idsPedidos
    );
}
