
package pe.edu.upeu.pollogordo_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TRABAJADOR")
public class Trabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trabajador")
    private Long idTrabajador;

    @ManyToOne
    @JoinColumn(name = "id_persona")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "id_cargo")
    private Cargo cargo;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "capacidad_preparacion")
    private Integer capacidadPreparacion;

    @Column(name = "capacidad_asado")
    private Integer capacidadAsado;

    @Column(name = "puede_repartir")
    private Boolean puedeRepartir = false;

    @Column(name = "estado", length = 1, nullable = false)
    private String estado = "A";

    public Trabajador() {
    }


    public Trabajador(Long idTrabajador, Persona persona, Cargo cargo, String direccion, Integer capacidadPreparacion, Integer capacidadAsado, Boolean puedeRepartir, String estado) {
        this.idTrabajador = idTrabajador;
        this.persona = persona;
        this.cargo = cargo;
        this.direccion = direccion;
        this.capacidadPreparacion = capacidadPreparacion;
        this.capacidadAsado = capacidadAsado;
        this.puedeRepartir = puedeRepartir;
        this.estado = estado;
    }

    public Long getIdTrabajador() {
        return idTrabajador;
    }

    public void setIdTrabajador(Long idTrabajador) {
        this.idTrabajador = idTrabajador;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getCapacidadPreparacion() {
        return capacidadPreparacion;
    }

    public void setCapacidadPreparacion(
            Integer capacidadPreparacion) {
        this.capacidadPreparacion = capacidadPreparacion;
    }

    public Integer getCapacidadAsado() {
        return capacidadAsado;
    }

    public void setCapacidadAsado(Integer capacidadAsado) {
        this.capacidadAsado = capacidadAsado;
    }

    public Boolean getPuedeRepartir() {
        return puedeRepartir;
    }

    public void setPuedeRepartir(Boolean puedeRepartir) {
        this.puedeRepartir = puedeRepartir;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
