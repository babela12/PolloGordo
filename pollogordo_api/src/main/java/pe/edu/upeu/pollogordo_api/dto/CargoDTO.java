package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;

public record CargoDTO(

        @Parameter(hidden = true)
        Long idCargo,
        @NotBlank(message = "El nombre del cargo es obligatorio")
        String nombre,
        String descripcion,
        String estado

) {

}