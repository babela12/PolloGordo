
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ProductoService } from '../../../core/services/producto.service';
import { Producto } from '../../../core/interfaces/producto.interface';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {

  private productoService = inject(ProductoService);
  private alertService = inject(AlertService);

  products: Producto[] = [];
  busqueda = '';
  procesandoId: number | null = null;

  ngOnInit(): void {
    this.listProduct();
  }

  listProduct(): void {
    this.productoService.getProductos().subscribe({
      next: (data) => {
        this.products = data;
      },
      error: (err) => {
        console.error('Error al cargar productos:', err);
        this.alertService.error('No se pudieron cargar los productos');
      }
    });
  }

  get productosFiltrados(): Producto[] {
    return this.products.filter(p =>
      p.nombre.toLowerCase().includes(this.busqueda.toLowerCase())
    );
  }

  getImagen(producto: Producto): string {
    return '/pollogordo_api/images/' +
      (producto.imagen || 'no-image.png');
  }

  async cambiarEstado(producto: Producto): Promise<void> {

    if (producto.idProducto == null || this.procesandoId !== null) {
      return;
    }

    const estaActivo = producto.estado === 'A';
    const nuevoEstado = estaActivo ? 'I' : 'A';
    const accion = estaActivo ? 'desactivar' : 'activar';

    const confirmado = await this.alertService.confirm(
      `¿Deseas ${accion} el producto "${producto.nombre}"?`,
      estaActivo ? '¿Desactivar producto?' : '¿Activar producto?'
    );

    if (!confirmado) {
      return;
    }

    const formData = new FormData();

    formData.append('nombre', producto.nombre);
    formData.append('descripcion', producto.descripcion ?? '');
    formData.append('tipo', producto.tipo ?? '');
    formData.append('precio', String(producto.precio));
    formData.append('estado', nuevoEstado);

    // No enviamos un archivo nuevo.
    // El backend conservará la imagen existente.

    this.procesandoId = producto.idProducto;

    this.productoService.updateProduct(producto.idProducto, formData)
      .subscribe({
        next: () => {
          this.procesandoId = null;

          this.alertService.toast(
            estaActivo
              ? 'Producto desactivado correctamente'
              : 'Producto activado correctamente'
          );

          this.listProduct();
        },
        error: (err) => {
          this.procesandoId = null;
          console.error('Error al cambiar el estado:', err);

          this.alertService.error(
            'No se pudo cambiar el estado del producto'
          );
        }
      });
  }
}
