package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.EntregaDTO;

import java.util.List;
import java.util.Optional;

public interface IEntregaService {

    List<EntregaDTO> listarTodo();

    Optional<EntregaDTO> buscarPorId(Long id);

    EntregaDTO crear(EntregaDTO entrega);

    Optional<EntregaDTO> actualizar(Long id, EntregaDTO entrega);

    boolean eliminar(Long id);
}