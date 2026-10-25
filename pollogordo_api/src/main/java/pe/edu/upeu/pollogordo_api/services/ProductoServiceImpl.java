package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.ProductoDTO;
import pe.edu.upeu.pollogordo_api.entity.Producto;
import pe.edu.upeu.pollogordo_api.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodo() {

        return productoRepository.findAll()
                .stream()
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductoDTO> buscarPorId(Long id) {

        return productoRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public ProductoDTO crear(ProductoDTO dto) {

        Producto pro = new Producto();

        pro.setNombre(dto.nombre());
        pro.setDescripcion(dto.descripcion());
        pro.setTipo(dto.tipo());
        pro.setPrecio(dto.precio());
        pro.setEstado(dto.estado());
        pro.setImagen(dto.imagen());

        return converTODTO(productoRepository.save(pro));
    }

    @Override
    @Transactional
    public Optional<ProductoDTO> actualizar(Long id, ProductoDTO dto) {

        return productoRepository.findById(id).map(pro -> {

            pro.setNombre(dto.nombre());
            pro.setDescripcion(dto.descripcion());
            pro.setTipo(dto.tipo());
            pro.setPrecio(dto.precio());
            pro.setEstado(dto.estado());
            pro.setImagen(dto.imagen());

            return converTODTO(productoRepository.save(pro));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private ProductoDTO converTODTO(Producto p) {

        return new ProductoDTO(
                p.getIdProducto(),
                p.getNombre(),
                p.getDescripcion(),
                p.getTipo(),
                p.getPrecio(),
                p.getEstado(),
                p.getImagen()
        );
    }
}