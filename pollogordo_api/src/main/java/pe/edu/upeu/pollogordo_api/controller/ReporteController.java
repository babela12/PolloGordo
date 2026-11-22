
package pe.edu.upeu.pollogordo_api.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import pe.edu.upeu.pollogordo_api.dto.ReporteEventosDTO;
import pe.edu.upeu.pollogordo_api.services.IReporteService;
import java.time.LocalDate;
import pe.edu.upeu.pollogordo_api.repository.PedidoRepository;
import pe.edu.upeu.pollogordo_api.repository.PagoRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final IReporteService reporteService;
    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;

    public ReporteController(PedidoRepository pedidoRepository,
                             PagoRepository pagoRepository,
                             IReporteService reporteService) {
        this.reporteService = reporteService;
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
    }

    @GetMapping("/pedidos")
    public List<Map<String, Object>> reportePedidos() {
        return pedidoRepository.reportePorEstado();
    }

    @GetMapping("/pagos")
    public List<Map<String, Object>> reportePagos() {
        return pagoRepository.reportePorMetodo();
    }

    @GetMapping("/eventos")
    public ReporteEventosDTO reporteEventos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "false") boolean soloConfirmados) {
        return reporteService.obtenerReporteEventos(fechaInicio, fechaFin, soloConfirmados);
    }
}
