package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.VehiculoDTO;
import pe.edu.upeu.pollogordo_api.entity.Vehiculo;
import pe.edu.upeu.pollogordo_api.repository.VehiculoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class VehiculoServiceImpl implements IVehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoServiceImpl(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDTO> listarTodo() {

        return vehiculoRepository.findAll()
                .stream()
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehiculoDTO> buscarPorId(Long id) {

        return vehiculoRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public VehiculoDTO crear(VehiculoDTO dto) {

        Vehiculo vehiculo = new Vehiculo();

        vehiculo.setTipo(dto.tipo());
        vehiculo.setPlaca(dto.placa());
        vehiculo.setCapacidad(dto.capacidad());
        vehiculo.setEstado(dto.estado());

        return converTODTO(
                vehiculoRepository.save(vehiculo)
        );
    }

    @Override
    @Transactional
    public Optional<VehiculoDTO> actualizar(Long id, VehiculoDTO dto) {

        return vehiculoRepository.findById(id)
                .map(vehiculo -> {

                    vehiculo.setTipo(dto.tipo());
                    vehiculo.setPlaca(dto.placa());
                    vehiculo.setCapacidad(dto.capacidad());
                    vehiculo.setEstado(dto.estado());

                    return converTODTO(
                            vehiculoRepository.save(vehiculo)
                    );
                });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (vehiculoRepository.existsById(id)) {
            vehiculoRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private VehiculoDTO converTODTO(Vehiculo v) {

        return new VehiculoDTO(
                v.getIdVehiculo(),
                v.getTipo(),
                v.getPlaca(),
                v.getCapacidad(),
                v.getEstado()
        );
    }
}