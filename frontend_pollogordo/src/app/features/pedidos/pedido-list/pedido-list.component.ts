import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';
import { forkJoin } from 'rxjs';
import { PagoFormComponent } from '../../pagos/pago-form/pago-form.component';
import { Trabajador } from '../../../core/interfaces/trabajador.interface';
import { Vehiculo } from '../../../core/interfaces/vehiculo.interface';
import { AsigTrabajador } from '../../../core/interfaces/asig-trabajador.interface';
import { TrabajadorService } from '../../../core/services/trabajador.service';
import { VehiculoService } from '../../../core/services/vehiculo.service';
import { AsigTrabajadorService } from '../../../core/services/asig-trabajador.service';
import { AsignacionFormComponent } from '../../asignaciones/asignacion-form/asignacion-form.component';
import { Producto } from '../../../core/interfaces/producto.interface';
import { Insumo } from '../../../core/interfaces/insumo.interface';
import { ProductoService } from '../../../core/services/producto.service';
import { InsumoService } from '../../../core/services/insumo.service';
import { PedidoService } from '../../../core/services/pedido.service';
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
interface ProductoConfirmado {
  producto: string;
  cantidad: number;
  unidad: string;
  cantidadPollos?: number;
  precioUnitario: number;
  subtotal: number;
  acompanamiento?: string;
  tipoArroz?: string;
  tipoEnsalada?: string;
}
interface PedidoConfirmado {
  idPedido: number;
  cliente: string;
  total: number;
  estado: string;
  fecha: string;
  fechaEvento: string;
  horaEntrega: string;
  direccionEvento: string;
  telefonoContacto: string;
  confirmado: string;
  fechaConfirmacion?: string;
  observacion?: string;
  productos: ProductoConfirmado[];
}
@Component({
  selector: 'app-pedido-list',
  standalone: true,
  imports: [CommonModule, FormsModule, AsignacionFormComponent, PagoFormComponent],
  templateUrl: './pedido-list.component.html',
  styleUrl: './pedido-list.component.css'
})
export class PedidoListComponent implements OnInit {
  private pedidoTemporalService = inject(PedidoTemporalService);
  private pedidoService = inject(PedidoService);
  private productoService = inject(ProductoService);
  private insumoService = inject(InsumoService);
  private router = inject(Router);
  private trabajadorService = inject(TrabajadorService);
  private vehiculoService = inject(VehiculoService);
  private asignacionService = inject(AsigTrabajadorService);
  // El rol se obtiene del JWT de la sesión actual.
  // Si el token no declara el rol, se deniega la gestión de personal.
  get esGerente(): boolean {
    const token = localStorage.getItem('token_ecommerce');
    if (!token) return false;
    try {
      const parte = token.split('.')[1];
      if (!parte) return false;
      const base64 = parte.replace(/-/g, '+').replace(/_/g, '/');
      const datos = JSON.parse(atob(base64)) as Record<string, unknown>;
      const valores = [datos['rol'], datos['role'], datos['roles'], datos['authorities'], datos['authoritiesNames']];
      return valores.some(valor => {
        if (Array.isArray(valor)) {
          return valor.some(v => String(typeof v === 'object' && v !== null ? (v as Record<string, unknown>)['authority'] ?? '' : v).toUpperCase().replace('ROLE_', '') === 'GERENTE');
        }
        return String(valor ?? '').toUpperCase().replace('ROLE_', '') === 'GERENTE';
      });
    } catch {
      return false;
    }
  }

