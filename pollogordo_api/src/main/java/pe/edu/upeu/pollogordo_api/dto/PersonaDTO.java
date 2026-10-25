package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PersonaDTO(

        @Parameter(hidden = true)
        Long idPersona,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(
                regexp = "\\d{8}",
                message = "El DNI debe contener exactamente 8 dígitos"
        )
        String dni,

        @NotBlank(message = "El número de celular es obligatorio")
        @Pattern(
                regexp = "9\\d{8}",
                message = "El celular debe tener 9 dígitos y comenzar con 9"
        )
        String numTelef
) {
}