
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import pe.edu.upeu.pollogordo_api.dto.PagoDTO;
import pe.edu.upeu.pollogordo_api.dto.ResumenPagoDTO;
import pe.edu.upeu.pollogordo_api.entity.Pago;
import pe.edu.upeu.pollogordo_api.entity.Pedido;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.repository.DetallePedidoRepository;
import pe.edu.upeu.pollogordo_api.repository.PagoRepository;
import pe.edu.upeu.pollogordo_api.repository.PedidoRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PagoServiceImpl implements IPagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final EvidenciaPagoService evidenciaPagoService;

    private static final Set<String> METODOS =
            Set.of("YAPE", "PLIN", "TRANSFERENCIA", "EFECTIVO");

    public PagoServiceImpl(
            PagoRepository pagoRepository,
            PedidoRepository pedidoRepository,
            DetallePedidoRepository detallePedidoRepository,
            EvidenciaPagoService evidenciaPagoService) {

        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.evidenciaPagoService = evidenciaPagoService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> listarTodo() {
        return pagoRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoDTO> buscarPorId(Long id) {
        return pagoRepository.findById(id)
                .map(this::convertirDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> listarPorPedido(Long idPedido) {
        buscarPedido(idPedido);

        return pagoRepository
                .findByPedido_IdPedidoOrderByFechaPagoDescIdPagoDesc(idPedido)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenPagoDTO obtenerResumen(Long idPedido) {
        buscarPedido(idPedido);

        double total = redondear(
                detallePedidoRepository.calcularTotalPedido(idPedido)
        );

        double abonado = redondear(
                pagoRepository.totalAbonadoPorPedido(idPedido)
        );

        double saldo = redondear(Math.max(0, total - abonado));

        String estado;

        if (abonado <= 0) {
            estado = "PENDIENTE";
        } else if (saldo > 0) {
            estado = "PARCIAL";
        } else {
            estado = "PAGADO";
        }

        return new ResumenPagoDTO(
                idPedido, total, abonado, saldo, estado
        );
    }

    @Override
    @Transactional
    public PagoDTO crear(PagoDTO dto) {
        Pedido pedido = buscarPedidoConBloqueo(dto.idPedido());

        validarPedidoConfirmado(pedido);
        validarMetodo(dto.metodo());
        validarMonto(dto.monto());

        double disponible = calcularDisponible(dto.idPedido(), 0);

        if (redondear(dto.monto()) > disponible) {
            throw new IllegalArgumentException(
                    "El abono supera el saldo disponible de S/ " + disponible
            );
        }

        Pago pago = new Pago();
        asignarDatos(pago, dto, pedido);
        pago.setEstado("REGISTRADO");

        return convertirDTO(pagoRepository.save(pago));
    }

    @Override
    @Transactional
    public Optional<PagoDTO> actualizar(Long id, PagoDTO dto) {
        Pago existente = pagoRepository.findById(id).orElse(null);

        if (existente == null) {
            return Optional.empty();
        }

        Long idPedido = existente.getPedido().getIdPedido();
        Pedido pedido = buscarPedidoConBloqueo(idPedido);

        Pago pago = buscarPago(id);

        if (!idPedido.equals(dto.idPedido())) {
            throw new IllegalArgumentException(
                    "No se puede cambiar el pedido de un pago"
            );
        }

        validarPedidoConfirmado(pedido);
        validarMetodo(dto.metodo());
        validarMonto(dto.monto());

        if (!"REGISTRADO".equalsIgnoreCase(pago.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se pueden modificar pagos registrados"
            );
        }

        double disponible = calcularDisponible(idPedido, pago.getMonto());

        if (redondear(dto.monto()) > disponible) {
            throw new IllegalArgumentException(
                    "El monto supera el saldo disponible de S/ " + disponible
            );
        }

        if (pago.getEvidencia() != null &&
                !pago.getMetodo().equalsIgnoreCase(dto.metodo().trim())) {
            throw new IllegalArgumentException(
                    "No puedes cambiar el método de un pago con comprobante"
            );
        }

        asignarDatos(pago, dto, pedido);
        pago.setEstado("REGISTRADO");

        return Optional.of(
                convertirDTO(pagoRepository.save(pago))
        );
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {
        Pago existente = pagoRepository.findById(id).orElse(null);

        if (existente == null) {
            return false;
        }

        buscarPedidoConBloqueo(existente.getPedido().getIdPedido());

        Pago pago = buscarPago(id);

        if ("ANULADO".equalsIgnoreCase(pago.getEstado())) {
            return true;
        }

        pago.setEstado("ANULADO");
        pagoRepository.save(pago);

        return true;
    }

    @Override
    @Transactional
    public PagoDTO subirEvidencia(Long id, MultipartFile archivo)
            throws IOException {

        Pago existente = buscarPago(id);

        buscarPedidoConBloqueo(
                existente.getPedido().getIdPedido()
        );

        Pago pago = buscarPago(id);

        if (!"REGISTRADO".equalsIgnoreCase(pago.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se pueden adjuntar comprobantes a pagos registrados"
            );
        }

        if (pago.getEvidencia() != null &&
                !pago.getEvidencia().isBlank()) {
            throw new IllegalArgumentException(
                    "Este pago ya tiene un comprobante"
            );
        }

        String nombre = evidenciaPagoService.guardar(archivo);

        pago.setEvidencia(nombre);

        try {
            return convertirDTO(pagoRepository.saveAndFlush(pago));
        } catch (RuntimeException e) {
            evidenciaPagoService.eliminar(nombre);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Path obtenerEvidencia(Long id) {
        Pago pago = buscarPago(id);

        if (pago.getEvidencia() == null ||
                pago.getEvidencia().isBlank()) {
            throw new IllegalArgumentException(
                    "Este pago no tiene comprobante"
            );
        }

        return evidenciaPagoService.obtener(pago.getEvidencia());
    }

    private double calcularDisponible(Long idPedido, double montoActual) {
        double total = redondear(
                detallePedidoRepository.calcularTotalPedido(idPedido)
        );

        double abonado = redondear(
                pagoRepository.totalAbonadoPorPedido(idPedido)
        );

        double pendiente = redondear(
                pagoRepository.totalPendienteVerificacion(idPedido)
        );

        return redondear(Math.max(
                0, total - abonado - pendiente + montoActual
        ));
    }

    private Pedido buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pedido ID: " + id + " no existe"
                        )
                );
    }

    private Pedido buscarPedidoConBloqueo(Long id) {
        return pedidoRepository.buscarPorIdConBloqueo(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pedido ID: " + id + " no existe"
                        )
                );
    }

    private Pago buscarPago(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pago ID: " + id + " no existe"
                        )
                );
    }

    private void validarPedidoConfirmado(Pedido pedido) {
        if (!"S".equalsIgnoreCase(pedido.getConfirmado())) {
            throw new IllegalArgumentException(
                    "Solo se pueden registrar pagos de pedidos confirmados"
            );
        }

        if ("CANCELADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new IllegalArgumentException(
                    "No se pueden registrar pagos de pedidos cancelados"
            );
        }
    }

    private void validarMetodo(String metodo) {
        if (metodo == null ||
                !METODOS.contains(metodo.trim().toUpperCase())) {
            throw new IllegalArgumentException(
                    "Método permitido: YAPE, PLIN, TRANSFERENCIA o EFECTIVO"
            );
        }
    }

    private void validarMonto(Double monto) {
        if (monto == null ||
                !Double.isFinite(monto) ||
                monto < 0.01 ||
                redondear(monto) <= 0 ||
                Math.abs(monto - redondear(monto)) > 0.000001) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor a cero y tener máximo 2 decimales"
            );
        }
    }

    private void asignarDatos(Pago pago, PagoDTO dto, Pedido pedido) {
        pago.setPedido(pedido);
        pago.setMetodo(dto.metodo().trim().toUpperCase());
        pago.setMonto(redondear(dto.monto()));

        pago.setFechaPago(
                dto.fechaPago() != null
                        ? dto.fechaPago()
                        : LocalDate.now()
        );

        pago.setReferencia(dto.referencia());
    }

    private PagoDTO convertirDTO(Pago pago) {
        return new PagoDTO(
                pago.getIdPago(),
                pago.getPedido().getIdPedido(),
                pago.getMetodo(),
                pago.getMonto(),
                pago.getFechaPago(),
                pago.getReferencia(),
                pago.getEstado(),
                pago.getEvidencia()
        );
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
