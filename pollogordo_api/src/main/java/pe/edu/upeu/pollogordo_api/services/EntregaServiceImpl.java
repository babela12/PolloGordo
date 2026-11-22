package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.EntregaDTO;
import pe.edu.upeu.pollogordo_api.entity.AsigTrabajador;
import pe.edu.upeu.pollogordo_api.entity.DetallePedido;
import pe.edu.upeu.pollogordo_api.entity.Entrega;
import pe.edu.upeu.pollogordo_api.entity.Pedido;
import pe.edu.upeu.pollogordo_api.entity.Trabajador;
import pe.edu.upeu.pollogordo_api.entity.Vehiculo;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.repository.AsigTrabajadorRepository;
import pe.edu.upeu.pollogordo_api.repository.DetallePedidoRepository;
import pe.edu.upeu.pollogordo_api.repository.EntregaRepository;
import pe.edu.upeu.pollogordo_api.repository.PedidoRepository;
import pe.edu.upeu.pollogordo_api.repository.TrabajadorRepository;
import pe.edu.upeu.pollogordo_api.repository.VehiculoRepository;
import java.util.List;
import java.util.Optional;

@Service
public class EntregaServiceImpl implements IEntregaService {
    private final EntregaRepository entregaRepository;
    private final PedidoRepository pedidoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final AsigTrabajadorRepository asigTrabajadorRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public EntregaServiceImpl(EntregaRepository entregaRepository,
                              PedidoRepository pedidoRepository,
                              VehiculoRepository vehiculoRepository,
                              TrabajadorRepository trabajadorRepository,
                              AsigTrabajadorRepository asigTrabajadorRepository,
                              DetallePedidoRepository detallePedidoRepository) {
        this.entregaRepository = entregaRepository;
        this.pedidoRepository = pedidoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.asigTrabajadorRepository = asigTrabajadorRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntregaDTO> listarTodo() {
        return entregaRepository.findAll().stream().map(this::convertirDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntregaDTO> buscarPorId(Long id) {
        return entregaRepository.findById(id).map(this::convertirDTO);
    }

    @Override
    @Transactional
    public EntregaDTO crear(EntregaDTO dto) {
        Pedido pedido = buscarPedido(dto.idPedido());
        Vehiculo vehiculo = buscarVehiculo(dto.idVehiculo());
        Trabajador trabajador = buscarTrabajador(dto.idTrabajador());
        validarEntrega(dto, pedido, vehiculo, trabajador, null);
        Entrega entrega = new Entrega();
        asignarDatos(entrega, dto, pedido, vehiculo, trabajador);
        return convertirDTO(entregaRepository.save(entrega));
    }

    @Override
    @Transactional
    public Optional<EntregaDTO> actualizar(Long id, EntregaDTO dto) {
        return entregaRepository.findById(id).map(entrega -> {
            Pedido pedido = buscarPedido(dto.idPedido());
            Vehiculo vehiculo = buscarVehiculo(dto.idVehiculo());
            Trabajador trabajador = buscarTrabajador(dto.idTrabajador());
            validarEntrega(dto, pedido, vehiculo, trabajador, id);
            asignarDatos(entrega, dto, pedido, vehiculo, trabajador);
            return convertirDTO(entregaRepository.save(entrega));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {
        if (!entregaRepository.existsById(id)) {
            return false;
        }
        entregaRepository.deleteById(id);
        return true;
    }

    private Pedido buscarPedido(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El pedido es obligatorio");
        }
        return pedidoRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Pedido ID: " + id + " no existe"));
    }

    private Vehiculo buscarVehiculo(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El vehículo es obligatorio");
        }
        return vehiculoRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Vehículo ID: " + id + " no existe"));
    }

    private Trabajador buscarTrabajador(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El trabajador es obligatorio");
        }
        return trabajadorRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Trabajador ID: " + id + " no existe"));
    }

    private void validarEntrega(EntregaDTO dto, Pedido pedido, Vehiculo vehiculo,
                                Trabajador trabajador, Long idEntregaActual) {
        if (dto.fechaEntrega() == null || pedido.getFechaEvento() == null ||
                !dto.fechaEntrega().equals(pedido.getFechaEvento())) {
            throw new IllegalArgumentException("La entrega debe realizarse el día del evento");
        }
        if (dto.cantidadPollos() == null || dto.cantidadPollos() <= 0) {
            throw new IllegalArgumentException("La cantidad de pollos debe ser mayor a cero");
        }
        if (vehiculo.getCapacidad() == null || dto.cantidadPollos() > vehiculo.getCapacidad()) {
            throw new IllegalArgumentException("La cantidad de pollos supera la capacidad del vehículo");
        }
        if (!Boolean.TRUE.equals(trabajador.getPuedeRepartir())) {
            throw new IllegalArgumentException("El trabajador seleccionado no puede repartir");
        }
        boolean cancelada = "CANCELADO".equalsIgnoreCase(dto.estado());
        if (!cancelada) {
            boolean repartidorAsignado = false;
            for (AsigTrabajador asignacion : asigTrabajadorRepository.findByPedido_IdPedido(dto.idPedido())) {
                if (asignacion.getTrabajador().getIdTrabajador().equals(dto.idTrabajador()) &&
                        "REPARTIDOR".equals(asignacion.getFuncion()) &&
                        !"CANCELADO".equalsIgnoreCase(asignacion.getEstado())) {
                    repartidorAsignado = true;
                    break;
                }
            }
            if (!repartidorAsignado) {
                throw new IllegalArgumentException("El trabajador debe estar asignado como REPARTIDOR a este pedido");
            }
        }
        int totalPedido = 0;
        for (DetallePedido detalle : detallePedidoRepository.findByPedidoIdPedido(dto.idPedido())) {
            if (detalle.getCantidadPollos() != null) {
                totalPedido += detalle.getCantidadPollos();
            }
        }
        if (totalPedido <= 0) {
            throw new IllegalArgumentException("El pedido no tiene pollos registrados");
        }
        int totalAsignado = cancelada ? 0 : dto.cantidadPollos();
        for (Entrega entrega : entregaRepository.findAll()) {
            if (idEntregaActual != null && idEntregaActual.equals(entrega.getIdEntrega())) {
                continue;
            }
            if ("CANCELADO".equalsIgnoreCase(entrega.getEstado())) {
                continue;
            }
            if (entrega.getPedido().getIdPedido().equals(dto.idPedido()) &&
                    entrega.getCantidadPollos() != null) {
                totalAsignado += entrega.getCantidadPollos();
            }
            if (!cancelada && dto.fechaEntrega().equals(entrega.getFechaEntrega())) {
                if (entrega.getVehiculo().getIdVehiculo().equals(dto.idVehiculo())) {
                    throw new IllegalArgumentException("El vehículo ya tiene una entrega en esa fecha");
                }
                if (entrega.getTrabajador().getIdTrabajador().equals(dto.idTrabajador())) {
                    throw new IllegalArgumentException("El repartidor ya tiene una entrega en esa fecha");
                }
            }
        }
        if (totalAsignado > totalPedido) {
            throw new IllegalArgumentException("Las entregas superan los " + totalPedido +
                    " pollos del pedido. Total asignado: " + totalAsignado);
        }
    }

    private void asignarDatos(Entrega entrega, EntregaDTO dto, Pedido pedido,
                              Vehiculo vehiculo, Trabajador trabajador) {
        entrega.setPedido(pedido);
        entrega.setVehiculo(vehiculo);
        entrega.setTrabajador(trabajador);
        entrega.setFechaEntrega(dto.fechaEntrega());
        entrega.setCantidadPollos(dto.cantidadPollos());
        entrega.setEstado(dto.estado() == null ? "PROGRAMADO" : dto.estado());
        entrega.setObservacion(dto.observacion());
    }
    private EntregaDTO convertirDTO(Entrega entrega) {
        return new EntregaDTO(
                entrega.getIdEntrega(),
                entrega.getPedido().getIdPedido(),
                entrega.getVehiculo().getIdVehiculo(),
                entrega.getTrabajador().getIdTrabajador(),
                entrega.getFechaEntrega(),
                entrega.getHoraSalida(),
                entrega.getHoraLlegada(),
                entrega.getCantidadPollos(),
                entrega.getEstado(),
                entrega.getObservacion()
        );
    }

}