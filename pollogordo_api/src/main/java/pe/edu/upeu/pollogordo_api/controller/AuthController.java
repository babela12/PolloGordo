
package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.pollogordo_api.dto.ActualizarCuentaDTO;
import pe.edu.upeu.pollogordo_api.dto.AuthResponse;
import pe.edu.upeu.pollogordo_api.dto.LoginRequest;
import pe.edu.upeu.pollogordo_api.dto.RegistroDTO;
import pe.edu.upeu.pollogordo_api.dto.UsuarioDTO;
import pe.edu.upeu.pollogordo_api.entity.Persona;
import pe.edu.upeu.pollogordo_api.entity.Usuario;
import pe.edu.upeu.pollogordo_api.repository.UsuarioRepository;
import pe.edu.upeu.pollogordo_api.security.JwtService;
import pe.edu.upeu.pollogordo_api.security.UsuarioPrincipal;
import pe.edu.upeu.pollogordo_api.services.IUsuarioService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IUsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(
            IUsuarioService usuarioService,
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            JwtService jwtService
    ) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    // Registrar usuarios internos
    @PostMapping("/registro")
    @PreAuthorize("hasAuthority('GERENTE')")
    public ResponseEntity<UsuarioDTO> registrar(
            @Valid @RequestBody RegistroDTO dto
    ) {

        UsuarioDTO usuario = usuarioService.registrarUsuario(dto)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se pudo registrar el usuario"
                        )
                );

        return new ResponseEntity<>(usuario, HttpStatus.CREATED);
    }

    // Iniciar sesión
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usuario(),
                        request.password()
                )
        );

        Usuario usuario = usuarioRepository
                .findByUsuario(request.usuario())
                .orElseThrow();

        String jwtToken = jwtService.generateToken(
                new UsuarioPrincipal(usuario)
        );

        Persona persona = usuario.getPersona();

        Long idPersona = persona != null
                ? persona.getIdPersona()
                : null;

        String nombreMostrar = usuario.getUsuario();

        if (persona != null
                && persona.getNombre() != null
                && !persona.getNombre().isBlank()
                && persona.getApellidos() != null
                && !persona.getApellidos().isBlank()) {

            String primerNombre =
                    persona.getNombre().trim().split("\\s+")[0];

            String primerApellido =
                    persona.getApellidos().trim().split("\\s+")[0];

            nombreMostrar = primerNombre + " " + primerApellido;
        }

        return ResponseEntity.ok(new AuthResponse(
                jwtToken,
                usuario.getUsuario(),
                usuario.getRol().name(),
                idPersona,
                nombreMostrar
        ));
    }

    // Editar cuenta existente
    @PutMapping("/cuentas/{idUsuario}")
    @PreAuthorize("hasAuthority('GERENTE')")
    public ResponseEntity<UsuarioDTO> actualizarCuenta(
            @PathVariable Long idUsuario,
            @Valid @RequestBody ActualizarCuentaDTO dto
    ) {

        UsuarioDTO usuario = usuarioService.actualizarCuenta(
                idUsuario,
                dto
        );

        return ResponseEntity.ok(usuario);
    }
}
