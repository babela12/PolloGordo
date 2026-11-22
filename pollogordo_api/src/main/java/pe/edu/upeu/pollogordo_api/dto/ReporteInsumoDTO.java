
package pe.edu.upeu.pollogordo_api.dto;

public record ReporteInsumoDTO(
        Long idInsumo,
        String insumo,
        String unidad,
        Double cantidad,
        Long pedidos
) {
}
