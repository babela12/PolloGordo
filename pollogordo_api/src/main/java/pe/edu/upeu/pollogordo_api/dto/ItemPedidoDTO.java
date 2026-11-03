
package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoDTO(

        @NotNull(message = "El producto es obligatorio")
        Long idProducto,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Schema(example = "4")
        Integer cantidad,

        @NotBlank(message = "La unidad es obligatoria")
        String unidad,

        String acompanamiento,

        String tipoArroz,

        String tipoEnsalada

) {
}
