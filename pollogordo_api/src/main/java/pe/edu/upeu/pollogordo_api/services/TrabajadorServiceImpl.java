
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
import pe.edu.upeu.pollogordo_api.repository.AsigTrabajadorRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TrabajadorServiceImpl implements ITrabajadorService {

    private final TrabajadorRepository trabajadorRepository;
    private final PersonaRepository personaRepository;
    private final CargoRepository cargoRepository;
    private final AsigTrabajadorRepository asigTrabajadorRepository;

    public TrabajadorServiceImpl(
            TrabajadorRepository trabajadorRepository,
            PersonaRepository personaRepository,
            CargoRepository cargoRepository,
            AsigTrabajadorRepository asigTrabajadorRepository) {

        this.trabajadorRepository = trabajadorRepository;
        this.personaRepository = personaRepository;
        this.cargoRepository = cargoRepository;
        this.asigTrabajadorRepository = asigTrabajadorRepository;
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
                    "El DNI ya está registrado");
        }

        Cargo cargo = buscarCargo(dto.idCargo());

        Persona persona = new Persona();
        persona.setNombre(dto.persona().nombre().trim());
        persona.setApellidos(dto.persona().apellidos().trim());
        persona.setDni(dto.persona().dni());
        persona.setNumTelef(dto.persona().numTelef());

        persona = personaRepository.save(persona);

        Trabajador trabajador = new Trabajador();
        trabajador.setPersona(persona);
        trabajador.setCargo(cargo);
        trabajador.setDireccion(dto.direccion());
        trabajador.setCapacidadPreparacion(dto.capacidadPreparacion());
        trabajador.setCapacidadAsado(dto.capacidadAsado());
        trabajador.setPuedeRepartir(dto.puedeRepartir());
        trabajador.setEstado("A");

        return converTODTO(trabajadorRepository.save(trabajador));
    }

    @Override
    @Transactional
    public Optional<TrabajadorDTO> actualizar(Long id, TrabajadorDTO dto) {

        validarFunciones(dto);

        return trabajadorRepository.findById(id).map(trabajador -> {

            Persona persona = trabajador.getPersona();

            if (!persona.getDni().equals(dto.persona().dni())
                    && personaRepository.existsByDni(dto.persona().dni())) {

                throw new IllegalArgumentException(
                        "El DNI ya está registrado");
            }

            Cargo cargo = buscarCargo(dto.idCargo());

            persona.setNombre(dto.persona().nombre().trim());
            persona.setApellidos(dto.persona().apellidos().trim());
            persona.setDni(dto.persona().dni());
            persona.setNumTelef(dto.persona().numTelef());

            personaRepository.save(persona);

            trabajador.setCargo(cargo);
            trabajador.setDireccion(dto.direccion());
            trabajador.setCapacidadPreparacion(dto.capacidadPreparacion());
            trabajador.setCapacidadAsado(dto.capacidadAsado());
            trabajador.setPuedeRepartir(dto.puedeRepartir());

            return converTODTO(trabajadorRepository.save(trabajador));
        });
    }

    @Override
    @Transactional
    public Optional<TrabajadorDTO> cambiarEstado(Long id, String estado) {

        if (!"A".equals(estado) && !"I".equals(estado)) {
            throw new IllegalArgumentException(
                    "El estado debe ser A o I");
        }

        return trabajadorRepository.findById(id).map(trabajador -> {

            trabajador.setEstado(estado);

            return converTODTO(trabajadorRepository.save(trabajador));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (!trabajadorRepository.existsById(id)) {
            return false;
        }

        if (!asigTrabajadorRepository
                .findByTrabajador_IdTrabajador(id).isEmpty()) {

            throw new IllegalArgumentException(
                    "No se puede eliminar un trabajador con asignaciones registradas");
        }

        trabajadorRepository.deleteById(id);

        return true;
    }

    private Cargo buscarCargo(Long idCargo) {

        return cargoRepository.findById(idCargo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo ID: " + idCargo + " no existe"));
    }

    private void validarFunciones(TrabajadorDTO dto) {

        if (dto.puedeRepartir() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar si el trabajador puede repartir");
        }

        Cargo cargo = buscarCargo(dto.idCargo());

        if (cargo.getNombre().trim().equalsIgnoreCase("Gerente General")) {
            return;
        }

        if (dto.capacidadPreparacion() == null
                && dto.capacidadAsado() == null
                && !dto.puedeRepartir()) {

            throw new IllegalArgumentException(
                    "El trabajador debe tener al menos una función habilitada");
        }
    }

    private TrabajadorDTO converTODTO(Trabajador trabajador) {

        Persona persona = trabajador.getPersona();

        PersonaDTO personaDTO = new PersonaDTO(
                persona.getIdPersona(),
                persona.getNombre(),
                persona.getApellidos(),
                persona.getDni(),
                persona.getNumTelef()
        );

        return new TrabajadorDTO(
                trabajador.getIdTrabajador(),
                personaDTO,
                trabajador.getCargo().getIdCargo(),
                trabajador.getDireccion(),
                trabajador.getCapacidadPreparacion(),
                trabajador.getCapacidadAsado(),
                trabajador.getPuedeRepartir(),
                trabajador.getEstado()
        );
    }
}
