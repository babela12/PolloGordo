
package pe.edu.upeu.pollogordo_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoDTO(

        @NotNull(message = "El producto es obligatorio")
        Long idProducto,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(
                value = "0.01",
                message = "La cantidad debe ser mayor a 0"
        )
        Double cantidad,

        @NotBlank(message = "La unidad es obligatoria")
        String unidad,

        String acompanamiento,

        String tipoArroz,

        String tipoEnsalada

) {}
