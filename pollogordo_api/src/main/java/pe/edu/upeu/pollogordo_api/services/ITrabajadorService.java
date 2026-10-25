package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.TrabajadorDTO;

import java.util.List;
import java.util.Optional;

public interface ITrabajadorService {

    List<TrabajadorDTO> listarTodo();

    Optional<TrabajadorDTO> buscarPorId(Long id);

    TrabajadorDTO crear(TrabajadorDTO trabajador);

    Optional<TrabajadorDTO> actualizar(Long id, TrabajadorDTO trabajador);

    boolean eliminar(Long id);

    Optional<TrabajadorDTO> cambiarEstado(Long id, String estado);
}