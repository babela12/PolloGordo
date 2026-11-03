
package pe.edu.upeu.pollogordo_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AsigTrabajadorDTO(

        @Parameter(hidden = true)
        Long idAsig,

        @NotNull(message = "El pedido es obligatorio")
        Long idPedido,

        Long idTrabajador,

        @Pattern(
                regexp = "PREPARADOR|ASADOR|REPARTIDOR",
                message = "La función debe ser PREPARADOR, ASADOR o REPARTIDOR"
        )
        String funcion,

        @Schema(
                type = "string",
                format = "date",
                example = "2026-10-24"
        )
        LocalDate fechaTrabajo,

        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "HH:mm:ss"
        )
        @Schema(
                type = "string",
                format = "time",
                example = "07:00:00",
                description = "Hora de inicio del trabajo"
        )
        LocalTime horaInicio,

        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "HH:mm:ss"
        )
        @Schema(
                type = "string",
                format = "time",
                example = "15:00:00",
                description = "Hora de finalización del trabajo"
        )
        LocalTime horaFin,

        @DecimalMin(
                value = "0.0",
                message = "La cantidad asignada no puede ser negativa"
        )
        Double cantidadAsignada,

        @DecimalMin(
                value = "0.0",
                message = "La cantidad realizada no puede ser negativa"
        )
        Double cantidadRealizada,

        @DecimalMin(
                value = "0.0",
                message = "La tarifa no puede ser negativa"
        )
        BigDecimal tarifaAplicada,

        @DecimalMin(
                value = "0.0",
                message = "El pago adicional no puede ser negativo"
        )
        BigDecimal pagoAdicional,

        @Pattern(
                regexp = "PROGRAMADO|EN_PROCESO|COMPLETADO|CANCELADO",
                message = "Estado no válido"
        )
        String estado,

        @Valid
        List<AsigTrabajadorDTO> trabajadores,

        @Valid
        List<EntregaDTO> entregas

) {
}
