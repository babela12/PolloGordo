
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.pollogordo_api.dto.PagoDTO;
import pe.edu.upeu.pollogordo_api.entity.Pago;
import pe.edu.upeu.pollogordo_api.entity.Pedido;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.repository.PagoRepository;
import pe.edu.upeu.pollogordo_api.repository.PedidoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PagoServiceImpl implements IPagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;

    public PagoServiceImpl(
            PagoRepository pagoRepository,
            PedidoRepository pedidoRepository) {

        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
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
    @Transactional
    public PagoDTO crear(PagoDTO dto) {

        Pedido pedido = buscarPedido(dto.idPedido());

        Pago pago = new Pago();

        asignarDatos(pago, dto, pedido);

        return convertirDTO(pagoRepository.save(pago));
    }

    @Override
    @Transactional
    public Optional<PagoDTO> actualizar(Long id, PagoDTO dto) {

        return pagoRepository.findById(id).map(pago -> {

            Pedido pedido = buscarPedido(dto.idPedido());

            asignarDatos(pago, dto, pedido);

            return convertirDTO(pagoRepository.save(pago));
        });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (pagoRepository.existsById(id)) {
            pagoRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private Pedido buscarPedido(Long id) {

        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido ID: " + id + " no existe"));
    }

    private void asignarDatos(
            Pago pago,
            PagoDTO dto,
            Pedido pedido) {

        pago.setPedido(pedido);
        pago.setMetodo(dto.metodo());
        pago.setMonto(dto.monto());
        pago.setFechaPago(dto.fechaPago());
        pago.setReferencia(dto.referencia());
        pago.setEstado(dto.estado());
    }

    private PagoDTO convertirDTO(Pago pago) {

        return new PagoDTO(
                pago.getIdPago(),
                pago.getPedido().getIdPedido(),
                pago.getMetodo(),
                pago.getMonto(),
                pago.getFechaPago(),
                pago.getReferencia(),
                pago.getEstado()
        );
    }
}
