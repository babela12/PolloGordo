
package pe.edu.upeu.pollogordo_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PedidoDTO(

        @NotNull(message = "El cliente es obligatorio")
        Long idCliente,

        @NotEmpty(message = "El pedido debe tener al menos un producto")
        List<@Valid ItemPedidoDTO> items,

        @NotNull(message = "La fecha del evento es obligatoria")
        @FutureOrPresent(message = "La fecha del evento no puede ser anterior a hoy")
        LocalDate fechaEvento,

        @NotNull(message = "La hora de entrega es obligatoria")
        LocalTime horaEntrega,

        @NotBlank(message = "La dirección del evento es obligatoria")
        @Size(max = 250, message = "La dirección no puede superar los 250 caracteres")
        String direccionEvento,

        @NotBlank(message = "El teléfono de contacto es obligatorio")
        @Pattern(
                regexp = "^9[0-9]{8}$",
                message = "El teléfono debe tener 9 dígitos y comenzar con 9"
        )
        String telefonoContacto,

        String observacion

) {
}
