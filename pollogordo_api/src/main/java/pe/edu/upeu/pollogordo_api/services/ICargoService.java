package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.CargoDTO;

import java.util.List;
import java.util.Optional;

public interface ICargoService {

    List<CargoDTO> listarTodo();

    Optional<CargoDTO> buscarPorId(Long id);

    CargoDTO crear(CargoDTO cargo);

    Optional<CargoDTO> actualizar(Long id, CargoDTO cargo);

    boolean eliminar(Long id);
}