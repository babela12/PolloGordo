
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.pollogordo_api.dto.ProductoDTO;
import pe.edu.upeu.pollogordo_api.entity.Producto;
import pe.edu.upeu.pollogordo_api.repository.ProductoRepository;
import pe.edu.upeu.pollogordo_api.repository.DetallePedidoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            DetallePedidoRepository detallePedidoRepository) {

        this.productoRepository = productoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodo() {

        List<ProductoDTO> lista = new ArrayList<>();

        for (Producto producto : productoRepository.findAll()) {
            lista.add(converTODTO(producto));
        }

        return lista;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDTO> buscarPorId(Long id) {

        Optional<Producto> resultado = productoRepository.findById(id);

        if (resultado.isPresent()) {
            return Optional.of(converTODTO(resultado.get()));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public ProductoDTO crear(ProductoDTO dto) {

        Producto producto = new Producto();

        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setTipo(dto.tipo());
        producto.setPrecio(dto.precio());
        producto.setEstado(dto.estado());
        producto.setImagen(dto.imagen());

        productoRepository.save(producto);

        return converTODTO(producto);
    }

    @Override
    @Transactional
    public Optional<ProductoDTO> actualizar(Long id, ProductoDTO dto) {

        Optional<Producto> resultado = productoRepository.findById(id);

        if (resultado.isPresent()) {

            Producto producto = resultado.get();

            producto.setNombre(dto.nombre());
            producto.setDescripcion(dto.descripcion());
            producto.setTipo(dto.tipo());
            producto.setPrecio(dto.precio());
            producto.setEstado(dto.estado());
            producto.setImagen(dto.imagen());

            productoRepository.save(producto);

            return Optional.of(converTODTO(producto));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (!productoRepository.existsById(id)) {
            return false;
        }

        if (detallePedidoRepository.existsByProductoIdProducto(id)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar un producto que tiene pedidos registrados"
            );
        }

        productoRepository.deleteById(id);

        return true;
    }

    private ProductoDTO converTODTO(Producto producto) {

        return new ProductoDTO(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getTipo(),
                producto.getPrecio(),
                producto.getEstado(),
                producto.getImagen()
        );
    }
}
