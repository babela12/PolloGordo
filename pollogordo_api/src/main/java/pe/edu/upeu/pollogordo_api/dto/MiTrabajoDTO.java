
package pe.edu.upeu.pollogordo_api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MiTrabajoDTO(
        Long idAsig,
        Long idPedido,
        String funcion,
        LocalDate fechaTrabajo,
        LocalTime horaInicio,
        LocalTime horaFin,
        Double cantidadAsignada,
        Double cantidadRealizada,
        String estado
) {
}
