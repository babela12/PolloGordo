
import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { forkJoin, of, catchError, finalize } from 'rxjs';

import { PedidoService } from '../../../core/services/pedido.service';
import { PagoService } from '../../../core/services/pago.service';

import {
  Pago,
  PagoRequest,
  ResumenPago
} from '../../../core/interfaces/pago.interface';

interface PedidoPago {
  idPedido: number;
  cliente: string;
  fechaEvento: string;
  total: number;
  confirmado: string;
  resumen: ResumenPago | null;
}

type EstadoPago = 'PENDIENTE' | 'PARCIAL' | 'PAGADO';

@Component({
  selector: 'app-pago-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pago-list.component.html',
  styleUrl: './pago-list.component.css'
})
export class PagoListComponent implements OnInit, OnDestroy {

  private pedidoService = inject(PedidoService);
  private pagoService = inject(PagoService);
  private sanitizer = inject(DomSanitizer);

  pedidos: PedidoPago[] = [];
  pagos: Pago[] = [];

  pedidoSeleccionado: PedidoPago | null = null;

  busqueda = '';
  pestanaActiva: EstadoPago = 'PENDIENTE';

  cargando = false;
  cargandoHistorial = false;
  guardando = false;
  mostrarFormulario = false;
  mostrarHistorial = false;

  mostrarComprobante = false;
  cargandoComprobante = false;
  urlComprobante: string | null = null;
  urlPdfSeguro: SafeResourceUrl | null = null;
  tipoComprobante: 'imagen' | 'pdf' | null = null;
  errorComprobante = '';
  private solicitudComprobante = 0;

  error = '';
  mensaje = '';

  nuevoPago: PagoRequest = {
    idPedido: 0,
    metodo: 'YAPE',
    monto: 0,
    fechaPago: this.fechaActual(),
    referencia: ''
  };

  archivoSeleccionado: File | null = null;

  ngOnInit(): void {
    this.cargarPedidos();
  }

  ngOnDestroy(): void {
    this.cerrarComprobante();
  }

  private fechaActual(): string {
    const fecha = new Date();
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }

  cargarPedidos(): void {
    this.cargando = true;
    this.error = '';

    this.pedidoService.getPedidos().subscribe({
      next: pedidos => {
        const confirmados = pedidos.filter(
          pedido => pedido.confirmado === 'S'
        );

        if (confirmados.length === 0) {
          this.pedidos = [];
          this.cargando = false;
          return;
        }

        const consultas = confirmados.map(pedido =>
          this.pagoService.obtenerResumen(pedido.idPedido).pipe(
            catchError(() => of(null))
          )
        );

        forkJoin(consultas)
          .pipe(finalize(() => this.cargando = false))
          .subscribe({
            next: resumenes => {
              this.pedidos = confirmados.map((pedido, index) => ({
                idPedido: pedido.idPedido,
                cliente: pedido.cliente,
                fechaEvento: pedido.fechaEvento,
                total: pedido.total,
                confirmado: pedido.confirmado,
                resumen: resumenes[index]
              }));
            },
            error: () => {
              this.error = 'No se pudieron cargar los pagos';
            }
          });
      },
      error: () => {
        this.cargando = false;
        this.error = 'No se pudieron cargar los pedidos';
      }
    });
  }

  get pedidosFiltrados(): PedidoPago[] {
    const texto = this.busqueda.toLowerCase().trim();

    return this.pedidos.filter(pedido => {
      const coincideBusqueda =
        pedido.cliente.toLowerCase().includes(texto) ||
        pedido.idPedido.toString().includes(texto);

      return coincideBusqueda &&
        pedido.resumen?.estadoPago === this.pestanaActiva;
    });
  }

  contarPorEstado(estado: EstadoPago): number {
    return this.pedidos.filter(
      pedido => pedido.resumen?.estadoPago === estado
    ).length;
  }

  get totalPedidos(): number {
    return this.pedidos.reduce(
      (total, pedido) => total + pedido.total, 0
    );
  }

  get totalCobrado(): number {
    return this.pedidos.reduce(
      (total, pedido) =>
        total + (pedido.resumen?.totalAbonado || 0), 0
    );
  }

  get totalPendiente(): number {
    return this.pedidos.reduce(
      (total, pedido) =>
        total + (pedido.resumen?.saldoPendiente || 0), 0
    );
  }

  get resumenesNoDisponibles(): number {
    return this.pedidos.filter(
      pedido => pedido.resumen === null
    ).length;
  }

  abrirFormulario(pedido: PedidoPago): void {
    if (!pedido.resumen ||
        pedido.resumen.saldoPendiente <= 0) {
      return;
    }

    this.pedidoSeleccionado = pedido;

    this.nuevoPago = {
      idPedido: pedido.idPedido,
      metodo: 'YAPE',
      monto: 0,
      fechaPago: this.fechaActual(),
      referencia: ''
    };

    this.archivoSeleccionado = null;
    this.error = '';
    this.mensaje = '';
    this.mostrarFormulario = true;
  }

