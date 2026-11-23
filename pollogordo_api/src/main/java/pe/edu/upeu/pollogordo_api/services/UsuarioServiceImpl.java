
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.pollogordo_api.dto.ActualizarCuentaDTO;
import pe.edu.upeu.pollogordo_api.dto.RegistroDTO;
import pe.edu.upeu.pollogordo_api.dto.UsuarioDTO;
import pe.edu.upeu.pollogordo_api.entity.Persona;
import pe.edu.upeu.pollogordo_api.entity.Rol;
import pe.edu.upeu.pollogordo_api.entity.Usuario;
import pe.edu.upeu.pollogordo_api.exception.DuplicateResourceException;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.repository.PersonaRepository;
import pe.edu.upeu.pollogordo_api.repository.TrabajadorRepository;
import pe.edu.upeu.pollogordo_api.repository.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            PersonaRepository personaRepository,
            UsuarioRepository usuarioRepository,
            TrabajadorRepository trabajadorRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Registrar usuarios
    @Override
    @Transactional
    public Optional<UsuarioDTO> registrarUsuario(RegistroDTO dto) {

        String nombreUsuario = dto.usuario().trim();

        if (usuarioRepository.existsByUsuario(nombreUsuario)) {
            throw new DuplicateResourceException(
                    "El nombre de usuario '" + nombreUsuario + "' ya existe"
            );
        }

        Persona persona = personaRepository.findById(dto.idPersona())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la persona con ID: " + dto.idPersona()
                ));

        if (usuarioRepository.existsByPersona_IdPersona(dto.idPersona())) {
            throw new DuplicateResourceException(
                    "La persona ya tiene una cuenta de usuario registrada"
            );
        }

        if (dto.rol() == Rol.TRABAJADOR) {

            if (!trabajadorRepository.existsByPersona_IdPersona(dto.idPersona())) {
                throw new ResourceNotFoundException(
                        "La persona seleccionada no está registrada como trabajador"
                );
            }
        }

        Usuario usuario = new Usuario();
        usuario.setUsuario(nombreUsuario);

        usuario.setPassword(
                passwordEncoder.encode(dto.password())
        );

        usuario.setRol(dto.rol());
        usuario.setPersona(persona);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return Optional.of(new UsuarioDTO(
                usuarioGuardado.getIdUsuario(),
                usuarioGuardado.getUsuario(),
                usuarioGuardado.getRol().name(),
                persona.getNombre(),
                persona.getApellidos(),
                persona.getIdPersona()
        ));
    }

    // Actualizar nombre de usuario y contraseña
    @Override
    @Transactional
    public UsuarioDTO actualizarCuenta(
            Long idUsuario,
            ActualizarCuentaDTO dto
    ) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario con ID: " + idUsuario
                ));

        String nuevoUsuario = dto.usuario().trim();

        if (nuevoUsuario.isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario no puede estar vacío"
            );
        }

        if (!usuario.getUsuario().equals(nuevoUsuario)
                && usuarioRepository.existsByUsuario(nuevoUsuario)) {

            throw new DuplicateResourceException(
                    "El nombre de usuario '" + nuevoUsuario + "' ya existe"
            );
        }

        usuario.setUsuario(nuevoUsuario);

        // Guardar la nueva contraseña cifrada con BCrypt
        usuario.setPassword(
                passwordEncoder.encode(dto.password())
        );

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Persona persona = usuarioGuardado.getPersona();

        return new UsuarioDTO(
                usuarioGuardado.getIdUsuario(),
                usuarioGuardado.getUsuario(),
                usuarioGuardado.getRol().name(),
                persona.getNombre(),
                persona.getApellidos(),
                persona.getIdPersona()
        );
    }
}
