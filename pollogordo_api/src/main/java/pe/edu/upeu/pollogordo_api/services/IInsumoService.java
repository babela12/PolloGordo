package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.InsumoDTO;

import java.util.List;
import java.util.Optional;

public interface IInsumoService {

    List<InsumoDTO> listarTodo();

    Optional<InsumoDTO> buscarPorId(Long id);

    InsumoDTO crear(InsumoDTO insumo);

    Optional<InsumoDTO> actualizar(Long id, InsumoDTO insumo);

    boolean eliminar(Long id);
}