
package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.AsigTrabajadorDTO;

import java.util.List;
import java.util.Optional;

public interface IAsigTrabajadorService {

    List<AsigTrabajadorDTO> listarTodo();

    Optional<AsigTrabajadorDTO> buscarPorId(Long id);

    AsigTrabajadorDTO crear(AsigTrabajadorDTO asignacion);

    Optional<AsigTrabajadorDTO> actualizar(
            Long id,
            AsigTrabajadorDTO asignacion
    );

    boolean eliminar(Long id);

    AsigTrabajadorDTO crearAsignacionCompleta(
            AsigTrabajadorDTO asignacion
    );

}
