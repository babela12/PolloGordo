
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.pollogordo_api.dto.RegistroDTO;
import pe.edu.upeu.pollogordo_api.dto.UsuarioDTO;
import pe.edu.upeu.pollogordo_api.entity.Persona;
import pe.edu.upeu.pollogordo_api.entity.Usuario;
import pe.edu.upeu.pollogordo_api.exception.DuplicateResourceException;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.repository.PersonaRepository;
import pe.edu.upeu.pollogordo_api.repository.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            PersonaRepository personaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Optional<UsuarioDTO> registrarUsuario(RegistroDTO dto) {

        // Validar que el nombre de usuario sea único
        if (usuarioRepository.existsByUsuario(dto.usuario())) {
            throw new DuplicateResourceException(
                    "El nombre de usuario '" + dto.usuario() + "' ya existe"
            );
        }

        // Buscar la persona registrada en Oracle
        Persona persona = personaRepository.findById(dto.idPersona())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la persona con ID: " + dto.idPersona()
                ));

        // Verificar que la persona no tenga otra cuenta
        if (usuarioRepository.existsByPersona_IdPersona(dto.idPersona())) {
            throw new DuplicateResourceException(
                    "La persona ya tiene una cuenta de usuario registrada"
            );
        }

        // Crear el usuario
        Usuario usuario = new Usuario();
        usuario.setUsuario(dto.usuario());

        // Cifrar la contraseña con BCrypt
        String passwordEncriptado =
                passwordEncoder.encode(dto.password());

        usuario.setPassword(passwordEncriptado);

        // Asignar rol y persona
        usuario.setRol(dto.rol());
        usuario.setPersona(persona);

        // Guardar el usuario en Oracle
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Devolver los datos sin mostrar la contraseña
        return Optional.of(new UsuarioDTO(
                usuarioGuardado.getIdUsuario(),
                usuarioGuardado.getUsuario(),
                usuarioGuardado.getRol().name(),
                persona.getNombre(),
                persona.getApellidos()
        ));
    }
}
