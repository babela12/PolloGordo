
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.pollogordo_api.dto.AsigTrabajadorDTO;
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

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AsigTrabajadorServiceImpl implements IAsigTrabajadorService {

    private final AsigTrabajadorRepository asigTrabajadorRepository;
    private final PedidoRepository pedidoRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final EntregaRepository entregaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public AsigTrabajadorServiceImpl(
            AsigTrabajadorRepository asigTrabajadorRepository,
            PedidoRepository pedidoRepository,
            TrabajadorRepository trabajadorRepository,
            EntregaRepository entregaRepository,
            VehiculoRepository vehiculoRepository,
            DetallePedidoRepository detallePedidoRepository) {

        this.asigTrabajadorRepository = asigTrabajadorRepository;
        this.pedidoRepository = pedidoRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.entregaRepository = entregaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsigTrabajadorDTO> listarTodo() {

        return asigTrabajadorRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsigTrabajadorDTO> buscarPorId(Long id) {

        return asigTrabajadorRepository.findById(id)
                .map(this::convertirDTO);
    }

    @Override
    @Transactional
    public AsigTrabajadorDTO crear(AsigTrabajadorDTO dto) {

        Pedido pedido = obtenerPedido(dto.idPedido());
        Trabajador trabajador = obtenerTrabajador(dto.idTrabajador());

        validarAsignacion(dto, trabajador, null);

        AsigTrabajador asignacion = new AsigTrabajador();

        asignarDatos(asignacion, dto, pedido, trabajador);

        return convertirDTO(asigTrabajadorRepository.save(asignacion));
    }

    @Override
    @Transactional
    public Optional<AsigTrabajadorDTO> actualizar(
            Long id, AsigTrabajadorDTO dto) {

        return asigTrabajadorRepository.findById(id).map(asignacion -> {

            Pedido pedido = obtenerPedido(dto.idPedido());
            Trabajador trabajador = obtenerTrabajador(dto.idTrabajador());

            validarAsignacion(dto, trabajador, id);

            asignarDatos(asignacion, dto, pedido, trabajador);

            return convertirDTO(asigTrabajadorRepository.save(asignacion));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (asigTrabajadorRepository.existsById(id)) {
            asigTrabajadorRepository.deleteById(id);
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public AsigTrabajadorDTO crearAsignacionCompleta(
            AsigTrabajadorDTO dto) {

        Pedido pedido = obtenerPedido(dto.idPedido());

        if (dto.trabajadores() == null ||
                dto.trabajadores().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar trabajadores");
        }

        if (dto.entregas() == null ||
                dto.entregas().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar vehículos y repartidores");
        }

        if (!asigTrabajadorRepository
                .findByPedido_IdPedido(dto.idPedido()).isEmpty()) {

            throw new IllegalArgumentException(
                    "El pedido ya tiene asignaciones registradas");
        }

        for (Entrega entrega : entregaRepository.findAll()) {

            if (entrega.getPedido().getIdPedido()
                    .equals(dto.idPedido())) {

                throw new IllegalArgumentException(
                        "El pedido ya tiene entregas registradas");
            }
        }

        List<DetallePedido> detalles =
                detallePedidoRepository.findByPedidoIdPedido(
                        dto.idPedido());

        int totalPollos = 0;

        for (DetallePedido detalle : detalles) {

            if (detalle.getCantidadPollos() != null) {
                totalPollos += detalle.getCantidadPollos();
            }
        }

        if (totalPollos <= 0) {
            throw new IllegalArgumentException(
                    "El pedido no tiene pollos registrados");
        }

        double pollosPreparadores = 0;
        int cantidadAsadores = 0;

        Set<Long> repartidores = new HashSet<>();
        Set<Long> vehiculos = new HashSet<>();

        // Validar todos los trabajadores antes de guardar
        for (AsigTrabajadorDTO trabajadorDTO : dto.trabajadores()) {

            if (trabajadorDTO.idPedido() != null &&
                    !trabajadorDTO.idPedido().equals(dto.idPedido())) {

                throw new IllegalArgumentException(
                        "Todos los trabajadores deben pertenecer al mismo pedido");
            }

            Trabajador trabajador =
                    obtenerTrabajador(trabajadorDTO.idTrabajador());

            validarAsignacion(trabajadorDTO, trabajador, null);

            if ("PREPARADOR".equals(trabajadorDTO.funcion())) {

                pollosPreparadores += trabajadorDTO.cantidadAsignada();
            }

            if ("ASADOR".equals(trabajadorDTO.funcion())) {
                cantidadAsadores++;
            }

            if ("REPARTIDOR".equals(trabajadorDTO.funcion())) {

                if (!repartidores.add(
                        trabajadorDTO.idTrabajador())) {

                    throw new IllegalArgumentException(
                            "El repartidor está repetido");
                }
            }
        }

        if (Math.abs(pollosPreparadores - totalPollos) > 0.0001) {

            throw new IllegalArgumentException(
                    "Los preparadores deben cubrir los "
                            + totalPollos + " pollos del pedido. "
                            + "Asignados: " + pollosPreparadores);
        }

        if (cantidadAsadores == 0) {

            throw new IllegalArgumentException(
                    "Debe asignar al menos un asador");
        }

        int pollosVehiculos = 0;
        Set<Long> repartidoresConVehiculo = new HashSet<>();

        // Validar vehículos y entregas
        for (EntregaDTO entregaDTO : dto.entregas()) {

            if (!dto.idPedido().equals(entregaDTO.idPedido())) {

                throw new IllegalArgumentException(
                        "La entrega pertenece a otro pedido");
            }

            if (entregaDTO.idVehiculo() == null ||
                    entregaDTO.idTrabajador() == null) {

                throw new IllegalArgumentException(
                        "Debe seleccionar vehículo y repartidor");
            }

            Vehiculo vehiculo = vehiculoRepository
                    .findById(entregaDTO.idVehiculo())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Vehículo no encontrado"));

            Trabajador trabajador =
                    obtenerTrabajador(entregaDTO.idTrabajador());

            if (!Boolean.TRUE.equals(
                    trabajador.getPuedeRepartir())) {

                throw new IllegalArgumentException(
                        "El trabajador no puede repartir");
            }

            if (!repartidores.contains(
                    entregaDTO.idTrabajador())) {

                throw new IllegalArgumentException(
                        "El repartidor de la entrega debe estar "
                                + "incluido en la lista de trabajadores");
            }

            if (!vehiculos.add(entregaDTO.idVehiculo())) {

                throw new IllegalArgumentException(
                        "No puede repetir un vehículo en el mismo pedido");
            }

            if (!repartidoresConVehiculo.add(
                    entregaDTO.idTrabajador())) {

                throw new IllegalArgumentException(
                        "No puede repetir un repartidor en las entregas");
            }

            if (entregaDTO.cantidadPollos() == null ||
                    entregaDTO.cantidadPollos() <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad de pollos debe ser mayor a cero");
            }

            if (vehiculo.getCapacidad() == null ||
                    entregaDTO.cantidadPollos() >
                            vehiculo.getCapacidad()) {

                throw new IllegalArgumentException(
                        "La cantidad de pollos supera la capacidad "
                                + "del vehículo " + vehiculo.getPlaca());
            }

            for (Entrega existente : entregaRepository.findAll()) {

                if ("CANCELADO".equalsIgnoreCase(
                        existente.getEstado())) {
                    continue;
                }

                if (entregaDTO.fechaEntrega() != null &&
                        entregaDTO.fechaEntrega().equals(
                                existente.getFechaEntrega())) {

                    if (existente.getVehiculo().getIdVehiculo()
                            .equals(entregaDTO.idVehiculo())) {

                        throw new IllegalArgumentException(
                                "El vehículo ya tiene una entrega "
                                        + "en esa fecha");
                    }

                    if (existente.getTrabajador().getIdTrabajador()
                            .equals(entregaDTO.idTrabajador())) {

                        throw new IllegalArgumentException(
                                "El repartidor ya tiene una entrega "
                                        + "en esa fecha");
                    }
                }
            }

            pollosVehiculos += entregaDTO.cantidadPollos();
        }

        if (pollosVehiculos != totalPollos) {

            throw new IllegalArgumentException(
                    "Los vehículos deben transportar "
                            + totalPollos + " pollos. "
                            + "Asignados: " + pollosVehiculos);
        }

        if (!repartidores.equals(repartidoresConVehiculo)) {

            throw new IllegalArgumentException(
                    "Cada repartidor asignado debe tener un vehículo");
        }

        // Guardar trabajadores
        List<AsigTrabajadorDTO> trabajadoresGuardados =
                new ArrayList<>();

        for (AsigTrabajadorDTO trabajadorDTO : dto.trabajadores()) {

            Trabajador trabajador =
                    obtenerTrabajador(trabajadorDTO.idTrabajador());

            AsigTrabajador asignacion = new AsigTrabajador();

            asignarDatos(
                    asignacion,
                    trabajadorDTO,
                    pedido,
                    trabajador
            );

            trabajadoresGuardados.add(
                    convertirDTO(
                            asigTrabajadorRepository.save(asignacion)
                    )
            );
        }

        // Guardar entregas
        List<EntregaDTO> entregasGuardadas = new ArrayList<>();

        for (EntregaDTO entregaDTO : dto.entregas()) {

            Vehiculo vehiculo = vehiculoRepository
                    .findById(entregaDTO.idVehiculo())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Vehículo no encontrado"));

            Trabajador trabajador =
                    obtenerTrabajador(entregaDTO.idTrabajador());

            Entrega entrega = new Entrega();

            entrega.setPedido(pedido);
            entrega.setVehiculo(vehiculo);
            entrega.setTrabajador(trabajador);
            entrega.setFechaEntrega(entregaDTO.fechaEntrega());
            entrega.setCantidadPollos(entregaDTO.cantidadPollos());
            entrega.setObservacion(entregaDTO.observacion());

            if (entregaDTO.estado() != null) {
                entrega.setEstado(entregaDTO.estado());
            } else {
                entrega.setEstado("PROGRAMADO");
            }

            Entrega guardada = entregaRepository.save(entrega);

            entregasGuardadas.add(new EntregaDTO(
                    guardada.getIdEntrega(),
                    pedido.getIdPedido(),
                    vehiculo.getIdVehiculo(),
                    trabajador.getIdTrabajador(),
                    guardada.getFechaEntrega(),
                    guardada.getCantidadPollos(),
                    guardada.getEstado(),
                    guardada.getObservacion()
            ));
        }

        return new AsigTrabajadorDTO(
                null,
                pedido.getIdPedido(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "PROGRAMADO",
                trabajadoresGuardados,
                entregasGuardadas
        );
    }

    private Pedido obtenerPedido(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El pedido es obligatorio");
        }

        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido ID: " + id + " no existe"));
    }

    private Trabajador obtenerTrabajador(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El trabajador es obligatorio");
        }

        return trabajadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trabajador ID: " + id + " no existe"));
    }

    private void asignarDatos(
            AsigTrabajador asignacion,
            AsigTrabajadorDTO dto,
            Pedido pedido,
            Trabajador trabajador) {

        asignacion.setPedido(pedido);
        asignacion.setTrabajador(trabajador);
        asignacion.setFuncion(dto.funcion());
        asignacion.setFechaTrabajo(dto.fechaTrabajo());
        asignacion.setHoraInicio(dto.horaInicio());
        asignacion.setHoraFin(dto.horaFin());
        asignacion.setCantidadAsignada(dto.cantidadAsignada());
        asignacion.setCantidadRealizada(dto.cantidadRealizada());
        asignacion.setTarifaAplicada(dto.tarifaAplicada());

        if (dto.pagoAdicional() != null) {
            asignacion.setPagoAdicional(dto.pagoAdicional());
        } else {
            asignacion.setPagoAdicional(BigDecimal.ZERO);
        }

        if (dto.estado() != null) {
            asignacion.setEstado(dto.estado());
        } else {
            asignacion.setEstado("PROGRAMADO");
        }
    }

    private void validarAsignacion(
            AsigTrabajadorDTO dto,
            Trabajador trabajador,
            Long idExcluir) {

        if (dto.fechaTrabajo() == null) {
            throw new IllegalArgumentException(
                    "La fecha de trabajo es obligatoria");
        }

        if (dto.funcion() == null) {
            throw new IllegalArgumentException(
                    "La función es obligatoria");
        }

        if (dto.horaFin() != null && dto.horaInicio() == null) {
            throw new IllegalArgumentException(
                    "Debe registrar la hora de inicio antes de la hora de fin");
        }

        if (dto.horaInicio() != null &&
                !dto.horaFin().isAfter(dto.horaInicio())) {

            throw new IllegalArgumentException(
                    "La hora de fin debe ser posterior a la de inicio");
        }

        if (dto.cantidadRealizada() != null &&
                dto.cantidadAsignada() != null &&
                dto.cantidadRealizada() >
                        dto.cantidadAsignada()) {

            throw new IllegalArgumentException(
                    "La cantidad realizada no puede superar "
                            + "la cantidad asignada");
        }

        Integer capacidad = null;

        switch (dto.funcion()) {

            case "PREPARADOR":
                capacidad = trabajador.getCapacidadPreparacion();
                break;

            case "ASADOR":
                capacidad = trabajador.getCapacidadAsado();
                break;

            case "REPARTIDOR":

                if (!Boolean.TRUE.equals(
                        trabajador.getPuedeRepartir())) {

                    throw new IllegalArgumentException(
                            "El trabajador no está habilitado "
                                    + "para realizar repartos");
                }

                break;

            default:
                throw new IllegalArgumentException(
                        "Función no válida");
        }

        if (!dto.funcion().equals("REPARTIDOR")) {

            if (capacidad == null || capacidad <= 0) {

                throw new IllegalArgumentException(
                        "El trabajador no tiene capacidad "
                                + "registrada para esta función");
            }

            if (dto.cantidadAsignada() == null ||
                    dto.cantidadAsignada() <= 0) {

                throw new IllegalArgumentException(
                        "Debe asignar una cantidad mayor que cero");
            }

            validarCapacidad(dto, trabajador, capacidad, idExcluir);
        }

        validarHorarios(dto, trabajador, idExcluir);
    }

    private void validarCapacidad(
            AsigTrabajadorDTO dto,
            Trabajador trabajador,
            Integer capacidad,
            Long idExcluir) {

        List<AsigTrabajador> asignaciones =
                asigTrabajadorRepository
                        .findByTrabajador_IdTrabajadorAndFechaTrabajoAndFuncion(
                                trabajador.getIdTrabajador(),
                                dto.fechaTrabajo(),
                                dto.funcion()
                        );

        double totalAsignado = 0;

        for (AsigTrabajador asignacion : asignaciones) {

            if (idExcluir != null &&
                    idExcluir.equals(asignacion.getIdAsig())) {
                continue;
            }

            if ("CANCELADO".equals(asignacion.getEstado())) {
                continue;
            }

            if (asignacion.getCantidadAsignada() != null) {
                totalAsignado += asignacion.getCantidadAsignada();
            }
        }

        double nuevoTotal =
                totalAsignado + dto.cantidadAsignada();

        if (nuevoTotal > capacidad) {

            throw new IllegalArgumentException(
                    "La asignación supera la capacidad diaria. "
                            + "Capacidad: " + capacidad
                            + ", ya asignados: " + totalAsignado
                            + ", nueva cantidad: "
                            + dto.cantidadAsignada());
        }
    }

    private void validarHorarios(
            AsigTrabajadorDTO dto,
            Trabajador trabajador,
            Long idExcluir) {

        LocalTime inicio = dto.horaInicio();
        LocalTime fin = dto.horaFin();

        if (inicio == null || fin == null) {
            return;
        }

        List<AsigTrabajador> asignaciones =
                asigTrabajadorRepository
                        .findByTrabajador_IdTrabajadorAndFechaTrabajo(
                                trabajador.getIdTrabajador(),
                                dto.fechaTrabajo()
                        );

        for (AsigTrabajador asignacion : asignaciones) {

            if (idExcluir != null &&
                    idExcluir.equals(asignacion.getIdAsig())) {
                continue;
            }

            if ("CANCELADO".equals(asignacion.getEstado())) {
                continue;
            }

            if (asignacion.getHoraInicio() == null ||
                    asignacion.getHoraFin() == null) {
                continue;
            }

            boolean seCruzan =
                    inicio.isBefore(asignacion.getHoraFin())
                            && fin.isAfter(asignacion.getHoraInicio());

            if (!seCruzan) {
                continue;
            }

            boolean mismaFuncion =
                    dto.funcion().equals(asignacion.getFuncion());

            boolean funcionDeProduccion =
                    "PREPARADOR".equals(dto.funcion())
                            || "ASADOR".equals(dto.funcion());

            if (mismaFuncion && funcionDeProduccion) {
                continue;
            }

            throw new IllegalArgumentException(
                    "El trabajador tiene otra asignación "
                            + "en el mismo horario. "
                            + "Función existente: "
                            + asignacion.getFuncion());
        }
    }

    private AsigTrabajadorDTO convertirDTO(
            AsigTrabajador asignacion) {

        return new AsigTrabajadorDTO(
                asignacion.getIdAsig(),
                asignacion.getPedido().getIdPedido(),
                asignacion.getTrabajador().getIdTrabajador(),
                asignacion.getFuncion(),
                asignacion.getFechaTrabajo(),
                asignacion.getHoraInicio(),
                asignacion.getHoraFin(),
                asignacion.getCantidadAsignada(),
                asignacion.getCantidadRealizada(),
                asignacion.getTarifaAplicada(),
                asignacion.getPagoAdicional(),
                asignacion.getEstado(),
                null,
                null
        );
    }
}
