
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Producto } from '../../../core/interfaces/producto.interface';
import { Insumo } from '../../../core/interfaces/insumo.interface';
import { ProductoService } from '../../../core/services/producto.service';
import { InsumoService } from '../../../core/services/insumo.service';
import {
  PedidoTemporal,
  PedidoTemporalService
} from '../../../core/services/pedido-temporal.service';

interface FilaCarrito {
  nombre: string;
  imagen: string;
  precio: number;
  cantidad: number;
  subtotal: number;
  detalle: string;
}

@Component({
  selector: 'app-pedido-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pedido-list.component.html',
  styleUrl: './pedido-list.component.css'
})
export class PedidoListComponent implements OnInit {

  private pedidoTemporalService = inject(PedidoTemporalService);
  private productoService = inject(ProductoService);
  private insumoService = inject(InsumoService);
  private router = inject(Router);

  productos: Producto[] = [];
  insumos: Insumo[] = [];

  buscar = '';
  pedidoSeleccionado: number | null = null;

  ngOnInit(): void {
    this.cargarProductos();
    this.cargarInsumos();
  }

  cargarProductos(): void {
    this.productoService.getProductos().subscribe({
      next: (data) => {
        this.productos = data;
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  cargarInsumos(): void {
    this.insumoService.getInsumos().subscribe({
      next: (data) => {
        this.insumos = data;
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  get pedidos(): PedidoTemporal[] {
    return this.pedidoTemporalService.getPedidos();
  }

  get pedidosFiltrados(): PedidoTemporal[] {
    const texto = this.buscar.toLowerCase().trim();

    return this.pedidos.filter(item =>
      item.nombreCliente.toLowerCase().includes(texto) ||
      item.pedido.telefonoContacto.includes(texto) ||
      item.pedido.fechaEvento.includes(texto)
    );
  }

  verPedido(id: number): void {
    this.pedidoSeleccionado = id;
  }

  volverListado(): void {
    this.pedidoSeleccionado = null;
  }

  get pedidoActual(): PedidoTemporal | undefined {
    return this.pedidos.find(
      pedido => pedido.idTemporal === this.pedidoSeleccionado
    );
  }

  editarPedido(id: number): void {
    this.router.navigate(['/pedidos/nuevo'], {
      queryParams: { idTemporal: id }
    });
  }

  eliminarPedido(id: number): void {
    const confirmar = window.confirm(
      '¿Deseas eliminar este pedido temporal?'
    );

    if (confirmar) {
      this.pedidoTemporalService.eliminarPedido(id);

      if (this.pedidoSeleccionado === id) {
        this.pedidoSeleccionado = null;
      }
    }
  }

  nuevoPedido(): void {
    this.router.navigate(['/pedidos/nuevo']);
  }

  obtenerProducto(idProducto: number): Producto | undefined {
    return this.productos.find(
      producto => producto.idProducto === idProducto
    );
  }

  obtenerNombreProducto(idProducto: number): string {
    const producto = this.obtenerProducto(idProducto);

    return producto ? producto.nombre : 'Producto #' + idProducto;
  }

  getImagen(producto: Producto): string {
    return '/pollogordo_api/images/' +
      (producto.imagen || 'no-image.png');
  }

  esPreparado(producto: Producto): boolean {
    const tipo = producto.tipo.toUpperCase().trim();

    return (
      tipo === 'POLLO' || tipo === 'CHULETA'
    ) && producto.nombre.toUpperCase().trim() !== 'POLLO CRUDO';
  }

  obtenerDivisor(producto: Producto): number {
    if (/X\s*8\b/i.test(producto.nombre)) {
      return 8;
    }

    return 4;
  }

  calcularPorciones(
    cantidad: number,
    unidad: string,
    idProducto?: number
  ): number {
    if (unidad === 'POLLO') {
      const producto = idProducto === undefined
        ? undefined
        : this.obtenerProducto(idProducto);

      return cantidad * (
        producto ? this.obtenerDivisor(producto) : 4
      );
    }

    return cantidad;
  }

  calcularUnidadesCompletas(
    idProducto: number,
    cantidad: number,
    unidad: string
  ): number {
    const producto = this.obtenerProducto(idProducto);

    if (!producto || !this.esPreparado(producto)) {
      return cantidad;
    }

    if (unidad === 'CUARTO' || unidad === 'OCTAVO') {
      return cantidad / this.obtenerDivisor(producto);
    }

    return cantidad;
  }

  obtenerNombreUnidad(
    idProducto: number,
    unidad: string
  ): string {
    const producto = this.obtenerProducto(idProducto);

    if (unidad === 'CUARTO') {
      return 'cuartos';
    }

    if (unidad === 'OCTAVO') {
      return 'octavos';
    }

    if (unidad === 'POLLO') {
      if (
        producto &&
        producto.tipo.toUpperCase().trim() === 'CHULETA'
      ) {
        return 'paquetes completos';
      }

      if (producto && this.esPreparado(producto)) {
        return 'pollos enteros';
      }

      return 'unidades';
    }

    return unidad;
  }

  calcularPrecioArroz(tipo: string): number {
    if (tipo === 'ARABE') {
      return 2;
    }

    if (tipo === 'JARDINERA') {
      return 1.80;
    }

    return 0;
  }

  calcularPrecioEnsalada(tipo: string): number {
    if (tipo === 'BLANCA') {
      return 2;
    }

    if (tipo === 'FRESCA') {
      return 1.80;
    }

    return 0;
  }

  calcularPrecioBase(
    idProducto: number,
    cantidad: number,
    unidad: string
  ): number {
    const producto = this.obtenerProducto(idProducto);

    if (!producto) {
      return 0;
    }

    const completos = this.calcularUnidadesCompletas(
      idProducto,
      cantidad,
      unidad
    );

    return Math.round(
      producto.precio * completos * 100
    ) / 100;
  }

  calcularSubtotalArroz(
    idProducto: number,
    cantidad: number,
    unidad: string,
    tipoArroz: string = ''
  ): number {
    const producto = this.obtenerProducto(idProducto);

    if (!producto || !this.esPreparado(producto)) {
      return 0;
    }

    const porciones = this.calcularPorciones(
      cantidad,
      unidad,
      idProducto
    );

    return Math.round(
      this.calcularPrecioArroz(tipoArroz) * porciones * 100
    ) / 100;
  }

  calcularSubtotalEnsalada(
    idProducto: number,
    cantidad: number,
    unidad: string,
    tipoEnsalada: string = ''
  ): number {
    const producto = this.obtenerProducto(idProducto);

    if (!producto || !this.esPreparado(producto)) {
      return 0;
    }

    const porciones = this.calcularPorciones(
      cantidad,
      unidad,
      idProducto
    );

    return Math.round(
      this.calcularPrecioEnsalada(tipoEnsalada) * porciones * 100
    ) / 100;
  }

  calcularSubtotal(
    idProducto: number,
    cantidad: number,
    unidad: string,
    tipoArroz: string = '',
    tipoEnsalada: string = ''
  ): number {
    return Math.round((
      this.calcularPrecioBase(
        idProducto,
        cantidad,
        unidad
      ) +
      this.calcularSubtotalArroz(
        idProducto,
        cantidad,
        unidad,
        tipoArroz
      ) +
      this.calcularSubtotalEnsalada(
        idProducto,
        cantidad,
        unidad,
        tipoEnsalada
      )
    ) * 100) / 100;
  }

  calcularTotalBase(pedido: PedidoTemporal): number {
    return pedido.pedido.items.reduce((total, item) =>
      total + this.calcularPrecioBase(
        item.idProducto,
        item.cantidad,
        item.unidad
      ), 0
    );
  }

  calcularTotalArroz(pedido: PedidoTemporal): number {
    return pedido.pedido.items.reduce((total, item) =>
      total + this.calcularSubtotalArroz(
        item.idProducto,
        item.cantidad,
        item.unidad,
        item.tipoArroz
      ), 0
    );
  }

  calcularTotalEnsalada(pedido: PedidoTemporal): number {
    return pedido.pedido.items.reduce((total, item) =>
      total + this.calcularSubtotalEnsalada(
        item.idProducto,
        item.cantidad,
        item.unidad,
        item.tipoEnsalada
      ), 0
    );
  }

  calcularTotal(pedido: PedidoTemporal): number {
    return Math.round((
      this.calcularTotalBase(pedido) +
      this.calcularTotalArroz(pedido) +
      this.calcularTotalEnsalada(pedido)
    ) * 100) / 100;
  }

  calcularTotalProductos(pedido: PedidoTemporal): number {
    return pedido.pedido.items.reduce(
      (total, item) => total + item.cantidad,
      0
    );
  }

  obtenerImagenAcompanamiento(nombre: string): string {
    const insumo = this.insumos.find(
      item =>
        item.nombre.toUpperCase().trim() ===
        nombre.toUpperCase().trim()
    );

    return '/pollogordo_api/images/' +
      (insumo?.imagen || 'no-image.png');
  }

  obtenerFilasCarrito(pedido: PedidoTemporal): FilaCarrito[] {
    const filas: FilaCarrito[] = [];

    for (const item of pedido.pedido.items) {
      const producto = this.obtenerProducto(item.idProducto);

      if (!producto) {
        continue;
      }

      const preparado = this.esPreparado(producto);

      const completos = this.calcularUnidadesCompletas(
        item.idProducto,
        item.cantidad,
        item.unidad
      );

      const porciones = preparado
        ? this.calcularPorciones(
            item.cantidad,
            item.unidad,
            item.idProducto
          )
        : item.cantidad;

      filas.push({
        nombre: producto.nombre,
        imagen: this.getImagen(producto),
        precio: producto.precio,
        cantidad: completos,
        subtotal: this.calcularPrecioBase(
          item.idProducto,
          item.cantidad,
          item.unidad
        ),
        detalle: preparado
          ? item.cantidad + ' ' +
            this.obtenerNombreUnidad(
              item.idProducto,
              item.unidad
            ) +
            (item.acompanamiento
              ? ' - ' + item.acompanamiento
              : '')
          : item.cantidad + ' unidades'
      });

      if (preparado && item.tipoArroz) {
        const nombreArroz = item.tipoArroz === 'ARABE'
          ? 'Arroz árabe'
          : 'Arroz a la jardinera';

        const precio = this.calcularPrecioArroz(
          item.tipoArroz
        );

        filas.push({
          nombre: nombreArroz,
          imagen: this.obtenerImagenAcompanamiento(
            nombreArroz
          ),
          precio,
          cantidad: porciones,
          subtotal: Math.round(
            precio * porciones * 100
          ) / 100,
          detalle: 'Acompañamiento'
        });
      }

      if (preparado && item.tipoEnsalada) {
        const nombreEnsalada = item.tipoEnsalada === 'BLANCA'
          ? 'Ensalada blanca'
          : 'Ensalada fresca';

        const precio = this.calcularPrecioEnsalada(
          item.tipoEnsalada
        );

        filas.push({
          nombre: nombreEnsalada,
          imagen: this.obtenerImagenAcompanamiento(
            nombreEnsalada
          ),
          precio,
          cantidad: porciones,
          subtotal: Math.round(
            precio * porciones * 100
          ) / 100,
          detalle: 'Acompañamiento'
        });
      }
    }

    return filas;
  }

  confirmarPedido(id: number): void {
  }

}
