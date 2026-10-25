package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductoDTO(

        @Parameter(hidden = true)
        Long idProducto,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        String descripcion,

        String tipo,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01")
        Double precio,

        String estado,

        @Parameter(hidden = true)
        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        String imagen
) {
}