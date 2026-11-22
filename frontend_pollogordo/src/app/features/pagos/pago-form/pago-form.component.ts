import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import Swal from 'sweetalert2';
import { Pago, PagoRequest, ResumenPago } from '../../../core/interfaces/pago.interface';
import { PagoService } from '../../../core/services/pago.service';
interface PedidoPago {
  idPedido: number;
  total: number;
  cliente: string;
}

@Component({
  selector: 'app-pago-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pago-form.component.html',
  styleUrl: './pago-form.component.css'
})
export class PagoFormComponent implements OnChanges {
  @Input() pedido: PedidoPago | null = null;
  private pagoService = inject(PagoService);
  private sanitizer = inject(DomSanitizer);
  // CONTROL DE PAGOS
  resumenPago: ResumenPago | null = null;
  pagosPedido: Pago[] = [];
  cargandoPagos = false;
  errorPagos = '';
  guardandoPago = false;
  metodoPago: PagoRequest['metodo'] = 'YAPE';
  montoPago: number | null = null;
  referenciaPago = '';
  mostrarFormularioPago = false;
  archivoComprobante: File | null = null;
  subiendoComprobante = false;
  pagoPendienteEvidencia: number | null = null;
  mostrarModalComprobante = false;
  urlComprobante: string | null = null;
  urlPdfSeguro: SafeResourceUrl | null = null;
  tipoComprobante: 'imagen' | 'pdf' | null = null;
  cargandoComprobante = false;
  ngOnChanges(): void {
    if (this.pedido) this.cargarPagosPedido(this.pedido.idPedido);
    else this.limpiarControlPagos();
  }
  importe(valor: number | null | undefined): string {
    return Number(valor || 0).toFixed(2);
  }
  // CONTROL DE PAGOS
  limpiarControlPagos(): void {
    this.cerrarModalComprobante();
    this.resumenPago = null;
    this.pagosPedido = [];
    this.cargandoPagos = false;
    this.errorPagos = '';
    this.guardandoPago = false;
    this.subiendoComprobante = false;
    this.mostrarFormularioPago = false;
    this.metodoPago = 'YAPE';
    this.montoPago = null;
    this.referenciaPago = '';
    this.archivoComprobante = null;
    this.pagoPendienteEvidencia = null;
  }
  cargarPagosPedido(idPedido: number): void {
    this.resumenPago = null;
    this.pagosPedido = [];
    this.errorPagos = '';
    this.cargandoPagos = true;
    this.pagoService.obtenerResumen(idPedido).subscribe({
      next: (resumen) => {
        if (this.pedido?.idPedido !== idPedido) {
          return;
        }
        this.resumenPago = resumen;
        this.cargandoPagos = false;
      },
      error: (err) => {
        if (this.pedido?.idPedido !== idPedido) {
          return;
        }
        console.error('Error al consultar resumen de pagos:', err);
        this.errorPagos = 'No se pudo consultar el resumen de pagos.';
        this.cargandoPagos = false;
      }
    });
    this.pagoService.listarPorPedido(idPedido).subscribe({
      next: (pagos) => {
        if (this.pedido?.idPedido !== idPedido) {
          return;
        }
        this.pagosPedido = Array.isArray(pagos) ? pagos : [];
      },
      error: (err) => {
        if (this.pedido?.idPedido !== idPedido) {
          return;
        }
        console.error('Error al consultar historial:', err);
        this.errorPagos = 'No se pudo consultar el historial de pagos.';
      }
    });
  }
  get porcentajePagado(): number {
    if (!this.resumenPago || this.resumenPago.totalPedido <= 0) {
      return 0;
    }
    const porcentaje = this.resumenPago.totalAbonado / this.resumenPago.totalPedido * 100;
    return Math.min(100, Math.max(0, porcentaje));
  }
  abrirFormularioPago(): void {
    if (!this.resumenPago || this.resumenPago.saldoPendiente <= 0) {
      return;
    }
    this.metodoPago = 'YAPE';
    this.montoPago = null;
    this.referenciaPago = '';
    this.archivoComprobante = null;
    this.pagoPendienteEvidencia = null;
    this.mostrarFormularioPago = true;
  }
  cerrarFormularioPago(): void {
    if (this.guardandoPago || this.subiendoComprobante) {
      return;
    }
    if (this.pagoPendienteEvidencia !== null) {
      Swal.fire({
        title: 'Comprobante pendiente',
        text: 'El pago ya fue registrado. Intenta subir el comprobante antes de cerrar.',
        icon: 'warning',
        confirmButtonColor: '#7B1E21'
      });
      return;
    }
    this.mostrarFormularioPago = false;
    this.archivoComprobante = null;
  }
  seleccionarComprobante(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;
    if (!archivo) {
      this.archivoComprobante = null;
      return;
    }
    const tiposPermitidos = ['image/jpeg', 'image/png', 'application/pdf'];
    const extensionValida = /\.(jpg|jpeg|png|pdf)$/i.test(archivo.name);
    const tamanoMaximo = 5 * 1024 * 1024;
    if (!tiposPermitidos.includes(archivo.type) || !extensionValida) {
      input.value = '';
      this.archivoComprobante = null;
      Swal.fire({
        title: 'Archivo no permitido',
        text: 'Selecciona una imagen JPG, PNG o un PDF.',
        icon: 'warning',
        confirmButtonColor: '#7B1E21'
      });
      return;
    }
    if (archivo.size > tamanoMaximo) {
      input.value = '';
      this.archivoComprobante = null;
      Swal.fire({
        title: 'Archivo demasiado grande',
        text: 'El comprobante no debe superar los 5 MB.',
        icon: 'warning',
        confirmButtonColor: '#7B1E21'
      });
      return;
    }
    this.archivoComprobante = archivo;
  }
  quitarComprobante(): void {
    this.archivoComprobante = null;
  }
  async registrarPago(): Promise<void> {
    const pedido = this.pedido;
    const resumen = this.resumenPago;
    if (!pedido || !resumen || this.guardandoPago || this.subiendoComprobante) {
      return;
    }
    if (this.pagoPendienteEvidencia !== null) {
      await this.reintentarSubidaComprobante();
      return;
    }
    const monto = Number(this.montoPago);
    const montoCentimos = Math.round(monto * 100);
    const saldoCentimos = Math.round(Number(resumen.saldoPendiente) * 100);
    if (this.montoPago === null || !Number.isFinite(monto) || monto <= 0 || montoCentimos <= 0) {
      await Swal.fire({
        title: 'Monto inválido',
        text: 'Ingresa un monto mayor a cero.',
        icon: 'warning',
        confirmButtonColor: '#7B1E21'
      });
      return;
    }
    if (Math.abs(monto * 100 - montoCentimos) > 0.000001) {
      await Swal.fire({
        title: 'Monto inválido',
        text: 'Solo se permiten dos decimales.',
        icon: 'warning',
        confirmButtonColor: '#7B1E21'
      });
      return;
    }
    if (montoCentimos > saldoCentimos) {
      await Swal.fire({
        title: 'Monto excedido',
        text: `El saldo pendiente es S/ ${this.importe(resumen.saldoPendiente)}.`,
        icon: 'warning',
        confirmButtonColor: '#7B1E21'
      });
      return;
    }
    const confirmacion = await Swal.fire({
      title: '¿Registrar pago?',
      text: `Se registrará S/ ${this.importe(monto)} mediante ${this.metodoPago}.`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, registrar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#7B1E21',
      cancelButtonColor: '#858585'
    });
    if (!confirmacion.isConfirmed) {
      return;
    }
    if (this.pedido?.idPedido !== pedido.idPedido) {
      return;
    }
    const solicitud: PagoRequest = {
      idPedido: pedido.idPedido,
      metodo: this.metodoPago,
      monto: montoCentimos / 100,
      referencia: this.referenciaPago.trim() || null
    };
    this.guardandoPago = true;
    this.pagoService.crear(solicitud).subscribe({
      next: (pagoRegistrado) => {
        this.guardandoPago = false;
        if (this.pedido?.idPedido !== pedido.idPedido) {
          return;
        }
        this.cargarPagosPedido(pedido.idPedido);
        if (this.archivoComprobante) {
          this.pagoPendienteEvidencia = pagoRegistrado.idPago;
          this.subirComprobanteRegistrado(pagoRegistrado.idPago, pedido.idPedido);
        } else {
          this.mostrarFormularioPago = false;
          Swal.fire({
            title: '¡Pago registrado!',
            text: 'El abono se guardó correctamente.',
            icon: 'success',
            confirmButtonColor: '#7B1E21'
          });
        }
      },
      error: (err) => {
        this.guardandoPago = false;
        console.error('Error al registrar pago:', err);
        Swal.fire({
          title: 'No se pudo registrar el pago',
          text: err.error?.message || 'Verifica los datos e intenta nuevamente.',
          icon: 'error',
          confirmButtonColor: '#7B1E21'
        });
      }
    });
  }
  private subirComprobanteRegistrado(idPago: number, idPedido: number): void {
    if (!this.archivoComprobante) {
      return;
    }
    this.subiendoComprobante = true;
    this.pagoService.subirEvidencia(idPago, this.archivoComprobante).subscribe({
      next: () => {
        this.subiendoComprobante = false;
        this.pagoPendienteEvidencia = null;
        this.archivoComprobante = null;
        this.mostrarFormularioPago = false;
        if (this.pedido?.idPedido === idPedido) {
          this.cargarPagosPedido(idPedido);
        }
        Swal.fire({
          title: '¡Pago registrado!',
          text: 'El pago y su comprobante se guardaron correctamente.',
          icon: 'success',
          confirmButtonColor: '#7B1E21'
        });
      },
      error: (err) => {
        this.subiendoComprobante = false;
        console.error('Error al subir comprobante:', err);
        Swal.fire({
          title: 'Pago registrado',
          text: 'El abono se guardó, pero no se pudo subir el comprobante. Puedes reintentar sin registrar otro pago.',
          icon: 'warning',
          confirmButtonColor: '#7B1E21'
        });
      }
    });
  }
  async reintentarSubidaComprobante(): Promise<void> {
    if (this.pagoPendienteEvidencia === null || !this.archivoComprobante) {
      return;
    }
    const idPedido = this.pedido?.idPedido;
    if (!idPedido) {
      return;
    }
    this.subirComprobanteRegistrado(this.pagoPendienteEvidencia, idPedido);
  }
  verComprobante(pago: Pago): void {
    this.cerrarModalComprobante();
    this.cargandoComprobante = true;
    this.mostrarModalComprobante = true;
    this.pagoService.obtenerEvidencia(pago.idPago).subscribe({
      next: (archivo: Blob) => {
        if (!this.mostrarModalComprobante) return;
        this.urlComprobante = URL.createObjectURL(archivo);
        const esPdf = archivo.type === 'application/pdf' ||
          archivo.type === 'application/octet-stream' &&
          String(pago.evidencia ?? '').toLowerCase().endsWith('.pdf');
        this.tipoComprobante = esPdf ? 'pdf' : 'imagen';
        this.urlPdfSeguro = esPdf
          ? this.sanitizer.bypassSecurityTrustResourceUrl(this.urlComprobante)
          : null;
        this.cargandoComprobante = false;
      },
      error: (err) => {
        console.error('Error al consultar comprobante:', err);
        this.cerrarModalComprobante();
        Swal.fire({
          title: 'No se pudo abrir',
          text: 'No se pudo consultar el comprobante de este pago.',
          icon: 'error',
          confirmButtonColor: '#7B1E21'
        });
      }
    });
  }
  cerrarModalComprobante(): void {
    this.mostrarModalComprobante = false;
    this.cargandoComprobante = false;
    if (this.urlComprobante) URL.revokeObjectURL(this.urlComprobante);
    this.urlComprobante = null;
    this.urlPdfSeguro = null;
    this.tipoComprobante = null;
  }
}