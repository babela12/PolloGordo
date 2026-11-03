
package pe.edu.upeu.pollogordo_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PedidoResponseDTO(

        Long idPedido,
        String cliente,
        Double total,
        String estado,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fecha,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaEvento,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        @Schema(type = "string", example = "12:30")
        LocalTime horaEntrega,

        String direccionEvento,
        String telefonoContacto,

        String confirmado,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaConfirmacion,

        List<DetalleResponseDTO> productos

) {
}
