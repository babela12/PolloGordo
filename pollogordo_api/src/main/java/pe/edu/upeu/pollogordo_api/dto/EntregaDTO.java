
package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EntregaDTO(

        @Parameter(hidden = true)
        Long idEntrega,

        @NotNull(message = "El pedido es obligatorio")
        Long idPedido,

        @NotNull(message = "El vehículo es obligatorio")
        Long idVehiculo,

        @NotNull(message = "El trabajador es obligatorio")
        Long idTrabajador,

        LocalDate fechaEntrega,

        @NotNull(message = "La cantidad de pollos es obligatoria")
        @Min(value = 1,
                message = "La cantidad de pollos debe ser mayor a cero")
        Integer cantidadPollos,

        String estado,

        String observacion

) {
}
