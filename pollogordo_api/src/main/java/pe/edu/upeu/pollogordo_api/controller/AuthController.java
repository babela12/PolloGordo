
package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import pe.edu.upeu.pollogordo_api.dto.AuthResponse;
import pe.edu.upeu.pollogordo_api.dto.LoginRequest;
import pe.edu.upeu.pollogordo_api.dto.RegistroDTO;
import pe.edu.upeu.pollogordo_api.dto.UsuarioDTO;
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

        Long idPersona = usuario.getPersona() != null
                ? usuario.getPersona().getIdPersona()
                : null;

        return ResponseEntity.ok(new AuthResponse(
                jwtToken,
                usuario.getUsuario(),
                usuario.getRol().name(),
                idPersona
        ));
    }
}
