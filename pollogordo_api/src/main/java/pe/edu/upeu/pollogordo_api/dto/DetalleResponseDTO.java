
package pe.edu.upeu.pollogordo_api.dto;

public record DetalleResponseDTO(

        String producto,
        Double cantidad,
        String unidad,
        Integer cantidadPollos,
        Double precioUnitario,
        Double subtotal,

        String acompanamiento,
        String tipoArroz,
        String tipoEnsalada

) {
}
