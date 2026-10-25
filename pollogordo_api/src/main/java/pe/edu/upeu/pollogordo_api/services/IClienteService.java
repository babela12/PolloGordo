package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.ClienteDTO;

import java.util.List;
import java.util.Optional;

public interface IClienteService {

    List<ClienteDTO> listarTodo();

    Optional<ClienteDTO> buscarPorId(Long id);

    ClienteDTO crear(ClienteDTO cliente);

    Optional<ClienteDTO> actualizar(Long id, ClienteDTO cliente);

    boolean eliminar(Long id);
}