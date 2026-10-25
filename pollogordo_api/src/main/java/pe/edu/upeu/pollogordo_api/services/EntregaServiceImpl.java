package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.EntregaDTO;
import pe.edu.upeu.pollogordo_api.entity.Entrega;
import pe.edu.upeu.pollogordo_api.entity.Pedido;
import pe.edu.upeu.pollogordo_api.entity.Trabajador;
import pe.edu.upeu.pollogordo_api.entity.Vehiculo;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
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

    public EntregaServiceImpl(
            EntregaRepository entregaRepository,
            PedidoRepository pedidoRepository,
            VehiculoRepository vehiculoRepository,
            TrabajadorRepository trabajadorRepository) {

        this.entregaRepository = entregaRepository;
        this.pedidoRepository = pedidoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.trabajadorRepository = trabajadorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntregaDTO> listarTodo() {

        return entregaRepository.findAll()
                .stream()
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntregaDTO> buscarPorId(Long id) {

        return entregaRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public EntregaDTO crear(EntregaDTO dto) {

        Pedido pedido = pedidoRepository.findById(dto.idPedido())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pedido ID: " + dto.idPedido() + " no existe"
                        ));

        Vehiculo vehiculo = vehiculoRepository.findById(dto.idVehiculo())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehículo ID: " + dto.idVehiculo() + " no existe"
                        ));

        Trabajador trabajador = trabajadorRepository.findById(dto.idTrabajador())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trabajador ID: " + dto.idTrabajador() + " no existe"
                        ));

        Entrega entrega = new Entrega();

        entrega.setPedido(pedido);
        entrega.setVehiculo(vehiculo);
        entrega.setTrabajador(trabajador);
        entrega.setFechaEntrega(dto.fechaEntrega());
        entrega.setEstado(dto.estado());
        entrega.setObservacion(dto.observacion());

        return converTODTO(
                entregaRepository.save(entrega)
        );
    }

    @Override
    @Transactional
    public Optional<EntregaDTO> actualizar(Long id, EntregaDTO dto) {

        return entregaRepository.findById(id)
                .map(entrega -> {

                    Pedido pedido = pedidoRepository.findById(dto.idPedido())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Pedido ID: " + dto.idPedido() + " no existe"
                                    ));

                    Vehiculo vehiculo = vehiculoRepository.findById(dto.idVehiculo())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Vehículo ID: " + dto.idVehiculo() + " no existe"
                                    ));

                    Trabajador trabajador = trabajadorRepository.findById(dto.idTrabajador())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Trabajador ID: " + dto.idTrabajador() + " no existe"
                                    ));

                    entrega.setPedido(pedido);
                    entrega.setVehiculo(vehiculo);
                    entrega.setTrabajador(trabajador);
                    entrega.setFechaEntrega(dto.fechaEntrega());
                    entrega.setEstado(dto.estado());
                    entrega.setObservacion(dto.observacion());

                    return converTODTO(
                            entregaRepository.save(entrega)
                    );
                });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (entregaRepository.existsById(id)) {
            entregaRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private EntregaDTO converTODTO(Entrega e) {

        return new EntregaDTO(
                e.getIdEntrega(),
                e.getPedido().getIdPedido(),
                e.getVehiculo().getIdVehiculo(),
                e.getTrabajador().getIdTrabajador(),
                e.getFechaEntrega(),
                e.getEstado(),
                e.getObservacion()
        );
    }
}