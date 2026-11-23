
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

import java.time.LocalTime;
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

    public EntregaServiceImpl(
            EntregaRepository entregaRepository,
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
        return entregaRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntregaDTO> buscarPorId(Long id) {
        return entregaRepository.findById(id)
                .map(this::convertirDTO);
    }

    // CREAR ENTREGA
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

    // ACTUALIZAR ENTREGA
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

    // ELIMINAR ENTREGA
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
            throw new IllegalArgumentException(
                    "El pedido es obligatorio"
            );
        }

        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pedido ID: " + id + " no existe"
                        )
                );
    }

    private Vehiculo buscarVehiculo(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El vehículo es obligatorio"
            );
        }

        return vehiculoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehículo ID: " + id + " no existe"
                        )
                );
    }

    private Trabajador buscarTrabajador(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El trabajador es obligatorio"
            );
        }

        return trabajadorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trabajador ID: " + id + " no existe"
                        )
                );
    }

    // VALIDAR ENTREGA
    private void validarEntrega(
            EntregaDTO dto,
            Pedido pedido,
            Vehiculo vehiculo,
            Trabajador trabajador,
            Long idEntregaActual) {

        // VALIDAR FECHA
        if (dto.fechaEntrega() == null
                || pedido.getFechaEvento() == null
                || !dto.fechaEntrega().equals(pedido.getFechaEvento())) {

            throw new IllegalArgumentException(
                    "La entrega debe realizarse el día del evento"
            );
        }

        // VALIDAR HORARIOS
        LocalTime horaSalida = dto.horaSalida();
        LocalTime horaLlegada = dto.horaLlegada();

        if (horaSalida == null || horaLlegada == null) {
            throw new IllegalArgumentException(
                    "Debe ingresar la hora de salida y llegada"
            );
        }

        if (!horaSalida.isBefore(horaLlegada)) {
            throw new IllegalArgumentException(
                    "La hora de llegada debe ser posterior a la salida"
            );
        }

        // VALIDAR CANTIDAD
        if (dto.cantidadPollos() == null
                || dto.cantidadPollos() <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad de pollos debe ser mayor a cero"
            );
        }

        // VALIDAR CAPACIDAD DEL VEHÍCULO
        if (vehiculo.getCapacidad() == null
                || dto.cantidadPollos() > vehiculo.getCapacidad()) {

            throw new IllegalArgumentException(
                    "La cantidad de pollos supera la capacidad del vehículo"
            );
        }

        // VALIDAR REPARTIDOR
        if (!Boolean.TRUE.equals(trabajador.getPuedeRepartir())) {

            throw new IllegalArgumentException(
                    "El trabajador seleccionado no puede repartir"
            );
        }

        boolean cancelada =
                "CANCELADO".equalsIgnoreCase(dto.estado());

        // VALIDAR ASIGNACIÓN DEL REPARTIDOR
        if (!cancelada) {

            boolean repartidorAsignado = false;

            for (AsigTrabajador asignacion :
                    asigTrabajadorRepository.findByPedido_IdPedido(
                            dto.idPedido())) {

                if (asignacion.getTrabajador()
                        .getIdTrabajador()
                        .equals(dto.idTrabajador())
                        && "REPARTIDOR".equalsIgnoreCase(
                        asignacion.getFuncion())
                        && !"CANCELADO".equalsIgnoreCase(
                        asignacion.getEstado())) {

                    repartidorAsignado = true;
                    break;
                }
            }

            if (!repartidorAsignado) {

                throw new IllegalArgumentException(
                        "El trabajador debe estar asignado como REPARTIDOR a este pedido"
                );
            }
        }

        // CALCULAR TOTAL DE POLLOS DEL PEDIDO
        int totalPedido = 0;

        for (DetallePedido detalle :
                detallePedidoRepository.findByPedidoIdPedido(
                        dto.idPedido())) {

            if (detalle.getCantidadPollos() != null) {
                totalPedido += detalle.getCantidadPollos();
            }
        }

        if (totalPedido <= 0) {

            throw new IllegalArgumentException(
                    "El pedido no tiene pollos registrados"
            );
        }

        int totalAsignado =
                cancelada ? 0 : dto.cantidadPollos();

        // REVISAR ENTREGAS EXISTENTES
        for (Entrega entrega : entregaRepository.findAll()) {

            // IGNORAR LA MISMA ENTREGA AL EDITAR
            if (idEntregaActual != null
                    && idEntregaActual.equals(
                    entrega.getIdEntrega())) {
                continue;
            }

            // IGNORAR ENTREGAS CANCELADAS
            if ("CANCELADO".equalsIgnoreCase(
                    entrega.getEstado())) {
                continue;
            }

            // SUMAR POLLOS DEL MISMO PEDIDO
            if (entrega.getPedido()
                    .getIdPedido()
                    .equals(dto.idPedido())
                    && entrega.getCantidadPollos() != null) {

                totalAsignado += entrega.getCantidadPollos();
            }

            // VALIDAR CRUCE DE HORARIOS
            if (!cancelada
                    && dto.fechaEntrega()
                    .equals(entrega.getFechaEntrega())) {

                LocalTime salidaExistente =
                        entrega.getHoraSalida();

                LocalTime llegadaExistente =
                        entrega.getHoraLlegada();

                if (salidaExistente == null
                        || llegadaExistente == null) {

                    boolean mismoVehiculo =
                            entrega.getVehiculo()
                                    .getIdVehiculo()
                                    .equals(dto.idVehiculo());

                    boolean mismoRepartidor =
                            entrega.getTrabajador()
                                    .getIdTrabajador()
                                    .equals(dto.idTrabajador());

                    if (mismoVehiculo || mismoRepartidor) {
                        throw new IllegalArgumentException(
                                "Existe una entrega sin horarios registrados. " +
                                        "Actualice sus horarios antes de asignar " +
                                        "el mismo vehículo o repartidor"
                        );
                    }

                    continue;
                }

                // HAY CRUCE SI AMBOS INTERVALOS SE SUPERPONEN
                boolean cruceHorario =
                        horaSalida.isBefore(llegadaExistente)
                                && horaLlegada.isAfter(salidaExistente);

                if (cruceHorario) {

                    // VEHÍCULO OCUPADO
                    if (entrega.getVehiculo()
                            .getIdVehiculo()
                            .equals(dto.idVehiculo())) {

                        throw new IllegalArgumentException(
                                "El vehículo ya tiene una entrega " +
                                        "en ese horario"
                        );
                    }

                    // REPARTIDOR OCUPADO
                    if (entrega.getTrabajador()
                            .getIdTrabajador()
                            .equals(dto.idTrabajador())) {

                        throw new IllegalArgumentException(
                                "El repartidor ya tiene una entrega " +
                                        "en ese horario"
                        );
                    }
                }
            }
        }

        // VALIDAR TOTAL ASIGNADO
        if (totalAsignado > totalPedido) {

            throw new IllegalArgumentException(
                    "Las entregas superan los " +
                            totalPedido +
                            " pollos del pedido. Total asignado: " +
                            totalAsignado
            );
        }
    }

    // GUARDAR DATOS DE LA ENTREGA
    private void asignarDatos(
            Entrega entrega,
            EntregaDTO dto,
            Pedido pedido,
            Vehiculo vehiculo,
            Trabajador trabajador) {

        entrega.setPedido(pedido);
        entrega.setVehiculo(vehiculo);
        entrega.setTrabajador(trabajador);

        entrega.setFechaEntrega(dto.fechaEntrega());

        // GUARDAR HORARIOS
        entrega.setHoraSalida(dto.horaSalida());
        entrega.setHoraLlegada(dto.horaLlegada());

        entrega.setCantidadPollos(dto.cantidadPollos());

        entrega.setEstado(
                dto.estado() == null
                        ? "PROGRAMADO"
                        : dto.estado()
        );

        entrega.setObservacion(dto.observacion());
    }

    // CONVERTIR A DTO
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
