
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.pollogordo_api.dto.AsigTrabajadorDTO;
import pe.edu.upeu.pollogordo_api.entity.AsigTrabajador;
import pe.edu.upeu.pollogordo_api.entity.Pedido;
import pe.edu.upeu.pollogordo_api.entity.Trabajador;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.repository.AsigTrabajadorRepository;
import pe.edu.upeu.pollogordo_api.repository.PedidoRepository;
import pe.edu.upeu.pollogordo_api.repository.TrabajadorRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsigTrabajadorServiceImpl
        implements IAsigTrabajadorService {

    private final AsigTrabajadorRepository asigTrabajadorRepository;
    private final PedidoRepository pedidoRepository;
    private final TrabajadorRepository trabajadorRepository;

    public AsigTrabajadorServiceImpl(
            AsigTrabajadorRepository asigTrabajadorRepository,
            PedidoRepository pedidoRepository,
            TrabajadorRepository trabajadorRepository) {

        this.asigTrabajadorRepository = asigTrabajadorRepository;
        this.pedidoRepository = pedidoRepository;
        this.trabajadorRepository = trabajadorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsigTrabajadorDTO> listarTodo() {

        return asigTrabajadorRepository.findAll()
                .stream()
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsigTrabajadorDTO> buscarPorId(Long id) {

        return asigTrabajadorRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public AsigTrabajadorDTO crear(AsigTrabajadorDTO dto) {

        Pedido pedido = obtenerPedido(dto.idPedido());
        Trabajador trabajador = obtenerTrabajador(dto.idTrabajador());

        validarAsignacion(dto, trabajador, null);

        AsigTrabajador asignacion = new AsigTrabajador();

        asignarDatos(asignacion, dto, pedido, trabajador);

        return converTODTO(
                asigTrabajadorRepository.save(asignacion)
        );
    }

    @Override
    @Transactional
    public Optional<AsigTrabajadorDTO> actualizar(
            Long id,
            AsigTrabajadorDTO dto) {

        return asigTrabajadorRepository.findById(id)
                .map(asignacion -> {

                    Pedido pedido = obtenerPedido(dto.idPedido());
                    Trabajador trabajador =
                            obtenerTrabajador(dto.idTrabajador());

                    validarAsignacion(dto, trabajador, id);

                    asignarDatos(
                            asignacion,
                            dto,
                            pedido,
                            trabajador
                    );

                    return converTODTO(
                            asigTrabajadorRepository.save(asignacion)
                    );
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

    private Pedido obtenerPedido(Long id) {

        return pedidoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pedido ID: " + id + " no existe"
                        )
                );
    }

    private Trabajador obtenerTrabajador(Long id) {

        return trabajadorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trabajador ID: " + id + " no existe"
                        )
                );
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

        asignacion.setPagoAdicional(
                dto.pagoAdicional() != null
                        ? dto.pagoAdicional()
                        : BigDecimal.ZERO
        );

        asignacion.setEstado(
                dto.estado() != null
                        ? dto.estado()
                        : "PROGRAMADO"
        );
    }

    private void validarAsignacion(
            AsigTrabajadorDTO dto,
            Trabajador trabajador,
            Long idExcluir) {

        if (dto.fechaTrabajo() == null) {
            throw new IllegalArgumentException(
                    "La fecha de trabajo es obligatoria"
            );
        }

        if (dto.funcion() == null) {
            throw new IllegalArgumentException(
                    "La función es obligatoria"
            );
        }

        if ((dto.horaInicio() == null)
                != (dto.horaFin() == null)) {

            throw new IllegalArgumentException(
                    "Debe registrar ambas horas o ninguna"
            );
        }

        if (dto.horaInicio() != null
                && !dto.horaFin().isAfter(dto.horaInicio())) {

            throw new IllegalArgumentException(
                    "La hora de fin debe ser posterior a la de inicio"
            );
        }

        if (dto.cantidadRealizada() != null
                && dto.cantidadAsignada() != null
                && dto.cantidadRealizada()
                > dto.cantidadAsignada()) {

            throw new IllegalArgumentException(
                    "La cantidad realizada no puede superar "
                            + "la cantidad asignada"
            );
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
                                    + "para realizar repartos"
                    );
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "Función no válida"
                );
        }

        if (!dto.funcion().equals("REPARTIDOR")) {

            if (capacidad == null || capacidad <= 0) {
                throw new IllegalArgumentException(
                        "El trabajador no tiene capacidad "
                                + "registrada para esta función"
                );
            }

            if (dto.cantidadAsignada() == null
                    || dto.cantidadAsignada() <= 0) {

                throw new IllegalArgumentException(
                        "Debe asignar una cantidad de pollos "
                                + "mayor que cero"
                );
            }

            validarCapacidad(
                    dto,
                    trabajador,
                    capacidad,
                    idExcluir
            );
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

        for (AsigTrabajador a : asignaciones) {

            if (idExcluir != null
                    && idExcluir.equals(a.getIdAsig())) {
                continue;
            }

            if ("CANCELADO".equals(a.getEstado())) {
                continue;
            }

            if (a.getCantidadAsignada() != null) {
                totalAsignado += a.getCantidadAsignada();
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
                            + dto.cantidadAsignada()
            );
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

        for (AsigTrabajador a : asignaciones) {

            if (idExcluir != null
                    && idExcluir.equals(a.getIdAsig())) {
                continue;
            }

            if ("CANCELADO".equals(a.getEstado())) {
                continue;
            }

            if (a.getHoraInicio() == null
                    || a.getHoraFin() == null) {
                continue;
            }

            boolean seCruzan =
                    inicio.isBefore(a.getHoraFin())
                            && fin.isAfter(a.getHoraInicio());

            if (!seCruzan) {
                continue;
            }

            // PREPARADOR y ASADOR pueden atender varios
            // pedidos simultáneamente de la misma función,
            // siempre que respeten su capacidad diaria.
            boolean mismaFuncion =
                    dto.funcion().equals(a.getFuncion());

            boolean funcionDeProduccion =
                    "PREPARADOR".equals(dto.funcion())
                            || "ASADOR".equals(dto.funcion());

            if (mismaFuncion && funcionDeProduccion) {
                continue;
            }

            // Para repartidores y funciones diferentes,
            // no permitimos horarios superpuestos.
            throw new IllegalArgumentException(
                    "El trabajador tiene otra asignación "
                            + "en el mismo horario. "
                            + "Función existente: "
                            + a.getFuncion()
            );
        }
    }

    private AsigTrabajadorDTO converTODTO(
            AsigTrabajador a) {

        return new AsigTrabajadorDTO(
                a.getIdAsig(),
                a.getPedido().getIdPedido(),
                a.getTrabajador().getIdTrabajador(),
                a.getFuncion(),
                a.getFechaTrabajo(),
                a.getHoraInicio(),
                a.getHoraFin(),
                a.getCantidadAsignada(),
                a.getCantidadRealizada(),
                a.getTarifaAplicada(),
                a.getPagoAdicional(),
                a.getEstado()
        );
    }
}
