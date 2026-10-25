package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.DistribucionDTO;
import pe.edu.upeu.pollogordo_api.dto.ItemPedidoDTO;
import pe.edu.upeu.pollogordo_api.entity.Pedido;

import java.util.List;
import java.util.Optional;

public interface IDistribucionService {

    List<DistribucionDTO> listarTodo();

    Optional<DistribucionDTO> buscarPorId(Long id);

    DistribucionDTO crear(DistribucionDTO distribucion);

    Optional<DistribucionDTO> actualizar(Long id, DistribucionDTO distribucion);

    boolean eliminar(Long id);

    void generarDistribucion(
            Pedido pedido,
            List<ItemPedidoDTO> items
    );
}