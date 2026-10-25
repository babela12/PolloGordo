package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.ProductoDTO;

import java.util.List;
import java.util.Optional;

public interface IProductoService {

    List<ProductoDTO> listarTodo();

    Optional<ProductoDTO> buscarPorId(Long id);

    ProductoDTO crear(ProductoDTO p);

    Optional<ProductoDTO> actualizar(Long id, ProductoDTO p);

    boolean eliminar(Long id);
}
