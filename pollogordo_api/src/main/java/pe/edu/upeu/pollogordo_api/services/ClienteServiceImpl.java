
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.ClienteDTO;
import pe.edu.upeu.pollogordo_api.dto.PersonaDTO;
import pe.edu.upeu.pollogordo_api.entity.Cliente;
import pe.edu.upeu.pollogordo_api.entity.Persona;
import pe.edu.upeu.pollogordo_api.repository.ClienteRepository;
import pe.edu.upeu.pollogordo_api.repository.PersonaRepository;
import pe.edu.upeu.pollogordo_api.repository.PedidoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteServiceImpl(
            ClienteRepository clienteRepository,
            PersonaRepository personaRepository,
            PedidoRepository pedidoRepository) {

        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> listarTodo() {

        return clienteRepository.findAll()
                .stream()
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClienteDTO> buscarPorId(Long id) {

        return clienteRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public ClienteDTO crear(ClienteDTO dto) {

        if (personaRepository.existsByDni(dto.persona().dni())) {
            throw new IllegalArgumentException(
                    "El DNI ya está registrado"
            );
        }

        Persona persona = new Persona();

        persona.setNombre(dto.persona().nombre().trim());
        persona.setApellidos(dto.persona().apellidos().trim());
        persona.setDni(dto.persona().dni());
        persona.setNumTelef(dto.persona().numTelef());

        persona = personaRepository.save(persona);

        Cliente cliente = new Cliente();

        cliente.setPersona(persona);
        cliente.setDireccion(dto.direccion());

        return converTODTO(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public Optional<ClienteDTO> actualizar(Long id, ClienteDTO dto) {

        return clienteRepository.findById(id).map(cliente -> {

            Persona persona = cliente.getPersona();

            if (!persona.getDni().equals(dto.persona().dni())
                    && personaRepository.existsByDni(dto.persona().dni())) {

                throw new IllegalArgumentException(
                        "El DNI ya está registrado"
                );
            }

            persona.setNombre(dto.persona().nombre().trim());
            persona.setApellidos(dto.persona().apellidos().trim());
            persona.setDni(dto.persona().dni());
            persona.setNumTelef(dto.persona().numTelef());

            personaRepository.save(persona);

            cliente.setDireccion(dto.direccion());

            return converTODTO(clienteRepository.save(cliente));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (!clienteRepository.existsById(id)) {
            return false;
        }

        if (!pedidoRepository.findByClienteIdCliente(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar un cliente con pedidos registrados"
            );
        }

        clienteRepository.deleteById(id);

        return true;
    }

    private ClienteDTO converTODTO(Cliente c) {

        Persona persona = c.getPersona();

        PersonaDTO personaDTO = new PersonaDTO(
                persona.getIdPersona(),
                persona.getNombre(),
                persona.getApellidos(),
                persona.getDni(),
                persona.getNumTelef()
        );

        return new ClienteDTO(
                c.getIdCliente(),
                personaDTO,
                c.getDireccion()
        );
    }
}
