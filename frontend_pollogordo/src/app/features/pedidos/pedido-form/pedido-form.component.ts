
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Cliente } from '../../../core/interfaces/cliente.interface';
import { Producto } from '../../../core/interfaces/producto.interface';
import { Pedido } from '../../../core/interfaces/pedido';
import { ClienteService } from '../../../core/services/cliente.service';
import { ProductoService } from '../../../core/services/producto.service';
import { PedidoTemporalService } from '../../../core/services/pedido-temporal.service';

interface ProductoPedido {
  producto: Producto;
  cantidad: number;
  unidad: string;
  acompanamiento: string;
  tipoArroz: string;
  tipoEnsalada: string;
}

@Component({
  selector: 'app-pedido-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pedido-form.component.html',
  styleUrl: './pedido-form.component.css'
})
export class PedidoFormComponent implements OnInit {

  private clienteService = inject(ClienteService);
  private productoService = inject(ProductoService);
  private pedidoTemporalService = inject(PedidoTemporalService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  clientes: Cliente[] = [];
  productos: Producto[] = [];
  carrito: ProductoPedido[] = [];

  buscarCliente = '';
  buscarProducto = '';

  idCliente: number | null = null;
  idProducto: number | null = null;
  clienteSeleccionado: Cliente | null = null;

  mostrarClientes = false;
  mostrarProductos = false;
  mostrarNuevoCliente = false;
  mostrarEditarProducto = false;

  nombre = '';
  apellidos = '';
  dni = '';
  numTelef = '';
  direccionCliente = '';

  guardandoCliente = false;
  errorCliente = '';

  cantidad = 1;
  unidad = 'CUARTO';
  acompanamiento = 'PAPA';
  tipoArroz = '';
  tipoEnsalada = '';

  indiceEditar = -1;
  cantidadEditar = 1;
  unidadEditar = 'CUARTO';
  acompanamientoEditar = 'PAPA';
  tipoArrozEditar = '';
  tipoEnsaladaEditar = '';

  fechaEvento = '';
  horaEntrega = '';
  direccionEvento = '';
  telefonoContacto = '';
  observacion = '';

  guardandoPedido = false;
  errorPedido = '';
  mensajePedido = '';

  idTemporal: number | null = null;

  ngOnInit(): void {
    const id = this.route.snapshot.queryParamMap.get('idCliente');
    const temporal = this.route.snapshot.queryParamMap.get('idTemporal');

    if (id !== null) {
      this.idCliente = Number(id);
    }

    if (temporal !== null) {
      this.idTemporal = Number(temporal);
    }

    this.cargarClientes();
    this.cargarProductos();
  }

  cargarClientes(): void {
    this.clienteService.getClientes().subscribe({
      next: (data) => {
        this.clientes = data;

        if (this.idCliente !== null) {
          const cliente = this.clientes.find(
            c => c.idCliente === this.idCliente
          );

          if (cliente) {
            this.seleccionarCliente(cliente);
          }
        }

        if (this.idTemporal !== null) {
          this.cargarPedidoTemporal();
        }
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  get clientesFiltrados(): Cliente[] {
    const texto = this.buscarCliente.toLowerCase().trim();

    return this.clientes.filter(cliente =>
      cliente.persona.nombre.toLowerCase().includes(texto) ||
      cliente.persona.apellidos.toLowerCase().includes(texto) ||
      cliente.persona.dni.includes(texto) ||
      cliente.persona.numTelef.includes(texto)
    );
  }

  buscarOtroCliente(): void {
    this.idCliente = null;
    this.clienteSeleccionado = null;
    this.buscarCliente = '';
    this.telefonoContacto = '';
    this.mostrarClientes = true;
  }

  seleccionarCliente(cliente: Cliente): void {
    this.idCliente = cliente.idCliente ?? null;
    this.clienteSeleccionado = cliente;

    this.buscarCliente =
      cliente.persona.nombre + ' ' + cliente.persona.apellidos;

    this.telefonoContacto = cliente.persona.numTelef;
    this.mostrarClientes = false;
  }

  abrirNuevoCliente(): void {
    this.mostrarClientes = false;
    this.mostrarNuevoCliente = true;
    this.errorCliente = '';
  }

  guardarCliente(): void {
    if (this.guardandoCliente) {
      return;
    }

    if (!this.nombre.trim() || !this.apellidos.trim()) {
      this.errorCliente = 'Completa el nombre y los apellidos.';
      return;
    }

    if (!/^\d{8}$/.test(this.dni)) {
      this.errorCliente = 'El DNI debe tener 8 números.';
      return;
    }

    if (!/^9\d{8}$/.test(this.numTelef)) {
      this.errorCliente = 'El celular debe tener 9 números y empezar con 9.';
      return;
    }

    const cliente: Cliente = {
      persona: {
        nombre: this.nombre.trim(),
        apellidos: this.apellidos.trim(),
        dni: this.dni,
        numTelef: this.numTelef
      },
      direccion: this.direccionCliente.trim()
    };

    this.guardandoCliente = true;
    this.errorCliente = '';

    this.clienteService.crearCliente(cliente).subscribe({
      next: (nuevo) => {
        this.guardandoCliente = false;
        this.mostrarNuevoCliente = false;

        this.nombre = '';
        this.apellidos = '';
        this.dni = '';
        this.numTelef = '';
        this.direccionCliente = '';

        this.seleccionarCliente(nuevo);
        this.cargarClientes();
      },
      error: (err) => {
        this.guardandoCliente = false;
        this.errorCliente =
          err.error?.message || 'No se pudo registrar el cliente.';
      }
    });
  }

  cargarProductos(): void {
    this.productoService.getProductos().subscribe({
      next: (data) => {
        this.productos = data;

        if (this.idTemporal !== null) {
          this.cargarPedidoTemporal();
        }
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  get productosFiltrados(): Producto[] {
    const texto = this.buscarProducto.toLowerCase().trim();

    return this.productos.filter(producto =>
      producto.nombre.toLowerCase().includes(texto)
    );
  }

  seleccionarProducto(producto: Producto): void {
    this.idProducto = producto.idProducto ?? null;
    this.buscarProducto = producto.nombre;
    this.mostrarProductos = false;

    this.cantidad = 1;
    this.unidad = this.obtenerDivisor(producto) === 8 ? 'OCTAVO' : 'CUARTO';
    this.acompanamiento = 'PAPA';
    this.tipoArroz = '';
    this.tipoEnsalada = '';
  }

  get productoSeleccionado(): Producto | undefined {
    return this.productos.find(
      producto => producto.idProducto === this.idProducto
    );
  }

  esPreparado(producto: Producto): boolean {
    const tipo = producto.tipo.toUpperCase().trim();

    return (
      tipo === 'POLLO' || tipo === 'CHULETA'
    ) && producto.nombre.toUpperCase().trim() !== 'POLLO CRUDO';
  }

  obtenerDivisor(producto: Producto): number {
    const nombre = producto.nombre.toUpperCase();

    if (/X\s*8\b/.test(nombre)) {
      return 8;
    }

    return 4;
  }

  unidadValida(producto: Producto, unidad: string): boolean {
    if (!this.esPreparado(producto)) {
      return true;
    }

    const divisor = this.obtenerDivisor(producto);

    return unidad === 'POLLO' ||
      (divisor === 4 && unidad === 'CUARTO') ||
      (divisor === 8 && unidad === 'OCTAVO');
  }

  obtenerUnidadesCompletas(
    producto: Producto,
    cantidad: number,
    unidad: string
  ): number {
    if (unidad === 'CUARTO' || unidad === 'OCTAVO') {
      return cantidad / this.obtenerDivisor(producto);
    }

    return cantidad;
  }

  agregarProducto(): void {
    const producto = this.productoSeleccionado;

    if (
      !producto ||
      producto.idProducto === undefined ||
      !Number.isInteger(this.cantidad) ||
      this.cantidad < 1
    ) {
      return;
    }

    if (!this.unidadValida(producto, this.unidad)) {
      this.errorPedido =
        'La presentación no corresponde al producto seleccionado.';
      return;
    }

    this.errorPedido = '';

    const preparado = this.esPreparado(producto);
    const acompanamiento = preparado ? this.acompanamiento : '';
    const tipoArroz = preparado ? this.tipoArroz : '';
    const tipoEnsalada = preparado ? this.tipoEnsalada : '';

    const existente = this.carrito.find(item =>
      item.producto.idProducto === producto.idProducto &&
      item.unidad === this.unidad &&
      item.acompanamiento === acompanamiento &&
      item.tipoArroz === tipoArroz &&
      item.tipoEnsalada === tipoEnsalada
    );

    if (existente) {
      existente.cantidad += this.cantidad;
    } else {
      this.carrito.push({
        producto,
        cantidad: this.cantidad,
        unidad: this.unidad,
        acompanamiento,
        tipoArroz,
        tipoEnsalada
      });
    }

    this.idProducto = null;
    this.buscarProducto = '';
    this.cantidad = 1;
    this.unidad = 'CUARTO';
    this.acompanamiento = 'PAPA';
    this.tipoArroz = '';
    this.tipoEnsalada = '';
  }

  actualizarCantidad(index: number, cantidad: number): void {
    if (Number.isInteger(cantidad) && cantidad >= 1) {
      this.carrito[index].cantidad = cantidad;
    }
  }

  abrirEditarProducto(index: number): void {
    const item = this.carrito[index];

    this.indiceEditar = index;
    this.cantidadEditar = item.cantidad;
    this.unidadEditar = item.unidad;
    this.acompanamientoEditar = item.acompanamiento;
    this.tipoArrozEditar = item.tipoArroz;
    this.tipoEnsaladaEditar = item.tipoEnsalada;

    this.mostrarEditarProducto = true;
  }

  guardarEdicion(): void {
    if (
      this.indiceEditar < 0 ||
      !Number.isInteger(this.cantidadEditar) ||
      this.cantidadEditar < 1
    ) {
      return;
    }

    const item = this.carrito[this.indiceEditar];

    if (!this.unidadValida(item.producto, this.unidadEditar)) {
      this.errorPedido =
        'La presentación no corresponde al producto seleccionado.';
      return;
    }

    this.errorPedido = '';

    item.cantidad = this.cantidadEditar;
    item.unidad = this.unidadEditar;
    item.acompanamiento = this.acompanamientoEditar;
    item.tipoArroz = this.tipoArrozEditar;
    item.tipoEnsalada = this.tipoEnsaladaEditar;

    this.mostrarEditarProducto = false;
    this.indiceEditar = -1;
  }

  eliminarItem(index: number): void {
    this.carrito.splice(index, 1);
  }

  vaciarCarrito(): void {
    this.carrito = [];
  }

  calcularPorciones(item: ProductoPedido): number {
    if (item.unidad === 'POLLO') {
      return item.cantidad * this.obtenerDivisor(item.producto);
    }

    return item.cantidad;
  }

  precioArroz(tipo: string): number {
    if (tipo === 'ARABE') {
      return 2;
    }

    if (tipo === 'JARDINERA') {
      return 1.80;
    }

    return 0;
  }

  precioEnsalada(tipo: string): number {
    if (tipo === 'BLANCA') {
      return 2;
    }

    if (tipo === 'FRESCA') {
      return 1.80;
    }

    return 0;
  }

  calcularPrecioBase(item: ProductoPedido): number {
    if (!this.esPreparado(item.producto)) {
      return item.producto.precio * item.cantidad;
    }

    const completos = this.obtenerUnidadesCompletas(
      item.producto,
      item.cantidad,
      item.unidad
    );

    return item.producto.precio * completos;
  }

  calcularSubtotal(item: ProductoPedido): number {
    const porciones = this.calcularPorciones(item);
    const arroz = this.precioArroz(item.tipoArroz) * porciones;
    const ensalada = this.precioEnsalada(item.tipoEnsalada) * porciones;

    return Math.round(
      (this.calcularPrecioBase(item) + arroz + ensalada) * 100
    ) / 100;
  }

  calcularTotal(): number {
    return this.carrito.reduce(
      (total, item) => total + this.calcularSubtotal(item),
      0
    );
  }

  cargarPedidoTemporal(): void {
    if (
      this.idTemporal === null ||
      this.clientes.length === 0 ||
      this.productos.length === 0
    ) {
      return;
    }

    const temporal = this.pedidoTemporalService.buscarPedido(
      this.idTemporal
    );

    if (!temporal) {
      this.errorPedido = 'No se encontró el pedido temporal.';
      return;
    }

    const pedido = temporal.pedido;

    const cliente = this.clientes.find(
      c => c.idCliente === pedido.idCliente
    );

    if (cliente) {
      this.seleccionarCliente(cliente);
    }

    this.fechaEvento = pedido.fechaEvento;
    this.horaEntrega = pedido.horaEntrega;
    this.direccionEvento = pedido.direccionEvento;
    this.telefonoContacto = pedido.telefonoContacto;
    this.observacion = pedido.observacion || '';

    this.carrito = pedido.items.flatMap(item => {
      const producto = this.productos.find(
        p => p.idProducto === item.idProducto
      );

      if (!producto) {
        return [];
      }

      return [{
        producto,
        cantidad: item.cantidad,
        unidad: item.unidad,
        acompanamiento: item.acompanamiento || '',
        tipoArroz: item.tipoArroz || '',
        tipoEnsalada: item.tipoEnsalada || ''
      }];
    });
  }

  guardarPedido(): void {
    if (this.guardandoPedido) {
      return;
    }

    this.errorPedido = '';
    this.mensajePedido = '';

    if (this.idCliente === null) {
      this.errorPedido = 'Selecciona un cliente.';
      return;
    }

    if (this.carrito.length === 0) {
      this.errorPedido = 'Agrega al menos un producto.';
      return;
    }

    if (this.carrito.some(item =>
      item.producto.idProducto === undefined ||
      !Number.isInteger(item.cantidad) ||
      item.cantidad < 1
    )) {
      this.errorPedido = 'Revisa las cantidades de los productos.';
      return;
    }

    if (this.carrito.some(item =>
      !this.unidadValida(item.producto, item.unidad)
    )) {
      this.errorPedido =
        'Hay productos con una presentación incorrecta.';
      return;
    }

    if (!this.fechaEvento || !this.horaEntrega) {
      this.errorPedido = 'Completa la fecha y hora de entrega.';
      return;
    }

    if (!this.direccionEvento.trim()) {
      this.errorPedido = 'Ingresa la dirección de entrega.';
      return;
    }

    if (!/^9\d{8}$/.test(this.telefonoContacto)) {
      this.errorPedido = 'Ingresa un celular válido de 9 dígitos.';
      return;
    }

    const pedido: Pedido = {
      idCliente: this.idCliente,
      items: this.carrito.map(item => ({
        idProducto: item.producto.idProducto!,
        cantidad: item.cantidad,
        unidad: item.unidad,
        acompanamiento: item.acompanamiento,
        tipoArroz: item.tipoArroz,
        tipoEnsalada: item.tipoEnsalada
      })),
      fechaEvento: this.fechaEvento,
      horaEntrega: this.horaEntrega,
      direccionEvento: this.direccionEvento.trim(),
      telefonoContacto: this.telefonoContacto,
      observacion: this.observacion.trim()
    };

    const nombreCliente = this.clienteSeleccionado
      ? this.clienteSeleccionado.persona.nombre + ' ' +
        this.clienteSeleccionado.persona.apellidos
      : this.buscarCliente;

    try {
      this.guardandoPedido = true;

      if (this.idTemporal !== null) {
        const existente = this.pedidoTemporalService.buscarPedido(
          this.idTemporal
        );

        if (!existente) {
          this.errorPedido = 'No se encontró el pedido para editar.';
          return;
        }

        this.pedidoTemporalService.editarPedido(
          this.idTemporal,
          pedido
        );

        this.mensajePedido = 'Pedido temporal actualizado.';
      } else {
        this.pedidoTemporalService.guardarPedido(
          nombreCliente,
          pedido
        );

        this.mensajePedido = 'Pedido guardado temporalmente.';
      }

      this.router.navigate(['/pedidos']);
    } catch (err) {
      console.error(err);
      this.errorPedido = 'No se pudo guardar el pedido temporal.';
    } finally {
      this.guardandoPedido = false;
    }
  }
}
