
package pe.edu.upeu.pollogordo_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.upeu.pollogordo_api.dto.UsuarioDTO;
import pe.edu.upeu.pollogordo_api.entity.Usuario;
import pe.edu.upeu.pollogordo_api.repository.UsuarioRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas")
public class CuentaController {

    private final UsuarioRepository usuarioRepository;

    public CuentaController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar() {

        List<Usuario> usuarios = usuarioRepository.findAll();

        List<UsuarioDTO> resultado = usuarios.stream()
                .map(usuario -> new UsuarioDTO(
                        usuario.getIdUsuario(),
                        usuario.getUsuario(),
                        usuario.getRol().name(),
                        usuario.getPersona() != null
                                ? usuario.getPersona().getNombre()
                                : null,
                        usuario.getPersona() != null
                                ? usuario.getPersona().getApellidos()
                                : null,
                        usuario.getPersona() != null
                                ? usuario.getPersona().getIdPersona()
                                : null
                ))
                .toList();

        return ResponseEntity.ok(resultado);
    }
}
