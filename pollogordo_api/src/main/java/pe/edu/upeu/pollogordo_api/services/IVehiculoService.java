package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.VehiculoDTO;

import java.util.List;
import java.util.Optional;

public interface IVehiculoService {

    List<VehiculoDTO> listarTodo();

    Optional<VehiculoDTO> buscarPorId(Long id);

    VehiculoDTO crear(VehiculoDTO vehiculo);

    Optional<VehiculoDTO> actualizar(Long id, VehiculoDTO vehiculo);

    boolean eliminar(Long id);
}