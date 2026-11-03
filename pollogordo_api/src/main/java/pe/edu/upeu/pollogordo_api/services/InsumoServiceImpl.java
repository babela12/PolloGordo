
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.InsumoDTO;
import pe.edu.upeu.pollogordo_api.entity.Insumo;
import pe.edu.upeu.pollogordo_api.repository.InsumoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InsumoServiceImpl implements IInsumoService {

    private final InsumoRepository insumoRepository;

    public InsumoServiceImpl(InsumoRepository insumoRepository) {
        this.insumoRepository = insumoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsumoDTO> listarTodo() {

        List<Insumo> insumos = insumoRepository.findAll();

        insumos.sort((a, b) ->
                a.getNombre().compareToIgnoreCase(b.getNombre()));

        List<InsumoDTO> lista = new ArrayList<>();

        for (Insumo insumo : insumos) {
            lista.add(converTODTO(insumo));
        }

        return lista;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InsumoDTO> buscarPorId(Long id) {

        Optional<Insumo> resultado = insumoRepository.findById(id);

        if (resultado.isPresent()) {
            return Optional.of(converTODTO(resultado.get()));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public InsumoDTO crear(InsumoDTO dto) {

        validarStock(dto);

        Insumo insumo = new Insumo();

        insumo.setNombre(dto.nombre());
        insumo.setUnidad(dto.unidad());
        insumo.setPerecible(dto.perecible());
        insumo.setControlaStock(dto.controlaStock());
        insumo.setCantidadStock(obtenerStock(dto));
        insumo.setEstado(dto.estado());
        insumo.setImagen(dto.imagen());

        insumoRepository.save(insumo);

        return converTODTO(insumo);
    }

    @Override
    @Transactional
    public Optional<InsumoDTO> actualizar(Long id, InsumoDTO dto) {

        validarStock(dto);

        Optional<Insumo> resultado = insumoRepository.findById(id);

        if (resultado.isPresent()) {

            Insumo insumo = resultado.get();

            insumo.setNombre(dto.nombre());
            insumo.setUnidad(dto.unidad());
            insumo.setPerecible(dto.perecible());
            insumo.setControlaStock(dto.controlaStock());
            insumo.setCantidadStock(obtenerStock(dto));
            insumo.setEstado(dto.estado());
            insumo.setImagen(dto.imagen());

            insumoRepository.save(insumo);

            return Optional.of(converTODTO(insumo));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (insumoRepository.existsById(id)) {
            insumoRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private void validarStock(InsumoDTO dto) {

        if (!"S".equals(dto.controlaStock())
                && !"N".equals(dto.controlaStock())) {

            throw new IllegalArgumentException(
                    "Controla stock debe ser S o N"
            );
        }

        if ("S".equals(dto.controlaStock())
                && dto.cantidadStock() == null) {

            throw new IllegalArgumentException(
                    "La cantidad de stock es obligatoria"
            );
        }
    }

    private Double obtenerStock(InsumoDTO dto) {

        if ("S".equals(dto.controlaStock())) {
            return dto.cantidadStock();
        }

        return null;
    }

    private InsumoDTO converTODTO(Insumo insumo) {

        return new InsumoDTO(
                insumo.getIdInsumo(),
                insumo.getNombre(),
                insumo.getUnidad(),
                insumo.getPerecible(),
                insumo.getControlaStock(),
                insumo.getCantidadStock(),
                insumo.getEstado(),
                insumo.getImagen()
        );
    }
}
