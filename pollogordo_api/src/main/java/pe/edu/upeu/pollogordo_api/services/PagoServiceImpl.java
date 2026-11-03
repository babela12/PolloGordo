
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.upeu.pollogordo_api.dto.PagoDTO;
import pe.edu.upeu.pollogordo_api.entity.Pago;
import pe.edu.upeu.pollogordo_api.entity.Pedido;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.repository.PagoRepository;
import pe.edu.upeu.pollogordo_api.repository.PedidoRepository;

import java.util.ArrayList;
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

        List<Pago> pagos = pagoRepository.findAll();
        List<PagoDTO> lista = new ArrayList<>();

        for (Pago pago : pagos) {
            lista.add(convertirDTO(pago));
        }

        return lista;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoDTO> buscarPorId(Long id) {

        Optional<Pago> resultado = pagoRepository.findById(id);

        if (resultado.isPresent()) {
            return Optional.of(convertirDTO(resultado.get()));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public PagoDTO crear(PagoDTO dto) {

        Pedido pedido = buscarPedido(dto.idPedido());

        Pago pago = new Pago();

        asignarDatos(pago, dto, pedido);

        Pago guardado = pagoRepository.save(pago);

        return convertirDTO(guardado);
    }

    @Override
    @Transactional
    public Optional<PagoDTO> actualizar(Long id, PagoDTO dto) {

        Optional<Pago> resultado = pagoRepository.findById(id);

        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        Pago pago = resultado.get();
        Pedido pedido = buscarPedido(dto.idPedido());

        asignarDatos(pago, dto, pedido);

        Pago guardado = pagoRepository.save(pago);

        return Optional.of(convertirDTO(guardado));
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

        Optional<Pedido> resultado = pedidoRepository.findById(id);

        if (resultado.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Pedido ID: " + id + " no existe"
            );
        }

        return resultado.get();
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