  productos: Producto[] = [];
  insumos: Insumo[] = [];
  buscar = '';
  pedidoSeleccionado: number | null = null;
  confirmandoPedido = false;
  // PESTAÑAS
  pestanaActiva: 'pendientes' | 'confirmados' | 'finalizados' | 'historial' = 'pendientes';
  pedidosOracle: PedidoConfirmado[] = [];
  pedidoConfirmadoSeleccionado: PedidoConfirmado | null = null;
  cargandoConfirmados = false;
  cargandoDetalleConfirmado = false;
  errorConfirmados = '';
  trabajadoresDisponibles: Trabajador[] = [];
  vehiculosDisponibles: Vehiculo[] = [];
  asignacionesPedido: AsigTrabajador[] = [];
  mostrarFormularioAsignacion = false;
  cargandoAsignaciones = false;
  errorAsignaciones = "";
  get totalPollosAsignacion(): number {
    return (this.pedidoConfirmadoSeleccionado?.productos ?? []).reduce((s, p) => s + Number(p.cantidadPollos ?? 0), 0);
  }
  nombreTrabajador(trabajador: Trabajador): string {
    const persona = trabajador.persona as unknown as Record<string, unknown>;
    const nombre = [persona?.['nombres'] ?? persona?.['nombre'], persona?.['apellidos'] ?? persona?.['apellido']].filter(Boolean).join(' ');
    return nombre || `Trabajador #${trabajador.idTrabajador}`;
  }
  nombreTrabajadorPorId(id?: number): string {
    const trabajador = this.trabajadoresDisponibles.find(t => t.idTrabajador === id);
    return trabajador ? this.nombreTrabajador(trabajador) : `Trabajador #${id ?? '—'}`;
  }
  placaVehiculoPorId(id?: number): string {
    const vehiculo = this.vehiculosDisponibles.find(v => v.idVehiculo === id);
    return vehiculo ? `${vehiculo.placa} (${vehiculo.tipo})` : `Vehículo #${id ?? '—'}`;
  }
  cargarAsignacionesPedido(idPedido: number): void {
    if (!this.esGerente) {
      this.asignacionesPedido = [];
      this.trabajadoresDisponibles = [];
      this.vehiculosDisponibles = [];
      this.errorAsignaciones = '';
      this.cargandoAsignaciones = false;
      return;
    }
    this.cargandoAsignaciones = true;
    this.errorAsignaciones = '';
    forkJoin({
      trabajadores: this.trabajadorService.getTrabajadores(),
      vehiculos: this.vehiculoService.getVehiculos(),
      asignaciones: this.asignacionService.getAsignacionesPorPedido(idPedido)
    }).subscribe({
      next: ({ trabajadores, vehiculos, asignaciones }) => {
        if (this.pedidoConfirmadoSeleccionado?.idPedido !== idPedido) return;
        this.trabajadoresDisponibles = trabajadores;
        this.vehiculosDisponibles = vehiculos;
        this.asignacionesPedido = asignaciones;
        this.cargandoAsignaciones = false;
      },
      error: (error) => {
        if (this.pedidoConfirmadoSeleccionado?.idPedido !== idPedido) return;
        console.error('Error cargando asignaciones:', error);
        this.errorAsignaciones = 'No se pudieron consultar trabajadores, vehículos o asignaciones.';
        this.cargandoAsignaciones = false;
      }
    });
  }
  abrirFormularioAsignacion(): void {
    if (!this.esGerente || !this.pedidoConfirmadoSeleccionado || this.cargandoAsignaciones || this.asignacionesPedido.length > 0) return;
    this.mostrarFormularioAsignacion = true;
  }
  cerrarFormularioAsignacion(): void { this.mostrarFormularioAsignacion = false; }
  asignacionGuardada(): void {
    this.mostrarFormularioAsignacion = false;
    if (this.pedidoConfirmadoSeleccionado) this.cargarAsignacionesPedido(this.pedidoConfirmadoSeleccionado.idPedido);
  }
  ngOnInit(): void {
    this.cargarProductos();
    this.cargarInsumos();
    this.cargarPedidosConfirmados();
  }
  // PRODUCTOS E INSUMOS
  cargarProductos(): void {
    this.productoService.getProductos().subscribe({
      next: (data) => {
        this.productos = data;
      },
      error: (err) => {
        console.error('Error al cargar productos:', err);
      }
    });
  }
  cargarInsumos(): void {
    this.insumoService.getInsumos().subscribe({
      next: (data) => {
        this.insumos = data;
      },
      error: (err) => {
        console.error('Error al cargar insumos:', err);
      }
    });
  }
  // PEDIDOS TEMPORALES
  get pedidos(): PedidoTemporal[] {
    return this.pedidoTemporalService.getPedidos();
  }
  get pedidosFiltrados(): PedidoTemporal[] {
    const texto = this.buscar.toLowerCase().trim();
    return this.pedidos.filter(item =>
      item.nombreCliente.toLowerCase().includes(texto) ||
      String(item.pedido.telefonoContacto ?? '').includes(texto) ||
      String(item.pedido.fechaEvento ?? '').includes(texto)
    );
  }
  get pedidoActual(): PedidoTemporal | undefined {
    return this.pedidos.find(
      pedido => pedido.idTemporal === this.pedidoSeleccionado
    );
  }
  verPedido(id: number): void {
    this.pedidoSeleccionado = id;
  }
  volverListado(): void {
    this.pedidoSeleccionado = null;
  }
  editarPedido(id: number): void {
    this.router.navigate(['/pedidos/nuevo'], {
      queryParams: { idTemporal: id }
    });
  }
  async eliminarPedido(id: number): Promise<void> {
    const idOracle = this.obtenerIdRegistrado(id);
    if (idOracle !== null) {
      await Swal.fire({
        title: 'Pedido registrado',
        text: `Este pedido ya tiene el número ${idOracle} en Oracle. Verifica su estado antes de descartarlo.`,
        icon: 'warning',
        confirmButtonColor: '#7B1E21'
      });
      return;
    }
    const resultado = await Swal.fire({
      title: '¿Eliminar pedido?',
      text: 'El pedido se eliminará de la lista de pendientes.',
      icon: 'warning',
      iconColor: '#7B1E21',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#7B1E21',
      cancelButtonColor: '#858585',
      reverseButtons: true
    });
    if (!resultado.isConfirmed) {
      return;
    }
    this.pedidoTemporalService.eliminarPedido(id);
    if (this.pedidoSeleccionado === id) {
      this.pedidoSeleccionado = null;
    }
    Swal.fire({
      title: 'Pedido eliminado',
      text: 'El pedido temporal fue eliminado correctamente.',
      icon: 'success',
      confirmButtonText: 'Continuar',
      confirmButtonColor: '#7B1E21'
    });
  }
  nuevoPedido(): void {
    this.router.navigate(['/pedidos/nuevo']);
  }
  // PESTAÑAS Y PEDIDOS CONFIRMADOS
  cambiarPestana(pestana: 'pendientes' | 'confirmados' | 'finalizados' | 'historial'): void {
    this.pestanaActiva = pestana;
    this.pedidoSeleccionado = null;
    this.pedidoConfirmadoSeleccionado = null;
    this.buscar = '';
    if (pestana !== 'pendientes') {
      this.cargarPedidosConfirmados();
    }
  }
  cargarPedidosConfirmados(): void {
    this.cargandoConfirmados = true;
    this.errorConfirmados = '';
    this.pedidoService.getPedidos().subscribe({
      next: (data) => {
        const lista = Array.isArray(data) ? data : [];
        this.pedidosOracle = lista.filter(
          (pedido: PedidoConfirmado) =>
            String(pedido.confirmado).toUpperCase() === 'S'
        );
        this.cargandoConfirmados = false;
      },
      error: (err) => {
        console.error('Error al listar pedidos:', err);
        this.errorConfirmados = 'No se pudieron cargar los pedidos confirmados.';
        this.cargandoConfirmados = false;
      }
    });
  }
  // CLASIFICACIÓN AUTOMÁTICA POR FECHA
  private fechaLocalHoy(): string {
    const hoy = new Date();
    const anio = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
  esPedidoFinalizado(pedido: PedidoConfirmado): boolean {
    const fecha = String(pedido.fechaEvento ?? '').slice(0, 10);
    return Boolean(fecha) && fecha < this.fechaLocalHoy();
  }
  get cantidadConfirmadosActivos(): number {
    return this.pedidosOracle.filter(
      pedido => !this.esPedidoFinalizado(pedido)
    ).length;
  }
  get cantidadFinalizados(): number {
    return this.pedidosOracle.filter(
      pedido => this.esPedidoFinalizado(pedido)
    ).length;
  }
  get pedidosConfirmadosFiltrados(): PedidoConfirmado[] {
    const texto = this.buscar.toLowerCase().trim();
    return this.pedidosOracle.filter(pedido => {
      const coincideBusqueda =
        String(pedido.idPedido ?? '').includes(texto) ||
        String(pedido.cliente ?? '').toLowerCase().includes(texto) ||
        String(pedido.telefonoContacto ?? '').includes(texto) ||
        String(pedido.fechaEvento ?? '').includes(texto);
      if (!coincideBusqueda) {
        return false;
      }
      if (this.pestanaActiva === 'confirmados') {
        return !this.esPedidoFinalizado(pedido);
      }
      if (this.pestanaActiva === 'finalizados') {
        return this.esPedidoFinalizado(pedido);
      }
      return this.pestanaActiva === 'historial';
    });
  }
  verPedidoConfirmado(id: number): void {
    this.cargandoDetalleConfirmado = true;
    this.pedidoService.getPedidoById(id).subscribe({
      next: (pedido: PedidoConfirmado) => {
        this.pedidoConfirmadoSeleccionado = pedido;
        this.cargandoDetalleConfirmado = false;
            this.mostrarFormularioAsignacion = false;
        this.asignacionesPedido = [];
        this.errorAsignaciones = '';
        if (this.esGerente) {
          this.cargarAsignacionesPedido(pedido.idPedido);
        }
      },
      error: (err) => {
        this.cargandoDetalleConfirmado = false;
        console.error('Error al consultar pedido:', err);
        Swal.fire({
          title: 'No se pudo abrir el pedido',
          text: 'Intenta nuevamente.',
          icon: 'error',
          confirmButtonColor: '#7B1E21'
        });
      }
    });
  }
  volverConfirmados(): void {
    this.pedidoConfirmadoSeleccionado = null;
  }
  // CÁLCULOS DE PEDIDOS CONFIRMADOS
  totalProductosConfirmados(pedido: PedidoConfirmado): number {
    return pedido.productos.reduce(
      (total, producto) => total + Number(producto.cantidad ?? 0),
      0
    );
  }
  importe(valor: number | null | undefined): string {
    return Number(valor ?? 0).toFixed(2);
  }
  // CÁLCULOS DEL CARRITO
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
      this.calcularPrecioBase(idProducto, cantidad, unidad) +
      this.calcularSubtotalArroz(idProducto, cantidad, unidad, tipoArroz) +
      this.calcularSubtotalEnsalada(idProducto, cantidad, unidad, tipoEnsalada)
    ) * 100) / 100;
  }
  calcularTotalBase(pedido: PedidoTemporal): number {
    return pedido.pedido.items.reduce(
      (total, item) =>
        total + this.calcularPrecioBase(
          item.idProducto,
          item.cantidad,
          item.unidad
        ),
      0
    );
  }
  calcularTotalArroz(pedido: PedidoTemporal): number {
    return pedido.pedido.items.reduce(
      (total, item) =>
        total + this.calcularSubtotalArroz(
          item.idProducto,
          item.cantidad,
          item.unidad,
          item.tipoArroz
        ),
      0
    );
  }
  calcularTotalEnsalada(pedido: PedidoTemporal): number {
    return pedido.pedido.items.reduce(
      (total, item) =>
        total + this.calcularSubtotalEnsalada(
          item.idProducto,
          item.cantidad,
          item.unidad,
          item.tipoEnsalada
        ),
      0
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
  // IMÁGENES DE ACOMPAÑAMIENTOS
  obtenerImagenAcompanamiento(nombre: string): string {
    const insumo = this.insumos.find(
      item =>
        item.nombre.toUpperCase().trim() ===
        nombre.toUpperCase().trim()
    );
    return '/pollogordo_api/images/' +
      (insumo?.imagen || 'no-image.png');
  }
  // FILAS DEL CARRITO
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
        const precio = this.calcularPrecioArroz(item.tipoArroz);
        filas.push({
          nombre: nombreArroz,
          imagen: this.obtenerImagenAcompanamiento(nombreArroz),
          precio,
          cantidad: porciones,
          subtotal: Math.round(precio * porciones * 100) / 100,
          detalle: 'Acompañamiento'
        });
      }
      if (preparado && item.tipoEnsalada) {
        const nombreEnsalada = item.tipoEnsalada === 'BLANCA'
          ? 'Ensalada blanca'
          : 'Ensalada fresca';
        const precio = this.calcularPrecioEnsalada(item.tipoEnsalada);
        filas.push({
          nombre: nombreEnsalada,
          imagen: this.obtenerImagenAcompanamiento(nombreEnsalada),
          precio,
          cantidad: porciones,
          subtotal: Math.round(precio * porciones * 100) / 100,
          detalle: 'Acompañamiento'
        });
      }
    }
    return filas;
  }
  // RELACIÓN ENTRE PEDIDO TEMPORAL Y ORACLE
  private obtenerIdRegistrado(idTemporal: number): number | null {
    const valor = localStorage.getItem(
      'pedido_oracle_' + idTemporal
    );
    if (!valor) {
      return null;
    }
    const id = Number(valor);
    return Number.isInteger(id) && id > 0
      ? id
      : null;
  }
  private guardarIdRegistrado(
    idTemporal: number,
    idPedido: number
  ): void {
    localStorage.setItem(
      'pedido_oracle_' + idTemporal,
      String(idPedido)
    );
  }
  // CONFIRMAR PEDIDO
  async confirmarPedido(id: number): Promise<void> {
    if (this.confirmandoPedido) {
      return;
    }
    const pedidoTemporal =
      this.pedidoTemporalService.buscarPedido(id);
    if (!pedidoTemporal) {
      await Swal.fire({
        title: 'Pedido no encontrado',
        text: 'No se encontró el pedido temporal.',
        icon: 'error',
        confirmButtonColor: '#7B1E21'
      });
      return;
    }
    const resultado = await Swal.fire({
      title: '¿Deseas confirmar este pedido?',
      text: 'Una vez confirmado, el pedido quedará registrado y listo para su gestión.',
      icon: 'question',
      iconColor: '#7B1E21',
      showCancelButton: true,
      confirmButtonText: 'Confirmar pedido',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#7B1E21',
      cancelButtonColor: '#858585',
      reverseButtons: true,
      background: '#FFFFFF',
      color: '#333333'
    });
    if (!resultado.isConfirmed) {
      return;
    }
    this.confirmandoPedido = true;
    const idExistente = this.obtenerIdRegistrado(id);
    if (idExistente !== null) {
      this.confirmarEnOracle(id, idExistente);
      return;
    }
    this.pedidoService
      .crearPedido(pedidoTemporal.pedido)
      .subscribe({
        next: (pedidoRegistrado) => {
          const idPedido = Number(
            pedidoRegistrado?.idPedido
          );
          if (!Number.isInteger(idPedido) || idPedido <= 0) {
            this.confirmandoPedido = false;
            Swal.fire({
              title: 'Revisar pedido',
              text: 'El servidor respondió sin un ID válido. Verifica Oracle antes de volver a intentarlo.',
              icon: 'warning',
              confirmButtonColor: '#7B1E21'
            });
            return;
          }
          this.guardarIdRegistrado(id, idPedido);
          this.confirmarEnOracle(id, idPedido);
        },
        error: (err) => {
          this.confirmandoPedido = false;
          console.error('Error al registrar pedido:', err);
          Swal.fire({
            title: 'No se pudo completar el registro',
            text: 'Verifica si el pedido existe en Oracle antes de reintentar. ' +
              (err.error?.message || ''),
            icon: 'error',
            confirmButtonColor: '#7B1E21'
          });
        }
      });
  }
  private confirmarEnOracle(
    idTemporal: number,
    idPedido: number
  ): void {
    this.pedidoService
      .confirmarPedido(idPedido)
      .subscribe({
        next: () => {
          this.confirmandoPedido = false;
          this.pedidoTemporalService
            .eliminarPedido(idTemporal);
          localStorage.removeItem(
            'pedido_oracle_' + idTemporal
          );
          this.pedidoSeleccionado = null;
          this.cambiarPestana('confirmados');
          Swal.fire({
            title: '¡Pedido confirmado con éxito!',
            html: `El pedido <b>N.º ${idPedido}</b> se ha registrado correctamente y ya está listo para su gestión.`,
            icon: 'success',
            confirmButtonText: 'Continuar',
            confirmButtonColor: '#7B1E21',
            background: '#FFFFFF',
            color: '#333333'
          });
        },
        error: (err) => {
          this.confirmandoPedido = false;
          console.error('Error al confirmar pedido:', err);
          Swal.fire({
            title: 'Pedido pendiente de verificar',
            text: `El pedido N.º ${idPedido} ya está registrado en Oracle. Verifica su estado antes de reintentar la confirmación.`,
            icon: 'warning',
            confirmButtonColor: '#7B1E21'
          });
        }
      });
  }
  // IMÁGENES DE PRODUCTOS CONFIRMADOS
  imagenProductoConfirmado(nombre: string): string {
    const normalizar = (texto: string): string =>
      (texto || '')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toUpperCase()
        .replace(/\s*[×X]\s*\d+\s*$/i, '')
        .replace(/\s+/g, ' ')
        .trim();
    const nombreBuscado = normalizar(nombre);
    const producto = this.productos.find(p =>
      normalizar(p.nombre) === nombreBuscado
    );
    if (producto) {
      return this.getImagen(producto);
    }
    return '/pollogordo_api/images/no-image.png';
  }
}