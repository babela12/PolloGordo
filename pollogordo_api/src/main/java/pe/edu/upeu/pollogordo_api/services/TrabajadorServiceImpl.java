package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.pollogordo_api.dto.PersonaDTO;
import pe.edu.upeu.pollogordo_api.dto.TrabajadorDTO;

import pe.edu.upeu.pollogordo_api.entity.Cargo;
import pe.edu.upeu.pollogordo_api.entity.Persona;
import pe.edu.upeu.pollogordo_api.entity.Trabajador;

import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;

import pe.edu.upeu.pollogordo_api.repository.CargoRepository;
import pe.edu.upeu.pollogordo_api.repository.PersonaRepository;
import pe.edu.upeu.pollogordo_api.repository.TrabajadorRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TrabajadorServiceImpl implements ITrabajadorService {

    private final TrabajadorRepository trabajadorRepository;
    private final PersonaRepository personaRepository;
    private final CargoRepository cargoRepository;

    public TrabajadorServiceImpl(
            TrabajadorRepository trabajadorRepository,
            PersonaRepository personaRepository,
            CargoRepository cargoRepository) {

        this.trabajadorRepository = trabajadorRepository;
        this.personaRepository = personaRepository;
        this.cargoRepository = cargoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrabajadorDTO> listarTodo() {

        return trabajadorRepository.findAll()
                .stream()
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrabajadorDTO> buscarPorId(Long id) {

        return trabajadorRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public TrabajadorDTO crear(TrabajadorDTO dto) {

        validarFunciones(dto);

        if (personaRepository.existsByDni(dto.persona().dni())) {
            throw new IllegalArgumentException(
                    "El DNI " + dto.persona().dni()
                            + " ya está registrado"
            );
        }

        Cargo cargo = cargoRepository.findById(dto.idCargo())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cargo ID: " + dto.idCargo()
                                        + " no existe"
                        ));

        Persona persona = new Persona();

        persona.setNombre(dto.persona().nombre());
        persona.setApellidos(dto.persona().apellidos());
        persona.setDni(dto.persona().dni());
        persona.setNumTelef(dto.persona().numTelef());

        persona = personaRepository.save(persona);

        Trabajador trabajador = new Trabajador();

        trabajador.setPersona(persona);
        trabajador.setCargo(cargo);
        trabajador.setDireccion(dto.direccion());

        trabajador.setCapacidadPreparacion(
                dto.capacidadPreparacion()
        );

        trabajador.setCapacidadAsado(
                dto.capacidadAsado()
        );

        trabajador.setPuedeRepartir(
                dto.puedeRepartir()
        );

        // TODO TRABAJADOR NUEVO ESTARÁ ACTIVO
        trabajador.setEstado("A");

        return converTODTO(
                trabajadorRepository.save(trabajador)
        );
    }

    @Override
    @Transactional
    public Optional<TrabajadorDTO> actualizar(
            Long id, TrabajadorDTO dto) {

        validarFunciones(dto);

        return trabajadorRepository.findById(id)
                .map(trabajador -> {

                    Persona persona = trabajador.getPersona();

                    if (!persona.getDni().equals(dto.persona().dni())
                            && personaRepository.existsByDni(
                            dto.persona().dni())) {

                        throw new IllegalArgumentException(
                                "El DNI " + dto.persona().dni()
                                        + " ya está registrado"
                        );
                    }

                    Cargo cargo = cargoRepository
                            .findById(dto.idCargo())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Cargo ID: " + dto.idCargo()
                                                    + " no existe"
                                    ));

                    persona.setNombre(dto.persona().nombre());
                    persona.setApellidos(dto.persona().apellidos());
                    persona.setDni(dto.persona().dni());
                    persona.setNumTelef(dto.persona().numTelef());

                    personaRepository.save(persona);

                    trabajador.setCargo(cargo);
                    trabajador.setDireccion(dto.direccion());

                    trabajador.setCapacidadPreparacion(
                            dto.capacidadPreparacion()
                    );

                    trabajador.setCapacidadAsado(
                            dto.capacidadAsado()
                    );

                    trabajador.setPuedeRepartir(
                            dto.puedeRepartir()
                    );

                    // NO MODIFICAMOS EL ESTADO AL EDITAR

                    return converTODTO(
                            trabajadorRepository.save(trabajador)
                    );
                });
    }

    // CAMBIAR ESTADO DEL TRABAJADOR
    @Override
    @Transactional
    public Optional<TrabajadorDTO> cambiarEstado(
            Long id, String estado) {

        if (!"A".equals(estado) && !"I".equals(estado)) {
            throw new IllegalArgumentException(
                    "El estado debe ser A o I"
            );
        }

        return trabajadorRepository.findById(id)
                .map(trabajador -> {

                    trabajador.setEstado(estado);

                    return converTODTO(
                            trabajadorRepository.save(trabajador)
                    );
                });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (trabajadorRepository.existsById(id)) {
            trabajadorRepository.deleteById(id);
            return true;
        }

        return false;
    }

    // VALIDAR FUNCIONES SEGÚN EL CARGO
    private void validarFunciones(TrabajadorDTO dto) {

        if (dto.puedeRepartir() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar si el trabajador puede repartir"
            );
        }

        Cargo cargo = cargoRepository.findById(dto.idCargo())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cargo ID: " + dto.idCargo()
                                        + " no existe"
                        ));

        // GERENTE GENERAL NO NECESITA FUNCIONES OPERATIVAS
        if (cargo.getNombre().trim()
                .equalsIgnoreCase("Gerente General")) {
            return;
        }

        if (dto.capacidadPreparacion() == null
                && dto.capacidadAsado() == null
                && !dto.puedeRepartir()) {

            throw new IllegalArgumentException(
                    "El trabajador debe tener al menos una "
                            + "función habilitada"
            );
        }
    }

    // CONVERTIR ENTIDAD A DTO
    private TrabajadorDTO converTODTO(Trabajador t) {

        Persona persona = t.getPersona();

        PersonaDTO personaDTO = new PersonaDTO(
                persona.getIdPersona(),
                persona.getNombre(),
                persona.getApellidos(),
                persona.getDni(),
                persona.getNumTelef()
        );

        return new TrabajadorDTO(
                t.getIdTrabajador(),
                personaDTO,
                t.getCargo().getIdCargo(),
                t.getDireccion(),
                t.getCapacidadPreparacion(),
                t.getCapacidadAsado(),
                t.getPuedeRepartir(),
                t.getEstado()
        );
    }
}