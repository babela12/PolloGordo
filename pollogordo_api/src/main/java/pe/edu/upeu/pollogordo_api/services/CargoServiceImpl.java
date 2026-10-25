package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.CargoDTO;
import pe.edu.upeu.pollogordo_api.entity.Cargo;
import pe.edu.upeu.pollogordo_api.repository.CargoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CargoServiceImpl implements ICargoService {

    private final CargoRepository cargoRepository;

    public CargoServiceImpl(CargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoDTO> listarTodo() {

        return cargoRepository.findAll()
                .stream()
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CargoDTO> buscarPorId(Long id) {

        return cargoRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public CargoDTO crear(CargoDTO dto) {

        Cargo cargo = new Cargo();

        cargo.setNombre(dto.nombre());
        cargo.setDescripcion(dto.descripcion());
        cargo.setEstado(dto.estado());

        return converTODTO(
                cargoRepository.save(cargo)
        );
    }

    @Override
    @Transactional
    public Optional<CargoDTO> actualizar(Long id, CargoDTO dto) {

        return cargoRepository.findById(id)
                .map(cargo -> {

                    cargo.setNombre(dto.nombre());
                    cargo.setDescripcion(dto.descripcion());
                    cargo.setEstado(dto.estado());

                    return converTODTO(
                            cargoRepository.save(cargo)
                    );
                });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (cargoRepository.existsById(id)) {
            cargoRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private CargoDTO converTODTO(Cargo c) {

        return new CargoDTO(
                c.getIdCargo(),
                c.getNombre(),
                c.getDescripcion(),
                c.getEstado()
        );
    }
}