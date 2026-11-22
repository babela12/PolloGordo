
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import {
  HomeService,
  PedidoHome
} from '../../core/services/home.service';

import { InsumoService } from '../../core/services/insumo.service';
import { Insumo } from '../../core/interfaces/insumo.interface';

import {
  PedidoTemporalService
} from '../../core/services/pedido-temporal.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {

  // SERVICIOS
  private homeService = inject(HomeService);
  private insumoService = inject(InsumoService);
  private pedidoTemporalService = inject(PedidoTemporalService);

  // ESTADÍSTICAS DE PEDIDOS
  pedidosSemana = 0;
  importeSemana = 0;
  confirmadosSemana = 0;
  porConfirmarSemana = 0;

  // ESTADÍSTICAS DE INVENTARIO
  insumosAgotados: number | null = null;

  // PRÓXIMOS EVENTOS
  eventosProximos: PedidoHome[] = [];

  // ESTADOS DE CARGA
  cargando = false;
  cargandoInsumos = false;

  // MENSAJES DE ERROR
  error = '';
  errorInsumos = '';

  ngOnInit(): void {
    this.actualizar();
  }

  // ACTUALIZAR INFORMACIÓN DEL HOME
  actualizar(): void {

    if (this.cargando || this.cargandoInsumos) {
      return;
    }

    this.cargando = true;
    this.cargandoInsumos = true;

    this.error = '';
    this.errorInsumos = '';

    this.insumosAgotados = null;

    // PEDIDOS TEMPORALES POR CONFIRMAR
    this.cargarPedidosPorConfirmar();

    // CONSULTAR PEDIDOS DE ORACLE
    this.homeService.listarPedidos().subscribe({

      next: (pedidos: PedidoHome[]) => {

        this.calcularResumen(pedidos);

        this.cargando = false;
      },

      error: (err) => {

        console.error(
          'Error al cargar pedidos:',
          err
        );

        this.error =
          'No se pudo cargar la información de pedidos.';

        this.cargando = false;
      }

    });

    // CONSULTAR INSUMOS DE ORACLE
    this.insumoService.getInsumos().subscribe({

      next: (insumos: Insumo[]) => {

        this.calcularInsumosAgotados(insumos);

        this.cargandoInsumos = false;
      },

      error: (err) => {

        console.error(
          'Error al cargar insumos:',
          err
        );

        this.insumosAgotados = null;

        this.errorInsumos =
          'No se pudo consultar el inventario.';

        this.cargandoInsumos = false;
      }

    });
  }

  // PEDIDOS POR CONFIRMAR
  // MISMA FUENTE QUE GESTIÓN DE PEDIDOS
  private cargarPedidosPorConfirmar(): void {

    const pedidosPendientes =
      this.pedidoTemporalService.getPedidos();

    this.porConfirmarSemana = pedidosPendientes.length;
  }

  // CALCULAR RESUMEN SEMANAL
  private calcularResumen(pedidos: PedidoHome[]): void {

    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);

    // INICIO DE SEMANA: LUNES
    const inicioSemana = new Date(hoy);

    const diaSemana = (hoy.getDay() + 6) % 7;

    inicioSemana.setDate(
      hoy.getDate() - diaSemana
    );

    // FIN DE SEMANA: DOMINGO
    const finSemana = new Date(inicioSemana);

    finSemana.setDate(
      inicioSemana.getDate() + 6
    );

    finSemana.setHours(23, 59, 59, 999);

    // EXCLUIR PEDIDOS CANCELADOS
    const pedidosValidos = pedidos.filter(
      pedido => !this.estaCancelado(pedido)
    );

    // PEDIDOS DEL PERÍODO
    const pedidosDelPeriodo = pedidosValidos.filter(
      pedido => {

        if (!pedido.fechaEvento) {
          return false;
        }

        const fecha =
          this.convertirFecha(pedido.fechaEvento);

        return fecha >= inicioSemana &&
               fecha <= finSemana;
      }
    );

    // TOTAL DE PEDIDOS DE LA SEMANA
    this.pedidosSemana = pedidosDelPeriodo.length;

    // IMPORTE CONTRATADO
    this.importeSemana = pedidosDelPeriodo.reduce(
      (total, pedido) =>
        total + Number(pedido.total ?? 0),
      0
    );

    // CONFIRMADOS DE LA SEMANA
    this.confirmadosSemana = pedidosDelPeriodo.filter(
      pedido => this.estaConfirmado(pedido)
    ).length;

    // IMPORTANTE:
    // NO CALCULAMOS AQUÍ LOS PENDIENTES.
    // SE OBTIENEN DESDE PedidoTemporalService.

    // PRÓXIMOS EVENTOS PROGRAMADOS
    this.eventosProximos = pedidosValidos
      .filter(pedido => {

        if (!pedido.fechaEvento) {
          return false;
        }

        const fecha =
          this.convertirFecha(pedido.fechaEvento);

        return fecha >= hoy;
      })
      .sort((a, b) => {

        const fechaA =
          `${a.fechaEvento}T${a.horaEntrega || '00:00'}`;

        const fechaB =
          `${b.fechaEvento}T${b.horaEntrega || '00:00'}`;

        return fechaA.localeCompare(fechaB);
      });
  }

  // VERIFICAR PEDIDO CANCELADO
  private estaCancelado(pedido: PedidoHome): boolean {

    return String(pedido.estado ?? '')
      .trim()
      .toUpperCase() === 'CANCELADO';
  }

  // VERIFICAR PEDIDO CONFIRMADO
  private estaConfirmado(pedido: PedidoHome): boolean {

    return String(pedido.confirmado ?? '')
      .trim()
      .toUpperCase() === 'S';
  }

  // CALCULAR INSUMOS AGOTADOS
  private calcularInsumosAgotados(insumos: Insumo[]): void {

    this.insumosAgotados = insumos.filter(insumo => {

      const activo =
        String(insumo.estado ?? '')
          .trim()
          .toUpperCase() === 'A';

      const controlaStock =
        String(insumo.controlaStock ?? '')
          .trim()
          .toUpperCase() === 'S';

      const agotado =
        insumo.cantidadStock !== null &&
        insumo.cantidadStock !== undefined &&
        Number(insumo.cantidadStock) <= 0;

      return activo && controlaStock && agotado;

    }).length;
  }

  // CONVERTIR FECHA SIN DESFASE HORARIO
  private convertirFecha(fecha: string): Date {

    const [anio, mes, dia] =
      fecha.split('-').map(Number);

    return new Date(anio, mes - 1, dia);
  }
}
