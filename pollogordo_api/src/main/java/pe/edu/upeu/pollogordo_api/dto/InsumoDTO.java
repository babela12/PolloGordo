
    package pe.edu.upeu.pollogordo_api.dto;

    import io.swagger.v3.oas.annotations.Parameter;
    import io.swagger.v3.oas.annotations.media.Schema;
    import jakarta.validation.constraints.DecimalMin;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.Pattern;

    public record InsumoDTO(

            @Parameter(hidden = true)
            Long idInsumo,

            @NotBlank(message = "El nombre es obligatorio")
            String nombre,

            @NotBlank(message = "La unidad es obligatoria")
            String unidad,

            @NotBlank(message = "Debe indicar si el insumo es perecible")
            @Pattern(regexp = "[SN]", message = "Perecible debe ser S o N")
            String perecible,

            @NotBlank(message = "Debe indicar si controla stock")
            @Pattern(regexp = "[SN]", message = "Controla stock debe ser S o N")
            String controlaStock,

            @DecimalMin(value = "0.0",
                    message = "La cantidad de stock no puede ser negativa")
            Double cantidadStock,

            String estado,

            @Parameter(hidden = true)
            @Schema(accessMode = Schema.AccessMode.READ_ONLY)
            String imagen
    ) {
    }
