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
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoDTO> buscarPorId(Long id) {

        return pagoRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public PagoDTO crear(PagoDTO dto) {

        Pedido pedido = pedidoRepository.findById(dto.idPedido())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pedido ID: " + dto.idPedido() + " no existe"
                        ));

        Pago pago = new Pago();

        pago.setPedido(pedido);
        pago.setMetodo(dto.metodo());
        pago.setMonto(dto.monto());
        pago.setFechaPago(dto.fechaPago());
        pago.setReferencia(dto.referencia());
        pago.setEstado(dto.estado());

        return converTODTO(
                pagoRepository.save(pago)

        );
    }

    @Override
    @Transactional
    public Optional<PagoDTO> actualizar(Long id, PagoDTO dto) {

        return pagoRepository.findById(id)
                .map(pago -> {

                    Pedido pedido = pedidoRepository.findById(dto.idPedido())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Pedido ID: " + dto.idPedido() + " no existe"
                                    ));

                    pago.setPedido(pedido);
                    pago.setMetodo(dto.metodo());
                    pago.setMonto(dto.monto());
                    pago.setFechaPago(dto.fechaPago());
                    pago.setReferencia(dto.referencia());
                    pago.setEstado(dto.estado());

                    return converTODTO(
                            pagoRepository.save(pago)
                    );
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

    private PagoDTO converTODTO(Pago p) {

        return new PagoDTO(
                p.getIdPago(),
                p.getPedido().getIdPedido(),
                p.getMetodo(),
                p.getMonto(),
                p.getFechaPago(),
                p.getReferencia(),
                p.getEstado()
        );
    }
}