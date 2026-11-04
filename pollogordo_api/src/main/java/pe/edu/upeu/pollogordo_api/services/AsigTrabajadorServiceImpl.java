
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
public class AsigTrabajadorServiceImpl
        implements IAsigTrabajadorService {

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

        return convertirDTO(
                asigTrabajadorRepository.save(asignacion));
    }

    @Override
    @Transactional
    public Optional<AsigTrabajadorDTO> actualizar(
            Long id, AsigTrabajadorDTO dto) {

        return asigTrabajadorRepository.findById(id)
                .map(asignacion -> {

                    Pedido pedido = obtenerPedido(dto.idPedido());
                    Trabajador trabajador =
                            obtenerTrabajador(dto.idTrabajador());

                    validarAsignacion(dto, trabajador, id);

                    asignarDatos(
                            asignacion, dto, pedido, trabajador);

                    return convertirDTO(
                            asigTrabajadorRepository.save(asignacion));
                });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (!asigTrabajadorRepository.existsById(id)) {
            return false;
        }

        asigTrabajadorRepository.deleteById(id);
        return true;
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

        if (entregaRepository.existsByPedido_IdPedido(dto.idPedido())) {
            throw new IllegalArgumentException(
                    "El pedido ya tiene entregas registradas");
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

        List<AsigTrabajadorDTO> revisados = new ArrayList<>();

        // Validar trabajadores
        for (AsigTrabajadorDTO trabajadorDTO : dto.trabajadores()) {

            if (trabajadorDTO.idPedido() != null &&
                    !trabajadorDTO.idPedido()
                            .equals(dto.idPedido())) {

                throw new IllegalArgumentException(
                        "Los trabajadores deben pertenecer al mismo pedido");
            }

            Trabajador trabajador =
                    obtenerTrabajador(trabajadorDTO.idTrabajador());

            validarAsignacion(trabajadorDTO, trabajador, null);

            // Revisar trabajadores dentro de la misma solicitud
            for (AsigTrabajadorDTO anterior : revisados) {

                if (!anterior.idTrabajador()
                        .equals(trabajadorDTO.idTrabajador())) {
                    continue;
                }

                if (!anterior.fechaTrabajo()
                        .equals(trabajadorDTO.fechaTrabajo())) {
                    continue;
                }

                boolean mismaFuncion =
                        anterior.funcion()
                                .equals(trabajadorDTO.funcion());

                if (mismaFuncion) {
                    throw new IllegalArgumentException(
                            "El trabajador está repetido "
                                    + "en la misma función y fecha");
                }

                if (horariosSeCruzan(
                        anterior.horaInicio(),
                        anterior.horaFin(),
                        trabajadorDTO.horaInicio(),
                        trabajadorDTO.horaFin())) {

                    throw new IllegalArgumentException(
                            "El trabajador tiene funciones "
                                    + "con horarios cruzados");
                }
            }

            revisados.add(trabajadorDTO);

            if ("PREPARADOR".equals(trabajadorDTO.funcion())) {

                pollosPreparadores +=
                        trabajadorDTO.cantidadAsignada();
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
                    "Los preparadores deben cubrir "
                            + totalPollos + " pollos. Asignados: "
                            + pollosPreparadores);
        }

        if (cantidadAsadores == 0) {
            throw new IllegalArgumentException(
                    "Debe asignar al menos un asador");
        }

        int pollosVehiculos = 0;

        Set<Long> repartidoresConVehiculo = new HashSet<>();

        // Validar entregas y vehículos
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

            if (entregaDTO.fechaEntrega() == null) {

                throw new IllegalArgumentException(
                        "La fecha de entrega es obligatoria");
            }

            Vehiculo vehiculo = vehiculoRepository
                    .findById(entregaDTO.idVehiculo())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
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
                        "El repartidor debe estar incluido "
                                + "en los trabajadores asignados");
            }

            if (!vehiculos.add(entregaDTO.idVehiculo())) {

                throw new IllegalArgumentException(
                        "El vehículo está repetido");
            }

            if (!repartidoresConVehiculo.add(
                    entregaDTO.idTrabajador())) {

                throw new IllegalArgumentException(
                        "El repartidor está repetido en las entregas");
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
                        "Se supera la capacidad del vehículo "
                                + vehiculo.getPlaca());
            }

            for (Entrega existente : entregaRepository.findAll()) {

                if ("CANCELADO".equalsIgnoreCase(
                        existente.getEstado())) {
                    continue;
                }

                if (!entregaDTO.fechaEntrega()
                        .equals(existente.getFechaEntrega())) {
                    continue;
                }

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

            pollosVehiculos += entregaDTO.cantidadPollos();
        }

        if (pollosVehiculos != totalPollos) {

            throw new IllegalArgumentException(
                    "Los vehículos deben transportar "
                            + totalPollos + " pollos. Asignados: "
                            + pollosVehiculos);
        }

        if (!repartidores.equals(repartidoresConVehiculo)) {

            throw new IllegalArgumentException(
                    "Cada repartidor debe tener un vehículo");
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
                            asigTrabajadorRepository.save(asignacion)));
        }

        // Guardar entregas
        List<EntregaDTO> entregasGuardadas = new ArrayList<>();

        for (EntregaDTO entregaDTO : dto.entregas()) {

            Vehiculo vehiculo = vehiculoRepository
                    .findById(entregaDTO.idVehiculo())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pedido ID: " + id + " no existe"));
    }

    private Trabajador obtenerTrabajador(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El trabajador es obligatorio");
        }

        return trabajadorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
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

        // La hora final es opcional.
        if (dto.horaFin() != null &&
                dto.horaInicio() == null) {

            throw new IllegalArgumentException(
                    "Debe registrar primero la hora de inicio");
        }

        if (dto.horaInicio() != null &&
                dto.horaFin() != null &&
                !dto.horaFin().isAfter(dto.horaInicio())) {

            throw new IllegalArgumentException(
                    "La hora final debe ser posterior a la inicial");
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
                            "El trabajador no puede repartir");
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "Función no válida");
        }

        if (!"REPARTIDOR".equals(dto.funcion())) {

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

            validarCapacidad(
                    dto, trabajador, capacidad, idExcluir);
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

            if ("ASADOR".equals(dto.funcion())) {
                totalAsignado += 1;
            } else if (asignacion.getCantidadAsignada() != null) {
                totalAsignado += asignacion.getCantidadAsignada();
            }
        }

        double nuevaCantidad;

        if ("ASADOR".equals(dto.funcion())) {
            // La capacidad del asador se mide en pedidos.
            nuevaCantidad = 1;
        } else {
            // La capacidad del preparador se mide en pollos.
            nuevaCantidad = dto.cantidadAsignada();
        }

        if (totalAsignado + nuevaCantidad > capacidad) {

            throw new IllegalArgumentException(
                    "La asignación supera la capacidad diaria. "
                            + "Capacidad: " + capacidad
                            + ", ya asignados: " + totalAsignado);
        }
    }

    private void validarHorarios(
            AsigTrabajadorDTO dto,
            Trabajador trabajador,
            Long idExcluir) {

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

            boolean mismaFuncion =
                    dto.funcion().equals(asignacion.getFuncion());

            boolean funcionDeProduccion =
                    "PREPARADOR".equals(dto.funcion())
                            || "ASADOR".equals(dto.funcion());

            // Permitir varios pedidos de la misma función
            // respetando la capacidad diaria.
            if (mismaFuncion && funcionDeProduccion) {
                continue;
            }

            if (horariosSeCruzan(
                    dto.horaInicio(),
                    dto.horaFin(),
                    asignacion.getHoraInicio(),
                    asignacion.getHoraFin())) {

                throw new IllegalArgumentException(
                        "El trabajador tiene otra asignación "
                                + "en el mismo horario. Función: "
                                + asignacion.getFuncion());
            }
        }
    }

    private boolean horariosSeCruzan(
            LocalTime inicio1,
            LocalTime fin1,
            LocalTime inicio2,
            LocalTime fin2) {

        // Si falta la hora de inicio, no se puede
        // asegurar que los horarios sean compatibles.
        if (inicio1 == null || inicio2 == null) {
            return true;
        }

        // Hora final null significa horario abierto.
        boolean primeraCondicion =
                fin2 == null || inicio1.isBefore(fin2);

        boolean segundaCondicion =
                fin1 == null || inicio2.isBefore(fin1);

        return primeraCondicion && segundaCondicion;
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
