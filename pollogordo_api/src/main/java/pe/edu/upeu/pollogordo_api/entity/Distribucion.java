package pe.edu.upeu.pollogordo_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DISTRIBUCION")
public class Distribucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_distribucion")
    private Long idDistribucion;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_insumo")
    private Insumo insumo;

    @Column(name = "cantidad")
    private Double cantidad;

    @Column(name = "estado")
    private String estado;

    public Distribucion() {
    }

    public Distribucion(Long idDistribucion, Pedido pedido, Insumo insumo, Double cantidad, String estado) {
        this.idDistribucion = idDistribucion;
        this.pedido = pedido;
        this.insumo = insumo;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    public Long getIdDistribucion() {
        return idDistribucion;
    }

    public void setIdDistribucion(Long idDistribucion) {
        this.idDistribucion = idDistribucion;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
