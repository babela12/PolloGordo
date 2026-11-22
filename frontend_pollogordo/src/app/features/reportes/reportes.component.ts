
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ReporteService } from '../../core/services/reporte.service';
import { ReporteEventos } from '../../core/interfaces/reporte.interface';

import { InsumoService } from '../../core/services/insumo.service';
import { Insumo } from '../../core/interfaces/insumo.interface';

interface CompraInsumo {
  insumo: string;
  unidad: string;
  cantidadNecesaria: number;
  stockDisponible: number | null;
  cantidadComprar: number | null;
  pedidos: number;
  controlaStock: boolean;
  encontrado: boolean;
}

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.component.html',
  styleUrl: './reportes.component.css'
})
export class ReportesComponent implements OnInit {

  private reporteService = inject(ReporteService);
  private insumoService = inject(InsumoService);

  fechaInicio = '';
  fechaFin = '';
  soloConfirmados = false;

  tipo: 'pedidos' | 'insumos' = 'pedidos';

  reporte: ReporteEventos | null = null;
  insumosInventario: Insumo[] = [];

  cargando = false;
  error = '';
  errorInventario = '';
  inventarioDisponible = false;

  ngOnInit(): void {
    this.seleccionarSemana();
    this.consultar();
  }

  seleccionarSemana(): void {
    const inicio = new Date();
    inicio.setHours(12, 0, 0, 0);

    inicio.setDate(
      inicio.getDate() - ((inicio.getDay() + 6) % 7)
    );

    const fin = new Date(inicio);
    fin.setDate(inicio.getDate() + 6);

    this.fechaInicio = this.fechaLocal(inicio);
    this.fechaFin = this.fechaLocal(fin);

    this.invalidar();
  }

  private fechaLocal(fecha: Date): string {
    return (
      `${fecha.getFullYear()}-` +
      `${String(fecha.getMonth() + 1).padStart(2, '0')}-` +
      `${String(fecha.getDate()).padStart(2, '0')}`
    );
  }

  invalidar(): void {
    this.reporte = null;
    this.insumosInventario = [];
    this.inventarioDisponible = false;
    this.error = '';
    this.errorInventario = '';
  }

  consultar(): void {

    if (this.cargando) {
      return;
    }

    this.invalidar();

    if (
      !this.fechaInicio ||
      !this.fechaFin ||
      this.fechaInicio > this.fechaFin
    ) {
      this.error =
        'Elige un rango válido: la fecha inicial debe ser menor o igual a la final.';
      return;
    }

    this.cargando = true;

    forkJoin({
      reporte: this.reporteService.getReporteEventos(
        this.fechaInicio,
        this.fechaFin,
        this.soloConfirmados
      ),

      inventario: this.insumoService.getInsumos().pipe(
        catchError(err => {
          console.error('Error al consultar inventario:', err);

          this.errorInventario =
            'No se pudo consultar el inventario. No es posible calcular las cantidades por comprar.';

          return of(null);
        })
      )
    }).subscribe({

      next: ({ reporte, inventario }) => {

        this.reporte = reporte;

        if (inventario !== null) {
          this.insumosInventario = inventario;
          this.inventarioDisponible = true;
        }

        this.cargando = false;
      },

      error: err => {

        console.error('Error al consultar reporte:', err);

        this.cargando = false;

        this.error = err.status === 401
          ? 'La sesión venció. Vuelve a iniciar sesión.'
          : (
            err.error?.message ||
            'No se pudo consultar el reporte. Verifica la conexión e intenta de nuevo.'
          );
      }
    });
  }

  // ESTADÍSTICAS DE PEDIDOS

  get totalPedidos(): number {
    return this.reporte?.pedidos.length ?? 0;
  }

  get totalImporte(): number {
    return this.reporte?.pedidos.reduce(
      (total, pedido) => total + Number(pedido.total ?? 0),
      0
    ) ?? 0;
  }

  get confirmados(): number {
    return this.reporte?.pedidos.filter(
      pedido => pedido.confirmado === 'S'
    ).length ?? 0;
  }

  get hayFilas(): boolean {
    return this.tipo === 'pedidos'
      ? (this.reporte?.pedidos.length ?? 0) > 0
      : (this.reporte?.insumos.length ?? 0) > 0;
  }

  // NORMALIZAR NOMBRES DE INSUMOS

