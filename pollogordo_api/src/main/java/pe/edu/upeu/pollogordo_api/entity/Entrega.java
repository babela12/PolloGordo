package pe.edu.upeu.pollogordo_api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "ENTREGA")
public class Entrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega")
    private Long idEntrega;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_vehiculo")
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "id_trabajador")
    private Trabajador trabajador;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Convert(converter = HoraEntregaConverter.class)
    @Column(name = "hora_salida", length = 5)
    private LocalTime horaSalida;

    @Convert(converter = HoraEntregaConverter.class)
    @Column(name = "hora_llegada", length = 5)
    private LocalTime horaLlegada;

    @Column(name = "cantidad_pollos")
    private Integer cantidadPollos;

    @Column(name = "estado")
    private String estado;

    @Column(name = "observacion")
    private String observacion;

    public Entrega() {}

    public Long getIdEntrega() {
        return idEntrega;
    }
    public void setIdEntrega(Long idEntrega) {
        this.idEntrega = idEntrega;
    }
    public Pedido getPedido() {
        return pedido;
    }
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
    public Vehiculo getVehiculo() {
        return vehiculo;
    }
    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }
    public Trabajador getTrabajador() {
        return trabajador;
    }
    public void setTrabajador(Trabajador trabajador) {
        this.trabajador = trabajador;
    }
    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }
    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }
    public LocalTime getHoraSalida() {
        return horaSalida;
    }
    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }
    public LocalTime getHoraLlegada() {
        return horaLlegada;
    }
    public void setHoraLlegada(LocalTime horaLlegada) {
        this.horaLlegada = horaLlegada;
    }
    public Integer getCantidadPollos() {
        return cantidadPollos;
    }
    public void setCantidadPollos(Integer cantidadPollos) {
        this.cantidadPollos = cantidadPollos;
    }
    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }
    public String getObservacion() {
        return observacion;
    }
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
