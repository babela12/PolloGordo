
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AsigTrabajadorServiceImpl implements IAsigTrabajadorService {

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

        List<AsigTrabajador> asignaciones = asigTrabajadorRepository.findAll();
        List<AsigTrabajadorDTO> lista = new ArrayList<>();

        for (AsigTrabajador asignacion : asignaciones) {
            lista.add(convertirDTO(asignacion));
        }

        return lista;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsigTrabajadorDTO> buscarPorId(Long id) {

        Optional<AsigTrabajador> resultado = asigTrabajadorRepository.findById(id);

        if (resultado.isPresent()) {
            return Optional.of(convertirDTO(resultado.get()));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public AsigTrabajadorDTO crear(AsigTrabajadorDTO dto) {

        Pedido pedido = obtenerPedido(dto.idPedido());
        Trabajador trabajador = obtenerTrabajador(dto.idTrabajador());

        validarAsignacion(dto, trabajador, null);

        AsigTrabajador asignacion = new AsigTrabajador();

        asignarDatos(asignacion, dto, pedido, trabajador);

        AsigTrabajador guardado = asigTrabajadorRepository.save(asignacion);

        return convertirDTO(guardado);
    }

    @Override
    @Transactional
    public Optional<AsigTrabajadorDTO> actualizar(Long id, AsigTrabajadorDTO dto) {

        Optional<AsigTrabajador> resultado = asigTrabajadorRepository.findById(id);

        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        AsigTrabajador asignacion = resultado.get();

        Pedido pedido = obtenerPedido(dto.idPedido());
        Trabajador trabajador = obtenerTrabajador(dto.idTrabajador());

        validarAsignacion(dto, trabajador, id);

        asignarDatos(asignacion, dto, pedido, trabajador);

        AsigTrabajador guardado = asigTrabajadorRepository.save(asignacion);

        return Optional.of(convertirDTO(guardado));
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

        Optional<Pedido> resultado = pedidoRepository.findById(id);

        if (resultado.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Pedido ID: " + id + " no existe"
            );
        }

        return resultado.get();
    }

    private Trabajador obtenerTrabajador(Long id) {

        Optional<Trabajador> resultado = trabajadorRepository.findById(id);

        if (resultado.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Trabajador ID: " + id + " no existe"
            );
        }

        return resultado.get();
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
                    "La fecha de trabajo es obligatoria"
            );
        }

        if (dto.funcion() == null) {
            throw new IllegalArgumentException(
                    "La función es obligatoria"
            );
        }

        if ((dto.horaInicio() == null) != (dto.horaFin() == null)) {
            throw new IllegalArgumentException(
                    "Debe registrar ambas horas o ninguna"
            );
        }

        if (dto.horaInicio() != null &&
                !dto.horaFin().isAfter(dto.horaInicio())) {

            throw new IllegalArgumentException(
                    "La hora de fin debe ser posterior a la de inicio"
            );
        }

        if (dto.cantidadRealizada() != null &&
                dto.cantidadAsignada() != null &&
                dto.cantidadRealizada() > dto.cantidadAsignada()) {

            throw new IllegalArgumentException(
                    "La cantidad realizada no puede superar la cantidad asignada"
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

                if (!Boolean.TRUE.equals(trabajador.getPuedeRepartir())) {
                    throw new IllegalArgumentException(
                            "El trabajador no está habilitado para realizar repartos"
                    );
                }

                break;

            default:
                throw new IllegalArgumentException("Función no válida");
        }

        if (!dto.funcion().equals("REPARTIDOR")) {

            if (capacidad == null || capacidad <= 0) {
                throw new IllegalArgumentException(
                        "El trabajador no tiene capacidad registrada para esta función"
                );
            }

            if (dto.cantidadAsignada() == null ||
                    dto.cantidadAsignada() <= 0) {

                throw new IllegalArgumentException(
                        "Debe asignar una cantidad de pollos mayor que cero"
                );
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

        double nuevoTotal = totalAsignado + dto.cantidadAsignada();

        if (nuevoTotal > capacidad) {
            throw new IllegalArgumentException(
                    "La asignación supera la capacidad diaria. " +
                            "Capacidad: " + capacidad +
                            ", ya asignados: " + totalAsignado +
                            ", nueva cantidad: " + dto.cantidadAsignada()
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
                    inicio.isBefore(asignacion.getHoraFin()) &&
                            fin.isAfter(asignacion.getHoraInicio());

            if (!seCruzan) {
                continue;
            }

            boolean mismaFuncion =
                    dto.funcion().equals(asignacion.getFuncion());

            boolean funcionDeProduccion =
                    "PREPARADOR".equals(dto.funcion()) ||
                            "ASADOR".equals(dto.funcion());

            if (mismaFuncion && funcionDeProduccion) {
                continue;
            }

            throw new IllegalArgumentException(
                    "El trabajador tiene otra asignación en el mismo horario. " +
                            "Función existente: " + asignacion.getFuncion()
            );
        }
    }

    private AsigTrabajadorDTO convertirDTO(AsigTrabajador asignacion) {

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
                asignacion.getEstado()
        );
    }
}
