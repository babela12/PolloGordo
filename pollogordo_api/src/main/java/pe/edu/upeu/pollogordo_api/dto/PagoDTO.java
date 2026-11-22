
package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record PagoDTO(

        @Parameter(hidden = true)
        Long idPago,

        @NotNull(message = "El pedido es obligatorio")
        Long idPedido,

        @NotBlank(message = "El método de pago es obligatorio")
        @Pattern(
                regexp = "(?i)YAPE|PLIN|TRANSFERENCIA|EFECTIVO",
                message = "El método debe ser YAPE, PLIN, TRANSFERENCIA o EFECTIVO"
        )
        String metodo,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(
                value = "0.01",
                message = "El monto debe ser mayor a 0"
        )
        Double monto,

        LocalDate fechaPago,

        String referencia,

        String estado,

        @Parameter(hidden = true)
        String evidencia

) {
}
