
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import Swal from 'sweetalert2';

import {
  AsigTrabajador,
  EntregaAsignacion
} from '../../../core/interfaces/asig-trabajador.interface';

import { Trabajador } from '../../../core/interfaces/trabajador.interface';
import { AsigTrabajadorService } from '../../../core/services/asig-trabajador.service';
import { TrabajadorService } from '../../../core/services/trabajador.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { EntregaService } from '../../../core/services/entrega.service';
import { AuthService } from '../../../core/services/auth.service';
import { AsignacionFormComponent } from '../asignacion-form/asignacion-form.component';

interface ProductoAsignacion {
  cantidadPollos?: number | string | null;
}

interface PedidoAsignacion {
  idPedido: number;
  confirmado: string;
  fechaEvento: string;
  cliente: string;
  productos?: ProductoAsignacion[];
}

interface GrupoPedido {
  idPedido: number;
  cliente: string;
  fechaEvento: string;
  asignaciones: AsigTrabajador[];
}

interface GrupoTrabajador {
  idTrabajador: number;
  nombre: string;
  asignaciones: AsigTrabajador[];
}

@Component({
  selector: 'app-asignacion-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    AsignacionFormComponent
  ],
  templateUrl: './asignacion-list.component.html',
  styleUrl: './asignacion-list.component.css'
})
export class AsignacionListComponent implements OnInit {

  private asignacionService = inject(AsigTrabajadorService);
  private trabajadorService = inject(TrabajadorService);
  private pedidoService = inject(PedidoService);
  private entregaService = inject(EntregaService);
  private authService = inject(AuthService);

  asignaciones: AsigTrabajador[] = [];
  entregas: EntregaAsignacion[] = [];
  trabajadores: Trabajador[] = [];
  pedidos: PedidoAsignacion[] = [];

  vista: 'pedido' | 'trabajador' = 'pedido';

  idPedido = 0;
  pedidoActual: PedidoAsignacion | null = null;
  totalPollos = 0;

  mostrarFormulario = false;
  cargando = false;
  buscandoPedido = false;
  error = '';

