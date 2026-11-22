
package pe.edu.upeu.pollogordo_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.upeu.pollogordo_api.dto.MiTrabajoDTO;
import pe.edu.upeu.pollogordo_api.entity.AsigTrabajador;
import pe.edu.upeu.pollogordo_api.entity.Trabajador;
import pe.edu.upeu.pollogordo_api.entity.Usuario;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.repository.AsigTrabajadorRepository;
import pe.edu.upeu.pollogordo_api.repository.TrabajadorRepository;
import pe.edu.upeu.pollogordo_api.repository.UsuarioRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mi-trabajo")
public class MiTrabajoController {

    private final UsuarioRepository usuarioRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final AsigTrabajadorRepository asigTrabajadorRepository;

    public MiTrabajoController(
            UsuarioRepository usuarioRepository,
            TrabajadorRepository trabajadorRepository,
            AsigTrabajadorRepository asigTrabajadorRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.asigTrabajadorRepository = asigTrabajadorRepository;
    }

    @GetMapping
    public ResponseEntity<List<MiTrabajoDTO>> misAsignaciones(
            Authentication authentication
    ) {

        String nombreUsuario = authentication.getName();

        Usuario usuario = usuarioRepository.findByUsuario(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el usuario"
                ));

        Trabajador trabajador = trabajadorRepository
                .findByPersona_IdPersona(usuario.getPersona().getIdPersona())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el trabajador"
                ));

        List<AsigTrabajador> asignaciones =
                asigTrabajadorRepository.findByTrabajador_IdTrabajador(
                        trabajador.getIdTrabajador()
                );

        List<MiTrabajoDTO> resultado = asignaciones.stream()
                .map(asig -> new MiTrabajoDTO(
                        asig.getIdAsig(),
                        asig.getPedido() != null
                                ? asig.getPedido().getIdPedido()
                                : null,
                        asig.getFuncion(),
                        asig.getFechaTrabajo(),
                        asig.getHoraInicio(),
                        asig.getHoraFin(),
                        asig.getCantidadAsignada(),
                        asig.getCantidadRealizada(),
                        asig.getEstado()
                ))
                .toList();

        return ResponseEntity.ok(resultado);
    }
}
