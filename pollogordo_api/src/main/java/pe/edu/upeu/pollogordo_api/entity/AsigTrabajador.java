
package pe.edu.upeu.pollogordo_api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "ASIG_TRABAJADOR")
public class AsigTrabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asig")
    private Long idAsig;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_trabajador")
    private Trabajador trabajador;

    @Column(name = "funcion", length = 20)
    private String funcion;

    @Column(name = "fecha_trabajo")
    private LocalDate fechaTrabajo;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "cantidad_asignada")
    private Double cantidadAsignada;

    @Column(name = "cantidad_realizada")
    private Double cantidadRealizada;

    @Column(name = "tarifa_aplicada", precision = 10, scale = 2)
    private BigDecimal tarifaAplicada;

    @Column(name = "pago_adicional", precision = 10, scale = 2)
    private BigDecimal pagoAdicional = BigDecimal.ZERO;

    @Column(name = "estado", length = 20)
    private String estado;

    public AsigTrabajador() {
    }

    public AsigTrabajador(Long idAsig,
                          Pedido pedido,
                          Trabajador trabajador,
                          String funcion,
                          String estado) {
        this.idAsig = idAsig;
        this.pedido = pedido;
        this.trabajador = trabajador;
        this.funcion = funcion;
        this.estado = estado;
    }

    public Long getIdAsig() {
        return idAsig;
    }

    public void setIdAsig(Long idAsig) {
        this.idAsig = idAsig;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Trabajador getTrabajador() {
        return trabajador;
    }

    public void setTrabajador(Trabajador trabajador) {
        this.trabajador = trabajador;
    }

    public String getFuncion() {
        return funcion;
    }

    public void setFuncion(String funcion) {
        this.funcion = funcion;
    }

    public LocalDate getFechaTrabajo() {
        return fechaTrabajo;
    }

    public void setFechaTrabajo(LocalDate fechaTrabajo) {
        this.fechaTrabajo = fechaTrabajo;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Double getCantidadAsignada() {
        return cantidadAsignada;
    }

    public void setCantidadAsignada(Double cantidadAsignada) {
        this.cantidadAsignada = cantidadAsignada;
    }

    public Double getCantidadRealizada() {
        return cantidadRealizada;
    }

    public void setCantidadRealizada(Double cantidadRealizada) {
        this.cantidadRealizada = cantidadRealizada;
    }

    public BigDecimal getTarifaAplicada() {
        return tarifaAplicada;
    }

    public void setTarifaAplicada(BigDecimal tarifaAplicada) {
        this.tarifaAplicada = tarifaAplicada;
    }

    public BigDecimal getPagoAdicional() {
        return pagoAdicional;
    }

    public void setPagoAdicional(BigDecimal pagoAdicional) {
        this.pagoAdicional = pagoAdicional;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