  get esGerente(): boolean {
    return this.authService.esGerente();
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {

    this.cargando = true;
    this.error = '';

    forkJoin({
      asignaciones: this.asignacionService.getAsignaciones(),
      entregas: this.entregaService.getEntregas(),
      trabajadores: this.trabajadorService.getTrabajadores(),
      pedidos: this.pedidoService.getPedidos()
    }).subscribe({

      next: datos => {

        this.asignaciones = Array.isArray(datos.asignaciones)
          ? datos.asignaciones
          : [];

        this.entregas = Array.isArray(datos.entregas)
          ? datos.entregas
          : [];

        this.trabajadores = Array.isArray(datos.trabajadores)
          ? datos.trabajadores
          : [];

        const lista: PedidoAsignacion[] =
          Array.isArray(datos.pedidos) ? datos.pedidos : [];

        this.pedidos = lista.filter(p =>
          String(p.confirmado ?? '').trim().toUpperCase() === 'S'
        );

        if (
          this.idPedido &&
          !this.pedidosSinAsignar.some(p =>
            Number(p.idPedido) === Number(this.idPedido)
          )
        ) {
          this.idPedido = 0;
        }

        this.cargando = false;
      },

      error: err => {
        console.error(err);
        this.error =
          'No se pudieron cargar las asignaciones.';
        this.cargando = false;
      }
    });
  }

  get pedidosSinAsignar(): PedidoAsignacion[] {
    return this.pedidos.filter(p =>
      !this.tieneAsignaciones(p.idPedido)
    );
  }

  seleccionarPedido(valor: number | string): void {
    this.idPedido = Number(valor);
    this.error = '';
  }

  tieneAsignaciones(idPedido: number): boolean {
    return this.asignaciones.some(a =>
      Number(a.idPedido) === Number(idPedido) &&
      String(a.estado ?? '').trim().toUpperCase() !== 'CANCELADO'
    );
  }

  get gruposAsignaciones(): GrupoPedido[] {

    const mapa = new Map<number, GrupoPedido>();

    for (const a of this.asignaciones) {

      if (
        String(a.estado ?? '').toUpperCase() === 'CANCELADO'
      ) continue;

      const id = Number(a.idPedido);

      if (!Number.isFinite(id)) continue;

      if (!mapa.has(id)) {

        const p = this.pedidos.find(x =>
          Number(x.idPedido) === id
        );

        mapa.set(id, {
          idPedido: id,
          cliente: p?.cliente || '',
          fechaEvento: p?.fechaEvento || '',
          asignaciones: []
        });
      }

      mapa.get(id)!.asignaciones.push(a);
    }

    return [...mapa.values()]
      .sort((a, b) => b.idPedido - a.idPedido);
  }

  get gruposTrabajadores(): GrupoTrabajador[] {

    const mapa = new Map<number, GrupoTrabajador>();

    for (const a of this.asignaciones) {

      if (
        String(a.estado ?? '').toUpperCase() === 'CANCELADO'
      ) continue;

      const id = Number(a.idTrabajador);

      if (!Number.isFinite(id)) continue;

      if (!mapa.has(id)) {
        mapa.set(id, {
          idTrabajador: id,
          nombre: this.nombreTrabajador(id),
          asignaciones: []
        });
      }

      mapa.get(id)!.asignaciones.push(a);
    }

    return [...mapa.values()]
      .sort((a, b) => a.nombre.localeCompare(b.nombre));
  }

  nombreCliente(idPedido: number): string {
    return this.pedidos.find(p =>
      Number(p.idPedido) === Number(idPedido)
    )?.cliente || '';
  }

  cantidadRepartidor(
    asignacion: AsigTrabajador
  ): number | null {

    if (asignacion.funcion !== 'REPARTIDOR') {
      return asignacion.cantidadAsignada ?? null;
    }

    const total = this.entregas
      .filter(e =>
        Number(e.idPedido) === Number(asignacion.idPedido) &&
        Number(e.idTrabajador) === Number(asignacion.idTrabajador) &&
        String(e.estado ?? '').trim().toUpperCase() !== 'CANCELADO'
      )
      .reduce(
        (suma, e) => suma + Number(e.cantidadPollos || 0),
        0
      );

    return total > 0 ? total : null;
  }

  abrirFormulario(): void {

    if (!this.esGerente) return;

    const id = Number(this.idPedido);

    if (!id) {
      Swal.fire(
        'Atención',
        'Selecciona un pedido.',
        'warning'
      );
      return;
    }

    if (this.tieneAsignaciones(id)) {
      this.idPedido = 0;

      Swal.fire(
        'Pedido con asignaciones',
        'Usa Editar / Agregar en la tarjeta del pedido.',
        'info'
      );
      return;
    }

    this.abrirPedido(id);
  }

  editarPedido(idPedido: number): void {
    if (!this.esGerente) return;
    this.abrirPedido(idPedido);
  }

  private abrirPedido(id: number): void {

    if (!this.esGerente) return;

    if (this.buscandoPedido || this.mostrarFormulario) {
      return;
    }

    this.buscandoPedido = true;
    this.error = '';

    this.pedidoService.getPedidoById(id).subscribe({

      next: (pedido: PedidoAsignacion) => {

        this.buscandoPedido = false;

        if (
          String(pedido.confirmado ?? '').trim().toUpperCase() !== 'S'
        ) {
          Swal.fire(
            'Atención',
            'El pedido no está confirmado.',
            'warning'
          );
          return;
        }

        const productos = Array.isArray(pedido.productos)
          ? pedido.productos
          : [];

        const total = productos.reduce(
          (suma, p) => suma + Number(p.cantidadPollos || 0),
          0
        );

        if (total <= 0) {
          Swal.fire(
            'Atención',
            'No se pudo obtener la cantidad de pollos.',
            'warning'
          );
          return;
        }

        this.totalPollos = total;
        this.pedidoActual = pedido;
        this.mostrarFormulario = true;
      },

      error: err => {
        this.buscandoPedido = false;
        console.error(err);

        Swal.fire(
          'Error',
          'No se pudo consultar el pedido.',
          'error'
        );
      }
    });
  }

  asignacionGuardada(): void {
    this.cerrarFormulario();
    this.idPedido = 0;
    this.cargar();
  }

  cerrarFormulario(): void {
    this.mostrarFormulario = false;
    this.pedidoActual = null;
  }

  async eliminar(asignacion: AsigTrabajador): Promise<void> {

    if (!this.esGerente || !asignacion.idAsig) return;

    const respuesta = await Swal.fire({
      title: '¿Eliminar asignación?',
      text: 'Se eliminará la asignación del trabajador.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#7b1e21'
    });

    if (!respuesta.isConfirmed) return;

    this.asignacionService
      .eliminarAsignacion(asignacion.idAsig)
      .subscribe({

        next: () => this.cargar(),

        error: err => {
          console.error(err);
          Swal.fire(
            'Error',
            'No se pudo eliminar la asignación.',
            'error'
          );
        }
      });
  }

  nombreTrabajador(id?: number): string {

    const t = this.trabajadores.find(x =>
      Number(x.idTrabajador) === Number(id)
    );

    if (!t) return `Trabajador ${id ?? 'sin ID'}`;

    const p = t.persona as unknown as
      Record<string, unknown> | null;

    if (!p) return `Trabajador ${id}`;

    const nombres = String(
      p['nombres'] ?? p['nombre'] ?? ''
    ).trim();

    const apellidos = String(
      p['apellidos'] ?? ''
    ).trim() || [
      p['apellidoPaterno'],
      p['apellidoMaterno']
    ].filter(Boolean).join(' ').trim();

    return [nombres, apellidos]
      .filter(Boolean)
      .join(' ') || `Trabajador ${id}`;
  }
}
