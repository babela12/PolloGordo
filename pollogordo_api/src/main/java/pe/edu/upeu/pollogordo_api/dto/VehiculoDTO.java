package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehiculoDTO(

        @Parameter(hidden = true)
        Long idVehiculo,

        @NotBlank(message = "El tipo de vehículo es obligatorio")
        String tipo,
        @NotBlank(message = "La placa es obligatoria")
        String placa,
        @NotNull(message = "La capacidad es obligatoria")
        @DecimalMin(
                value = "0.01",
                message = "La capacidad debe ser mayor a 0"
        )
        Double capacidad,
        String estado

) {

}