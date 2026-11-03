
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
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntregaDTO> buscarPorId(Long id) {

        return entregaRepository.findById(id)
                .map(this::convertirDTO);
    }

    @Override
    @Transactional
    public EntregaDTO crear(EntregaDTO dto) {

        Pedido pedido = buscarPedido(dto.idPedido());
        Vehiculo vehiculo = buscarVehiculo(dto.idVehiculo());
        Trabajador trabajador = buscarTrabajador(dto.idTrabajador());

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

            asignarDatos(entrega, dto, pedido, vehiculo, trabajador);

            return convertirDTO(entregaRepository.save(entrega));
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

    private Pedido buscarPedido(Long id) {

        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido ID: " + id + " no existe"));
    }

    private Vehiculo buscarVehiculo(Long id) {

        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehículo ID: " + id + " no existe"));
    }

    private Trabajador buscarTrabajador(Long id) {

        return trabajadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trabajador ID: " + id + " no existe"));
    }

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
        entrega.setEstado(dto.estado());
        entrega.setObservacion(dto.observacion());
    }

    private EntregaDTO convertirDTO(Entrega entrega) {

        return new EntregaDTO(
                entrega.getIdEntrega(),
                entrega.getPedido().getIdPedido(),
                entrega.getVehiculo().getIdVehiculo(),
                entrega.getTrabajador().getIdTrabajador(),
                entrega.getFechaEntrega(),
                entrega.getEstado(),
                entrega.getObservacion()
        );
    }
}
