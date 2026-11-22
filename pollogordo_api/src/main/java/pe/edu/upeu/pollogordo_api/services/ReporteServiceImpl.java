
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.pollogordo_api.dto.ReporteEventosDTO;
import pe.edu.upeu.pollogordo_api.dto.ReportePedidoDTO;
import pe.edu.upeu.pollogordo_api.dto.ReporteInsumoDTO;

import pe.edu.upeu.pollogordo_api.repository.PedidoRepository;
import pe.edu.upeu.pollogordo_api.repository.DistribucionRepository;
import pe.edu.upeu.pollogordo_api.repository.DetallePedidoRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReporteServiceImpl implements IReporteService {

    private final PedidoRepository pedidoRepository;
    private final DistribucionRepository distribucionRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public ReporteServiceImpl(
            PedidoRepository pedidoRepository,
            DistribucionRepository distribucionRepository,
            DetallePedidoRepository detallePedidoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.distribucionRepository = distribucionRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteEventosDTO obtenerReporteEventos(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            boolean soloConfirmados) {

        // VALIDAR FECHAS
        if (fechaInicio == null ||
                fechaFin == null ||
                fechaInicio.isAfter(fechaFin)) {

            throw new IllegalArgumentException(
                    "La fecha inicial debe ser menor o igual a la fecha final"
            );
        }

        // REPORTE DE PEDIDOS
        List<Object[]> resultadosPedidos =
                pedidoRepository.ejecutarReportePedidos(
                        Date.valueOf(fechaInicio),
                        Date.valueOf(fechaFin),
                        soloConfirmados ? 1 : 0
                );

        List<ReportePedidoDTO> pedidos = resultadosPedidos.stream()
                .map(f -> new ReportePedidoDTO(
                        ((Number) f[0]).longValue(),
                        (String) f[1],
                        ((Timestamp) f[2])
                                .toLocalDateTime()
                                .toLocalDate(),
                        (String) f[3],
                        (String) f[4],
                        (String) f[5],
                        (String) f[6],
                        ((Number) f[7]).doubleValue()
                ))
                .toList();

        // REPORTE DE INSUMOS
        List<Object[]> resultadosInsumos =
                distribucionRepository.ejecutarReporteInsumos(
                        Date.valueOf(fechaInicio),
                        Date.valueOf(fechaFin),
                        soloConfirmados ? 1 : 0
                );

        List<ReporteInsumoDTO> insumos = resultadosInsumos.stream()
                .map(f -> new ReporteInsumoDTO(
                        ((Number) f[0]).longValue(),
                        (String) f[1],
                        (String) f[2],
                        ((Number) f[3]).doubleValue(),
                        ((Number) f[4]).longValue()
                ))
                .toList();

        // CALCULAR POLLOS NECESARIOS
        List<Long> idsPedidos = pedidos.stream()
                .map(ReportePedidoDTO::idPedido)
                .distinct()
                .toList();

        Long totalPollos = idsPedidos.isEmpty()
                ? 0L
                : detallePedidoRepository
                .calcularPollosPorPedidos(idsPedidos);

        // CONSTRUIR REPORTE
        return new ReporteEventosDTO(
                fechaInicio,
                fechaFin,
                soloConfirmados,
                pedidos,
                insumos,
                totalPollos == null ? 0L : totalPollos
        );
    }
}
