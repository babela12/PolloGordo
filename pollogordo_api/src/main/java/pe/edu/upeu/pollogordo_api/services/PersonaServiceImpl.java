package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.PersonaDTO;
import pe.edu.upeu.pollogordo_api.entity.Persona;
import pe.edu.upeu.pollogordo_api.exception.DuplicateResourceException;
import pe.edu.upeu.pollogordo_api.repository.PersonaRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PersonaServiceImpl implements IPersonaService {

    private final PersonaRepository personaRepository;

    public PersonaServiceImpl(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaDTO> listarTodo() {
        return personaRepository.findAll().stream()
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonaDTO> buscarPorId(Long id) {
        return personaRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public PersonaDTO crear(PersonaDTO dto) {

        if (personaRepository.existsByDni(dto.dni())) {
            throw new DuplicateResourceException("Ya existe una persona con el DNI: " + dto.dni());
        }

        Persona per = new Persona();

        per.setNombre(dto.nombre());
        per.setApellidos(dto.apellidos());
        per.setDni(dto.dni());
        per.setNumTelef(dto.numTelef());

        return converTODTO(personaRepository.save(per));
    }

    @Override
    @Transactional
    public Optional<PersonaDTO> actualizar(Long id, PersonaDTO dto) {

        return personaRepository.findById(id).map(per -> {

            per.setNombre(dto.nombre());
            per.setApellidos(dto.apellidos());
            per.setDni(dto.dni());
            per.setNumTelef(dto.numTelef());

            return converTODTO(personaRepository.save(per));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (personaRepository.existsById(id)) {
            personaRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private PersonaDTO converTODTO(Persona p) {

        return new PersonaDTO(
                p.getIdPersona(),
                p.getNombre(),
                p.getApellidos(),
                p.getDni(),
                p.getNumTelef()
        );
    }
}