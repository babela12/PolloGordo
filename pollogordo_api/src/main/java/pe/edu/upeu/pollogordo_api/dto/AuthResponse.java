package pe.edu.upeu.pollogordo_api.dto;

public record AuthResponse(
        String token,
        String usuario,
        String rol,
        Long idPersona,
        String nombreMostrar
) {
}
