package pe.edu.upeu.pollogordo_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "VEHICULO")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Long idVehiculo;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "placa")
    private String placa;

    @Column(name = "capacidad")
    private Double capacidad;

    @Column(name = "estado")
    private String estado;

    public Vehiculo() {
    }

    public Vehiculo(Long idVehiculo, String tipo, String placa, Double capacidad, String estado) {
        this.idVehiculo = idVehiculo;
        this.tipo = tipo;
        this.placa = placa;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public Long getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(Long idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public Double getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Double capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
