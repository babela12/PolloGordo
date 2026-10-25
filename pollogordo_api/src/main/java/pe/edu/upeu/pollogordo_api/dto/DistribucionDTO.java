package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record DistribucionDTO(

        @Parameter(hidden = true)
        Long idDistribucion,

        @NotNull(message = "El pedido es obligatorio")
        Long idPedido,

        @NotNull(message = "El insumo es obligatorio")
        Long idInsumo,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(
                value = "0.01",
                message = "La cantidad debe ser mayor a 0"
        )
        Double cantidad,
        String estado

) {

}