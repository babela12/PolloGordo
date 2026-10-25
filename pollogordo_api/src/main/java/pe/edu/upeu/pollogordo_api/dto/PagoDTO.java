package pe.edu.upeu.pollogordo_api.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PagoDTO(

        @Parameter(hidden = true)
        Long idPago,

        @NotNull(message = "El pedido es obligatorio")
        Long idPedido,

        @NotBlank(message = "El método de pago es obligatorio")
        String metodo,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(
                value = "0.01",
                message = "El monto debe ser mayor a 0"
        )
        Double monto,
        LocalDate fechaPago,
        String referencia,
        String estado

) {

}