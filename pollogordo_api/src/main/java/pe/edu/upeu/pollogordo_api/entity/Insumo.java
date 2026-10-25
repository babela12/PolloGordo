
package pe.edu.upeu.pollogordo_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "INSUMO")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo")
    private Long idInsumo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "unidad")
    private String unidad;

    @Column(name = "perecible")
    private String perecible;

    @Column(name = "controla_stock")
    private String controlaStock;

    @Column(name = "cantidad_stock")
    private Double cantidadStock;

    @Column(name = "estado")
    private String estado;

    @Column(name = "imagen")
    private String imagen;

    public Insumo() {
    }

    public Insumo(Long idInsumo, String nombre, String unidad,
                  String perecible, String controlaStock,
                  Double cantidadStock, String estado,
                  String imagen) {
        this.idInsumo = idInsumo;
        this.nombre = nombre;
        this.unidad = unidad;
        this.perecible = perecible;
        this.controlaStock = controlaStock;
        this.cantidadStock = cantidadStock;
        this.estado = estado;
        this.imagen = imagen;
    }

    public Long getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(Long idInsumo) {
        this.idInsumo = idInsumo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getPerecible() {
        return perecible;
    }

    public void setPerecible(String perecible) {
        this.perecible = perecible;
    }

    public String getControlaStock() {
        return controlaStock;
    }

    public void setControlaStock(String controlaStock) {
        this.controlaStock = controlaStock;
    }

    public Double getCantidadStock() {
        return cantidadStock;
    }

    public void setCantidadStock(Double cantidadStock) {
        this.cantidadStock = cantidadStock;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
