package pe.edu.upeu.pollogordo_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upeu.pollogordo_api.entity.Rol;

public record RegistroDTO(

        @NotBlank(message = "El usuario es obligatorio")
        String usuario,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        @NotNull(message = "La persona es obligatoria")
        Long idPersona

) {
}
