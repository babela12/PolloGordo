
package pe.edu.upeu.pollogordo_api.dto;

public record UsuarioDTO(
        Long idUsuario,
        String usuario,
        String rol,
        String nombrePersona,
        String apellidos,
        Long idPersona
) {
}