  private normalizarNombre(nombre: string): string {
    return (nombre ?? '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLowerCase();
  }

  // PLANIFICACIÓN DE COMPRAS

  get planificacionCompras(): CompraInsumo[] {

    if (!this.reporte) {
      return [];
    }

    return this.reporte.insumos.map(item => {

      const insumo = this.insumosInventario.find(
        i => this.normalizarNombre(i.nombre) ===
          this.normalizarNombre(item.insumo)
      );

      const encontrado =
        this.inventarioDisponible && insumo !== undefined;

      const controlaStock =
        encontrado &&
        String(insumo!.controlaStock ?? '')
          .trim()
          .toUpperCase() === 'S';

      const cantidadNecesaria = Number(item.cantidad ?? 0);

      const stockDisponible =
        controlaStock && insumo!.cantidadStock != null
          ? Number(insumo!.cantidadStock)
          : null;

      const cantidadComprar =
        stockDisponible !== null
          ? Math.max(0, cantidadNecesaria - stockDisponible)
          : encontrado && !controlaStock
            ? cantidadNecesaria
            : null;
            
      return {
        insumo: item.insumo,
        unidad: item.unidad,
        cantidadNecesaria,
        stockDisponible,
        cantidadComprar,
        pedidos: item.pedidos,
        controlaStock,
        encontrado
      };
    });
  }

  // CANTIDAD DE INSUMOS QUE REQUIEREN COMPRA

  get insumosPorComprar(): number {
    return this.planificacionCompras.filter(
      item => item.cantidadComprar !== null &&
        item.cantidadComprar > 0
    ).length;
  }

  // CANTIDAD DE INSUMOS SIN CONTROL DE STOCK

  get insumosSinControl(): number {
    return this.planificacionCompras.filter(
      item => item.encontrado && !item.controlaStock
    ).length;
  }

  // IMPRESIÓN

  imprimir(): void {
    if (this.reporte && this.hayFilas) {
      window.print();
    }
  }

  // EXPORTACIÓN CSV

  exportarCsv(): void {

    const reporte = this.reporte;

    if (!reporte || !this.hayFilas) {
      return;
    }

    let filas: (string | number | null)[][];

    if (this.tipo === 'pedidos') {

      filas = [
        [
          'Pedido',
          'Cliente',
          'Fecha del evento',
          'Hora de entrega',
          'Dirección',
          'Estado',
          'Confirmado',
          'Total (S/)'
        ],

        ...reporte.pedidos.map(pedido => [
          pedido.idPedido,
          pedido.cliente,
          pedido.fechaEvento,
          pedido.horaEntrega?.slice(0, 5) ?? '',
          pedido.direccionEvento,
          pedido.estado,
          pedido.confirmado === 'S' ? 'Sí' : 'No',
          Number(pedido.total).toFixed(2)
        ]),

        [
          'TOTAL',
          '',
          '',
          '',
          '',
          '',
          '',
          this.totalImporte.toFixed(2)
        ]
      ];

    } else {

      filas = [
        [
          'Insumo',
          'Unidad',
          'Cantidad necesaria',
          'Stock disponible',
          'Cantidad por comprar',
          'Pedidos relacionados'
        ],

        ...this.planificacionCompras.map(item => [
          item.insumo,
          item.unidad,
          item.cantidadNecesaria,

          item.stockDisponible !== null
            ? item.stockDisponible
            : (
              !this.inventarioDisponible
                ? 'Inventario no disponible'
                : !item.encontrado
                  ? 'Insumo no encontrado'
                  : item.controlaStock
                    ? 'Stock no registrado'
                    : 'No controla stock'
            ),

          item.cantidadComprar !== null
            ? item.cantidadComprar
            : 'Por definir',

          item.pedidos
        ])
      ];
    }

    const csv = filas
      .map(
        fila => fila.map(
          valor => this.celdaCsv(valor)
        ).join(';')
      )
      .join('\r\n');

    const archivo = new Blob(
      ['\uFEFF', csv],
      { type: 'text/csv;charset=utf-8;' }
    );

    const url = URL.createObjectURL(archivo);
    const enlace = document.createElement('a');

    enlace.href = url;

    const filtro = reporte.soloConfirmados
      ? 'confirmados'
      : 'todos';

    enlace.download =
      `${this.tipo}_${reporte.fechaInicio}_${reporte.fechaFin}_${filtro}.csv`;

    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();

    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  private celdaCsv(valor: string | number | null): string {

    let texto = String(valor ?? '');

    if (/^[\s]*[=+@-]/.test(texto)) {
      texto = "'" + texto;
    }

    return '"' + texto.replace(/"/g, '""') + '"';
  }
}
