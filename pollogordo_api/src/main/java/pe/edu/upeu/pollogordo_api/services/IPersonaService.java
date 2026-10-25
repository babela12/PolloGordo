package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.PersonaDTO;

import java.util.List;
import java.util.Optional;

public interface IPersonaService {

    List<PersonaDTO> listarTodo();

    Optional<PersonaDTO> buscarPorId(Long id);

    PersonaDTO crear(PersonaDTO p);

    Optional<PersonaDTO> actualizar(Long id, PersonaDTO p);

    boolean eliminar(Long id);
}
