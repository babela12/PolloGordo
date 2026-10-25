
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.InsumoDTO;
import pe.edu.upeu.pollogordo_api.entity.Insumo;
import pe.edu.upeu.pollogordo_api.repository.InsumoRepository;

import java.util.Comparator;
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
        return insumoRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        Insumo::getNombre,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InsumoDTO> buscarPorId(Long id) {
        return insumoRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public InsumoDTO crear(InsumoDTO dto) {
        validarStock(dto);

        Insumo ins = new Insumo();

        ins.setNombre(dto.nombre());
        ins.setUnidad(dto.unidad());
        ins.setPerecible(dto.perecible());
        ins.setControlaStock(dto.controlaStock());
        ins.setCantidadStock(obtenerStock(dto));
        ins.setEstado(dto.estado());
        ins.setImagen(dto.imagen());

        return converTODTO(insumoRepository.save(ins));
    }

    @Override
    @Transactional
    public Optional<InsumoDTO> actualizar(Long id, InsumoDTO dto) {
        validarStock(dto);

        return insumoRepository.findById(id).map(ins -> {

            ins.setNombre(dto.nombre());
            ins.setUnidad(dto.unidad());
            ins.setPerecible(dto.perecible());
            ins.setControlaStock(dto.controlaStock());
            ins.setCantidadStock(obtenerStock(dto));
            ins.setEstado(dto.estado());
            ins.setImagen(dto.imagen());

            return converTODTO(insumoRepository.save(ins));
        });
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
        if (!"S".equals(dto.controlaStock()) &&
                !"N".equals(dto.controlaStock())) {
            throw new IllegalArgumentException(
                    "Controla stock debe ser S o N");
        }

        if ("S".equals(dto.controlaStock()) &&
                dto.cantidadStock() == null) {
            throw new IllegalArgumentException(
                    "La cantidad de stock es obligatoria");
        }
    }

    private Double obtenerStock(InsumoDTO dto) {
        return "S".equals(dto.controlaStock())
                ? dto.cantidadStock()
                : null;
    }

    private InsumoDTO converTODTO(Insumo i) {
        return new InsumoDTO(
                i.getIdInsumo(),
                i.getNombre(),
                i.getUnidad(),
                i.getPerecible(),
                i.getControlaStock(),
                i.getCantidadStock(),
                i.getEstado(),
                i.getImagen()
        );
    }
}
