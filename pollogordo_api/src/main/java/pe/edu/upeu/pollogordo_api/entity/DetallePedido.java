
package pe.edu.upeu.pollogordo_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DETALLE_PEDIDO")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(name = "unidad", length = 10)
    private String unidad;

    @Column(name = "cantidad")
    private Double cantidad;

    @Column(name = "cantidad_pollos")
    private Integer cantidadPollos;

    @Column(name = "precio")
    private Double precio;

    @Column(name = "subtotal")
    private Double subtotal;

    // Acompañamiento principal: PAPA o YUCA
    @Column(name = "acompanamiento", length = 10)
    private String acompanamiento;

    // Arroz adicional: ARABE o JARDINERA
    @Column(name = "tipo_arroz", length = 15)
    private String tipoArroz;

    // Ensalada adicional: BLANCA o FRESCA
    @Column(name = "tipo_ensalada", length = 15)
    private String tipoEnsalada;

    public DetallePedido() {
    }

    public DetallePedido(
            Long idDetalle,
            Pedido pedido,
            Producto producto,
            String unidad,
            Double cantidad,
            Integer cantidadPollos,
            Double precio,
            Double subtotal) {

        this.idDetalle = idDetalle;
        this.pedido = pedido;
        this.producto = producto;
        this.unidad = unidad;
        this.cantidad = cantidad;
        this.cantidadPollos = cantidadPollos;
        this.precio = precio;
        this.subtotal = subtotal;
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCantidadPollos() {
        return cantidadPollos;
    }

    public void setCantidadPollos(Integer cantidadPollos) {
        this.cantidadPollos = cantidadPollos;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public String getAcompanamiento() {
        return acompanamiento;
    }

    public void setAcompanamiento(String acompanamiento) {
        this.acompanamiento = acompanamiento;
    }

    public String getTipoArroz() {
        return tipoArroz;
    }

    public void setTipoArroz(String tipoArroz) {
        this.tipoArroz = tipoArroz;
    }

    public String getTipoEnsalada() {
        return tipoEnsalada;
    }

    public void setTipoEnsalada(String tipoEnsalada) {
        this.tipoEnsalada = tipoEnsalada;
    }
}
