
package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.ReporteEventosDTO;
import java.time.LocalDate;

public interface IReporteService {
    ReporteEventosDTO obtenerReporteEventos(
            LocalDate fechaInicio, LocalDate fechaFin, boolean soloConfirmados);
}
