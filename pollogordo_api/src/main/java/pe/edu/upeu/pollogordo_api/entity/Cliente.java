package pe.edu.upeu.pollogordo_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CLIENTE")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @ManyToOne
    @JoinColumn(name = "id_persona")
    private Persona persona;

    @Column(name = "direccion")
    private String direccion;

    public Cliente() {
    }

    public Cliente(Long idCliente, Persona persona, String direccion) {
        this.idCliente = idCliente;
        this.persona = persona;
        this.direccion = direccion;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
