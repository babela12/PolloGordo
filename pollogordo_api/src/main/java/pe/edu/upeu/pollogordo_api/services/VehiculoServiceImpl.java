
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.pollogordo_api.dto.VehiculoDTO;
import pe.edu.upeu.pollogordo_api.entity.Vehiculo;
import pe.edu.upeu.pollogordo_api.repository.VehiculoRepository;

import java.util.ArrayList;
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

        List<VehiculoDTO> lista = new ArrayList<>();

        for (Vehiculo vehiculo : vehiculoRepository.findAll()) {
            lista.add(converTODTO(vehiculo));
        }

        return lista;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehiculoDTO> buscarPorId(Long id) {

        Optional<Vehiculo> resultado = vehiculoRepository.findById(id);

        if (resultado.isPresent()) {
            return Optional.of(converTODTO(resultado.get()));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public VehiculoDTO crear(VehiculoDTO dto) {

        Vehiculo vehiculo = new Vehiculo();

        vehiculo.setTipo(dto.tipo());
        vehiculo.setPlaca(dto.placa());
        vehiculo.setCapacidad(dto.capacidad());
        vehiculo.setEstado(dto.estado());

        vehiculoRepository.save(vehiculo);

        return converTODTO(vehiculo);
    }

    @Override
    @Transactional
    public Optional<VehiculoDTO> actualizar(Long id, VehiculoDTO dto) {

        Optional<Vehiculo> resultado = vehiculoRepository.findById(id);

        if (resultado.isPresent()) {

            Vehiculo vehiculo = resultado.get();

            vehiculo.setTipo(dto.tipo());
            vehiculo.setPlaca(dto.placa());
            vehiculo.setCapacidad(dto.capacidad());
            vehiculo.setEstado(dto.estado());

            vehiculoRepository.save(vehiculo);

            return Optional.of(converTODTO(vehiculo));
        }

        return Optional.empty();
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

    private VehiculoDTO converTODTO(Vehiculo vehiculo) {

        return new VehiculoDTO(
                vehiculo.getIdVehiculo(),
                vehiculo.getTipo(),
                vehiculo.getPlaca(),
                vehiculo.getCapacidad(),
                vehiculo.getEstado()
        );
    }
}
