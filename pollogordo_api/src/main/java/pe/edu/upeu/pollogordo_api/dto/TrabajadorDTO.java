
package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrabajadorDTO(

        @Parameter(hidden = true)
        Long idTrabajador,

        @NotNull(message = "La persona es obligatoria")
        @Valid
        PersonaDTO persona,

        @NotNull(message = "El cargo es obligatorio")
        Long idCargo,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200,
                message = "La dirección no puede superar 200 caracteres")
        String direccion,

        @Min(value = 1,
                message = "La capacidad de preparación debe ser mayor que cero")
        Integer capacidadPreparacion,

        @Min(value = 1,
                message = "La capacidad de asado debe ser mayor que cero")
        Integer capacidadAsado,

        @NotNull(message = "Debe indicar si puede repartir")
        Boolean puedeRepartir,

        String estado

) {
}
