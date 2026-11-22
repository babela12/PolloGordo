
package pe.edu.upeu.pollogordo_api.dto;

public record ResumenPagoDTO(
        Long idPedido,
        Double totalPedido,
        Double totalAbonado,
        Double saldoPendiente,
        String estadoPago
) {
}
