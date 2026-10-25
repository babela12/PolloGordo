package pe.edu.upeu.pollogordo_api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "PAGO")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @Column(name = "metodo")
    private String metodo;

    @Column(name = "monto")
    private Double monto;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "estado")
    private String estado;

    public Pago() {
    }

    public Pago(Long idPago, Pedido pedido, String metodo, Double monto, LocalDate fechaPago, String referencia, String estado) {
        this.idPago = idPago;
        this.pedido = pedido;
        this.metodo = metodo;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.referencia = referencia;
        this.estado = estado;
    }

    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
