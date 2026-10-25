package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ClienteDTO(

        @Parameter(hidden = true)
        Long idCliente,

        @NotNull(message = "La persona es obligatoria")
        @Valid
        PersonaDTO persona,

        String direccion

) {

}