
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.ConsolidadoDTO;
import pe.edu.upeu.pollogordo_api.dto.DetalleResponseDTO;
import pe.edu.upeu.pollogordo_api.dto.ItemPedidoDTO;
import pe.edu.upeu.pollogordo_api.dto.PedidoDTO;
import pe.edu.upeu.pollogordo_api.dto.PedidoResponseDTO;
import pe.edu.upeu.pollogordo_api.entity.Cliente;
import pe.edu.upeu.pollogordo_api.entity.DetallePedido;
import pe.edu.upeu.pollogordo_api.entity.Distribucion;
import pe.edu.upeu.pollogordo_api.entity.Insumo;
import pe.edu.upeu.pollogordo_api.entity.Pedido;
import pe.edu.upeu.pollogordo_api.entity.Producto;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.repository.ClienteRepository;
import pe.edu.upeu.pollogordo_api.repository.DetallePedidoRepository;
import pe.edu.upeu.pollogordo_api.repository.DistribucionRepository;
import pe.edu.upeu.pollogordo_api.repository.InsumoRepository;
import pe.edu.upeu.pollogordo_api.repository.PedidoRepository;
import pe.edu.upeu.pollogordo_api.repository.ProductoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoServiceImpl implements IPedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final DistribucionRepository distribucionRepository;
    private final InsumoRepository insumoRepository;
    private final IDistribucionService distribucionService;

    public PedidoServiceImpl(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            ClienteRepository clienteRepository,
            DetallePedidoRepository detallePedidoRepository,
            DistribucionRepository distribucionRepository,
            InsumoRepository insumoRepository,
            IDistribucionService distribucionService) {

        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.distribucionRepository = distribucionRepository;
        this.insumoRepository = insumoRepository;
        this.distribucionService = distribucionService;
    }

    @Override
    @Transactional
    public PedidoResponseDTO realizarPedido(PedidoDTO dto) {

        Cliente cliente = buscarCliente(dto.idCliente());

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setFechaPedido(LocalDate.now());
        pedido.setFechaEvento(dto.fechaEvento());
        pedido.setHoraEntrega(dto.horaEntrega());
        pedido.setDireccionEvento(dto.direccionEvento().trim());
        pedido.setTelefonoContacto(dto.telefonoContacto());
        pedido.setConfirmado("N");
        pedido.setEstado("P");
        pedido.setObservacion(dto.observacion());

        pedidoRepository.save(pedido);

        List<DetallePedido> detalles =
                guardarDetalles(pedido, dto.items());

        distribucionService.generarDistribucion(pedido, dto.items());

        return convertirAResponse(pedido, detalles);
    }

    @Override
    @Transactional
    public PedidoResponseDTO confirmarPedido(Long idPedido) {

        Pedido pedido = buscarPedido(idPedido);

        if (!"S".equalsIgnoreCase(pedido.getConfirmado())) {
            pedido.setConfirmado("S");
            pedido.setFechaConfirmacion(LocalDate.now());
        }

        pedidoRepository.save(pedido);

        List<DetallePedido> detalles =
                detallePedidoRepository.findByPedidoIdPedido(idPedido);

        return convertirAResponse(pedido, detalles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodo() {

        List<PedidoResponseDTO> lista = new ArrayList<>();

        for (Pedido pedido : pedidoRepository.findAll()) {

            List<DetallePedido> detalles =
                    detallePedidoRepository.findByPedidoIdPedido(
                            pedido.getIdPedido());

            lista.add(convertirAResponse(pedido, detalles));
        }

        return lista;
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(Long idPedido) {

        Pedido pedido = buscarPedido(idPedido);

        List<DetallePedido> detalles =
                detallePedidoRepository.findByPedidoIdPedido(idPedido);

        return convertirAResponse(pedido, detalles);
    }

    @Override
    @Transactional
    public PedidoResponseDTO modificarPedido(Long idPedido, PedidoDTO dto) {

        Pedido pedido = buscarPedido(idPedido);
        Cliente cliente = buscarCliente(dto.idCliente());

        List<Distribucion> distribucionesAnteriores =
                distribucionRepository.findByPedidoIdPedido(idPedido);

        restaurarStock(distribucionesAnteriores);
        distribucionRepository.deleteAll(distribucionesAnteriores);

        List<DetallePedido> detallesAnteriores =
                detallePedidoRepository.findByPedidoIdPedido(idPedido);

        detallePedidoRepository.deleteAll(detallesAnteriores);

        pedido.setCliente(cliente);
        pedido.setFechaEvento(dto.fechaEvento());
        pedido.setHoraEntrega(dto.horaEntrega());
        pedido.setDireccionEvento(dto.direccionEvento().trim());
        pedido.setTelefonoContacto(dto.telefonoContacto());
        pedido.setObservacion(dto.observacion());

        pedidoRepository.save(pedido);

        List<DetallePedido> detalles =
                guardarDetalles(pedido, dto.items());

        distribucionService.generarDistribucion(pedido, dto.items());

        return convertirAResponse(pedido, detalles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsolidadoDTO> obtenerConsolidado(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (fechaInicio == null || fechaFin == null
                || fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "El rango de fechas no es válido");
        }

        List<Distribucion> distribuciones =
                distribucionRepository.findAll();

        List<ConsolidadoDTO> consolidado = new ArrayList<>();

        for (Insumo insumo : insumoRepository.findAll()) {

            double total = 0;

            for (Distribucion distribucion : distribuciones) {

                LocalDate fecha =
                        distribucion.getPedido().getFechaPedido();

                if (distribucion.getInsumo().getIdInsumo()
                        .equals(insumo.getIdInsumo())
                        && !fecha.isBefore(fechaInicio)
                        && !fecha.isAfter(fechaFin)) {

                    total += distribucion.getCantidad();
                }
            }

            if (total > 0) {
                consolidado.add(new ConsolidadoDTO(
                        insumo.getNombre(),
                        total,
                        insumo.getUnidad()
                ));
            }
        }

        return consolidado;
    }

    private Cliente buscarCliente(Long idCliente) {

        Optional<Cliente> resultado =
                clienteRepository.findById(idCliente);

        if (resultado.isPresent()) {
            return resultado.get();
        }

        throw new ResourceNotFoundException(
                "Cliente no encontrado");
    }

    private Pedido buscarPedido(Long idPedido) {

        Optional<Pedido> resultado =
                pedidoRepository.findById(idPedido);

        if (resultado.isPresent()) {
            return resultado.get();
        }

        throw new ResourceNotFoundException(
                "Pedido ID: " + idPedido + " no encontrado");
    }

    private List<DetallePedido> guardarDetalles(
            Pedido pedido,
            List<ItemPedidoDTO> items) {

        List<DetallePedido> detalles = new ArrayList<>();

        for (ItemPedidoDTO item : items) {

            Optional<Producto> resultado =
                    productoRepository.findById(item.idProducto());

            if (resultado.isEmpty()) {
                throw new ResourceNotFoundException(
                        "Producto ID: " + item.idProducto() + " no existe");
            }

            Producto producto = resultado.get();

            String unidad = item.unidad().trim().toUpperCase();
            validarUnidad(unidad);

            double cantidad = item.cantidad();

            if (!Double.isFinite(cantidad) || cantidad <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor a cero");
            }

            double pollos = calcularPollos(
                    cantidad, unidad, producto);

            String acompanamiento =
                    normalizarOpcional(item.acompanamiento());

            String tipoArroz =
                    normalizarOpcional(item.tipoArroz());

            String tipoEnsalada =
                    normalizarOpcional(item.tipoEnsalada());

            validarAdicionales(tipoArroz, tipoEnsalada);

            double subtotal;

            if (pollos > 0) {
                subtotal = producto.getPrecio() * pollos;
            } else {
                subtotal = producto.getPrecio() * cantidad;
            }

            if (pollos > 0) {

                double porciones =
                        calcularPorciones(cantidad, unidad);

                subtotal += calcularPrecioAdicionales(
                        tipoArroz, tipoEnsalada, porciones);

            } else if (tipoArroz != null || tipoEnsalada != null) {

                throw new IllegalArgumentException(
                        "Los adicionales solo se permiten en productos preparados");
            }

            subtotal = Math.round(subtotal * 100.0) / 100.0;

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setUnidad(unidad);
            detalle.setAcompanamiento(acompanamiento);
            detalle.setTipoArroz(tipoArroz);
            detalle.setTipoEnsalada(tipoEnsalada);
            detalle.setCantidadPollos((int) Math.ceil(pollos));
            detalle.setPrecio(producto.getPrecio());
            detalle.setSubtotal(subtotal);

            detalles.add(detallePedidoRepository.save(detalle));
        }

        return detalles;
    }

    private String normalizarOpcional(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim().toUpperCase();
    }

    private void validarAdicionales(
            String tipoArroz,
            String tipoEnsalada) {

        if (tipoArroz != null
                && !tipoArroz.equals("ARABE")
                && !tipoArroz.equals("JARDINERA")) {

            throw new IllegalArgumentException(
                    "El arroz debe ser ARABE o JARDINERA");
        }

        if (tipoEnsalada != null
                && !tipoEnsalada.equals("BLANCA")
                && !tipoEnsalada.equals("FRESCA")) {

            throw new IllegalArgumentException(
                    "La ensalada debe ser BLANCA o FRESCA");
        }
    }

    private double calcularPorciones(
            double cantidad,
            String unidad) {

        switch (unidad) {
            case "POLLO":
                return cantidad * 4.0;

            case "CUARTO":
                return cantidad;

            case "OCTAVO":
                return cantidad;

            default:
                throw new IllegalArgumentException(
                        "Unidad no válida: " + unidad);
        }
    }

    private double calcularPrecioAdicionales(
            String tipoArroz,
            String tipoEnsalada,
            double porciones) {

        double precioPorPorcion = 0;

        if ("ARABE".equals(tipoArroz)) {
            precioPorPorcion += 2.00;
        } else if ("JARDINERA".equals(tipoArroz)) {
            precioPorPorcion += 1.80;
        }

        if ("BLANCA".equals(tipoEnsalada)) {
            precioPorPorcion += 2.00;
        } else if ("FRESCA".equals(tipoEnsalada)) {
            precioPorPorcion += 1.80;
        }

        return precioPorPorcion * porciones;
    }

    private double calcularPollos(
            double cantidad,
            String unidad,
            Producto producto) {

        if (!"POLLO".equalsIgnoreCase(producto.getTipo())
                && !"CHULETA".equalsIgnoreCase(producto.getTipo())) {
            return 0;
        }

        if ("POLLO CRUDO".equalsIgnoreCase(producto.getNombre())) {
            return 0;
        }

        switch (unidad) {
            case "POLLO":
                return cantidad;

            case "CUARTO":
                return cantidad / 4.0;

            case "OCTAVO":
                return cantidad / 8.0;

            default:
                throw new IllegalArgumentException(
                        "Unidad no válida: " + unidad);
        }
    }

    private void validarUnidad(String unidad) {

        if (!unidad.equals("POLLO")
                && !unidad.equals("CUARTO")
                && !unidad.equals("OCTAVO")) {

            throw new IllegalArgumentException(
                    "La unidad debe ser POLLO, CUARTO u OCTAVO");
        }
    }

    private void restaurarStock(
            List<Distribucion> distribuciones) {

        for (Distribucion distribucion : distribuciones) {

            Insumo insumo = distribucion.getInsumo();

            if (!"S".equalsIgnoreCase(insumo.getControlaStock())) {
                continue;
            }

            Double stock = insumo.getCantidadStock();

            if (stock == null) {
                throw new IllegalStateException(
                        "El insumo " + insumo.getNombre()
                                + " no tiene stock registrado");
            }

            insumo.setCantidadStock(
                    stock + distribucion.getCantidad());

            insumoRepository.save(insumo);
        }
    }

    private PedidoResponseDTO convertirAResponse(
            Pedido pedido,
            List<DetallePedido> detalles) {

        double totalPedido = 0;
        List<DetalleResponseDTO> productosResponse =
                new ArrayList<>();

        for (DetallePedido detalle : detalles) {

            totalPedido += detalle.getSubtotal();

            DetalleResponseDTO producto =
                    new DetalleResponseDTO(
                            detalle.getProducto().getNombre(),
                            detalle.getCantidad(),
                            detalle.getUnidad(),
                            detalle.getCantidadPollos(),
                            detalle.getPrecio(),
                            detalle.getSubtotal(),
                            detalle.getAcompanamiento(),
                            detalle.getTipoArroz(),
                            detalle.getTipoEnsalada()
                    );

            productosResponse.add(producto);
        }

        return new PedidoResponseDTO(
                pedido.getIdPedido(),
                pedido.getCliente().getPersona().getNombre(),
                totalPedido,
                pedido.getEstado(),
                pedido.getFechaPedido(),
                pedido.getFechaEvento(),
                pedido.getHoraEntrega(),
                pedido.getDireccionEvento(),
                pedido.getTelefonoContacto(),
                pedido.getConfirmado(),
                pedido.getFechaConfirmacion(),
                productosResponse
        );
    }
}