  verHistorial(pedido: PedidoPago): void {
    this.pedidoSeleccionado = pedido;
    this.pagos = [];
    this.error = '';
    this.cargandoHistorial = true;
    this.mostrarHistorial = true;

    this.pagoService.listarPorPedido(pedido.idPedido)
      .pipe(finalize(() => this.cargandoHistorial = false))
      .subscribe({
        next: pagos => {
          this.pagos = pagos;
        },
        error: () => {
          this.error = 'No se pudo cargar el historial de pagos';
        }
      });
  }

  seleccionarArchivo(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0] || null;

    if (archivo && archivo.size > 5 * 1024 * 1024) {
      this.error = 'El comprobante no puede superar los 5 MB';
      this.archivoSeleccionado = null;
      input.value = '';
      return;
    }

    if (archivo && ![
      'image/jpeg',
      'image/png',
      'application/pdf'
    ].includes(archivo.type)) {
      this.error = 'Solo se permiten archivos JPG, PNG o PDF';
      this.archivoSeleccionado = null;
      input.value = '';
      return;
    }

    this.archivoSeleccionado = archivo;
    this.error = '';
  }

  registrarPago(): void {
    if (!this.pedidoSeleccionado?.resumen || this.guardando) {
      return;
    }

    const saldo = this.pedidoSeleccionado.resumen.saldoPendiente;

    if (!this.nuevoPago.monto ||
        !Number.isFinite(this.nuevoPago.monto) ||
        this.nuevoPago.monto <= 0 ||
        this.nuevoPago.monto > saldo) {
      this.error = 'El monto debe ser mayor a cero y no superar el saldo';
      return;
    }

    this.guardando = true;
    this.error = '';
    this.mensaje = '';

    this.pagoService.crear(this.nuevoPago).subscribe({
      next: pago => {
        if (this.archivoSeleccionado) {
          this.pagoService.subirEvidencia(
            pago.idPago,
            this.archivoSeleccionado
          ).subscribe({
            next: () => this.finalizarRegistro(),
            error: () => {
              this.guardando = false;
              this.mostrarFormulario = false;
              this.mensaje =
                'El pago se registró, pero no se pudo subir el comprobante.';
              this.cargarPedidos();
            }
          });
        } else {
          this.finalizarRegistro();
        }
      },
      error: err => {
        this.guardando = false;
        this.error = err.error?.mensaje ||
          'No se pudo registrar el pago';
      }
    });
  }

  private finalizarRegistro(): void {
    this.guardando = false;
    this.mostrarFormulario = false;
    this.mensaje = '¡Pago registrado correctamente!';
    this.cargarPedidos();
  }

  verComprobante(pago: Pago): void {
    this.cerrarComprobante();

    const solicitudActual = this.solicitudComprobante;

    this.mostrarComprobante = true;
    this.cargandoComprobante = true;
    this.errorComprobante = '';

    this.pagoService.obtenerEvidencia(pago.idPago).subscribe({
      next: blob => {
        if (solicitudActual !== this.solicitudComprobante) {
          return;
        }

        const nombre = (pago.evidencia || '').toLowerCase();

        const esPdf =
          blob.type.toLowerCase().includes('application/pdf') ||
          nombre.endsWith('.pdf');

        const esImagen =
          blob.type.toLowerCase().startsWith('image/') ||
          /\.(jpg|jpeg|png)$/.test(nombre);

        if (!esPdf && !esImagen) {
          this.errorComprobante =
            'El archivo no tiene un formato compatible con la vista previa.';
          this.cargandoComprobante = false;
          return;
        }

        this.tipoComprobante = esPdf ? 'pdf' : 'imagen';

        const tipoMime = esPdf
          ? 'application/pdf'
          : nombre.endsWith('.png')
            ? 'image/png'
            : blob.type.startsWith('image/')
              ? blob.type
              : 'image/jpeg';

        const archivo = new Blob([blob], { type: tipoMime });

        this.urlComprobante = URL.createObjectURL(archivo);

        if (esPdf) {
          this.urlPdfSeguro =
            this.sanitizer.bypassSecurityTrustResourceUrl(
              this.urlComprobante
            );
        }

        this.cargandoComprobante = false;
      },
      error: () => {
        if (solicitudActual !== this.solicitudComprobante) {
          return;
        }

        this.cargandoComprobante = false;
        this.errorComprobante =
          'No se pudo visualizar el comprobante.';
      }
    });
  }

  cerrarComprobante(): void {
    this.solicitudComprobante++;

    if (this.urlComprobante) {
      URL.revokeObjectURL(this.urlComprobante);
    }

    this.urlComprobante = null;
    this.urlPdfSeguro = null;
    this.tipoComprobante = null;
    this.mostrarComprobante = false;
    this.cargandoComprobante = false;
    this.errorComprobante = '';
  }
}
