package pe.edu.upeu.pollogordo_api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "el usuario es obligatorio")
        String usuario,
        @NotBlank(message = "el password es obligatorio")
        String password
) {
}
