
import { CommonModule } from '@angular/common';
import {
  Component, EventEmitter, Input, Output,
  OnChanges, inject
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  forkJoin, from, concatMap, toArray,
  Observable
} from 'rxjs';
import Swal from 'sweetalert2';

import {
  AsigTrabajador,
  EntregaAsignacion,
  FuncionTrabajador,
  EstadoAsignacion
} from '../../../core/interfaces/asig-trabajador.interface';

import { Trabajador } from '../../../core/interfaces/trabajador.interface';
import { Vehiculo } from '../../../core/interfaces/vehiculo.interface';
import { AsigTrabajadorService } from '../../../core/services/asig-trabajador.service';
import { EntregaService } from '../../../core/services/entrega.service';
import { TrabajadorService } from '../../../core/services/trabajador.service';
import { VehiculoService } from '../../../core/services/vehiculo.service';

type Pestana = FuncionTrabajador | 'ENTREGAS';

interface FilaPersonal {
  idAsig?: number;
  idTrabajador: number | null;
  funcion: FuncionTrabajador;
  fechaTrabajo: string;
  horaInicio: string;
  horaFin: string;
  cantidadAsignada: number | null;
  estado: EstadoAsignacion;
  original?: string;
}

interface FilaEntrega {
  idEntrega?: number;
  idTrabajador: number | null;
  idVehiculo: number | null;
  fechaEntrega: string;
  horaSalida: string;
  horaLlegada: string;
  cantidadPollos: number | null;
  observacion: string;
  estado: string;
  original?: string;
}

@Component({
  selector: 'app-asignacion-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './asignacion-form.component.html',
  styleUrl: './asignacion-form.component.css'
})
export class AsignacionFormComponent implements OnChanges {

  @Input() idPedido = 0;
  @Input() fechaEvento = '';
  @Input() totalPollos = 0;
  @Input() visible = false;

  @Output() cerrar = new EventEmitter<void>();
  @Output() guardado = new EventEmitter<void>();

  private asignacionService = inject(AsigTrabajadorService);
  private entregaService = inject(EntregaService);
  private trabajadorService = inject(TrabajadorService);
  private vehiculoService = inject(VehiculoService);

  trabajadores: Trabajador[] = [];
  vehiculos: Vehiculo[] = [];
  filas: FilaPersonal[] = [];
  entregas: FilaEntrega[] = [];

  pestana: Pestana = 'PREPARADOR';
  cargando = false;
  guardando = false;
  error = '';

  ngOnChanges(): void {
    if (!this.visible || !this.idPedido) return;

    this.cargando = true;
    this.error = '';
    this.pestana = 'PREPARADOR';
    this.filas = [];
    this.entregas = [];

    forkJoin({
      trabajadores: this.trabajadorService.getTrabajadores(),
      vehiculos: this.vehiculoService.getVehiculos(),
      asignaciones: this.asignacionService.getAsignacionesPorPedido(
        this.idPedido
      ),
      entregas: this.entregaService.getEntregas()
    }).subscribe({
      next: datos => {
        this.trabajadores = Array.isArray(datos.trabajadores)
          ? datos.trabajadores : [];

        this.vehiculos = Array.isArray(datos.vehiculos)
          ? datos.vehiculos : [];

        this.filas = (datos.asignaciones || [])
          .filter(a =>
            a.estado !== 'CANCELADO' && !!a.funcion
          )
          .map(a => {
            const fila: FilaPersonal = {
              idAsig: a.idAsig,
              idTrabajador: a.idTrabajador ?? null,
              funcion: a.funcion!,
              fechaTrabajo: (a.fechaTrabajo || '').slice(0, 10),
              horaInicio: (a.horaInicio || '').slice(0, 5),
              horaFin: (a.horaFin || '').slice(0, 5),
              cantidadAsignada: a.cantidadAsignada ?? null,
              estado: a.estado || 'PROGRAMADO'
            };

            fila.original = this.firmaPersonal(fila);
            return fila;
          });

        this.entregas = (datos.entregas || [])
          .filter(e =>
            Number(e.idPedido) === Number(this.idPedido) &&
            e.estado !== 'CANCELADO'
          )
          .map(e => {
            const fila: FilaEntrega = {
              idEntrega: e.idEntrega,
              idTrabajador: e.idTrabajador,
              idVehiculo: e.idVehiculo,
              fechaEntrega: (e.fechaEntrega || '').slice(0, 10),
              horaSalida: (e.horaSalida || '').slice(0, 5),
              horaLlegada: (e.horaLlegada || '').slice(0, 5),
              cantidadPollos: e.cantidadPollos,
              observacion: e.observacion || '',
              estado: e.estado || 'PROGRAMADO'
            };

            fila.original = this.firmaEntrega(fila);
            return fila;
          });

        this.cargando = false;
      },
      error: err => {
        console.error('Error al cargar:', err);
        this.error = this.mensajeError(err);
        this.cargando = false;
      }
    });
  }

  private firmaPersonal(f: FilaPersonal): string {
    return JSON.stringify([
      f.idTrabajador,
      f.funcion,
      f.fechaTrabajo,
      (f.horaInicio || '').slice(0, 5),
      (f.horaFin || '').slice(0, 5),
      f.cantidadAsignada,
      f.estado
    ]);
  }

  private firmaEntrega(e: FilaEntrega): string {
    return JSON.stringify([
      e.idTrabajador,
      e.idVehiculo,
      e.fechaEntrega,
      (e.horaSalida || '').slice(0, 5),
      (e.horaLlegada || '').slice(0, 5),
      e.cantidadPollos,
      e.observacion.trim(),
      e.estado
    ]);
  }

  private cambioPersonal(f: FilaPersonal): boolean {
    return !f.idAsig ||
      this.firmaPersonal(f) !== f.original;
  }

  private cambioEntrega(e: FilaEntrega): boolean {
    return !e.idEntrega ||
      this.firmaEntrega(e) !== e.original;
  }

  private mensajeError(err: any): string {
    return err?.error?.details ||
      err?.error?.detail ||
      err?.error?.message ||
      err?.message ||
      'No se pudo completar la operación.';
  }

  diaAnterior(fecha: string): string {
    const partes = fecha.slice(0, 10).split('-').map(Number);

    if (partes.length !== 3 || partes.some(n => !n)) {
      return '';
    }

    return new Date(
      Date.UTC(partes[0], partes[1] - 1, partes[2] - 1)
    ).toISOString().slice(0, 10);
  }

  agregar(funcion: FuncionTrabajador): void {
    this.error = '';

    this.filas.push({
      idTrabajador: null,
      funcion,
      fechaTrabajo: funcion === 'PREPARADOR'
        ? this.diaAnterior(this.fechaEvento)
        : this.fechaEvento.slice(0, 10),
      horaInicio: '',
      horaFin: '',
      cantidadAsignada: funcion === 'REPARTIDOR'
        ? null : 1,
      estado: 'PROGRAMADO'
    });
  }

  quitar(indice: number): void {
    if (this.filas[indice].idAsig) {
      this.error =
        'Para eliminar una asignación guardada, utiliza Eliminar en el listado.';
      return;
    }

    this.filas.splice(indice, 1);
  }

  agregarEntrega(): void {
    this.error = '';

    this.entregas.push({
      idTrabajador: this.repartidoresSeleccionados.length === 1
        ? this.repartidoresSeleccionados[0]
        : null,
      idVehiculo: null,
      fechaEntrega: this.fechaEvento.slice(0, 10),
      horaSalida: '',
      horaLlegada: '',
      cantidadPollos: null,
      observacion: '',
      estado: 'PROGRAMADO'
    });
  }

  quitarEntrega(indice: number): void {
    if (this.entregas[indice].idEntrega) {
      this.error =
        'La entrega ya está guardada. No se eliminará sin confirmación.';
      return;
    }

    this.entregas.splice(indice, 1);
  }

  nombre(t: Trabajador): string {
    const persona = t.persona as unknown as
      Record<string, unknown> | null;

    return [
      persona?.['nombres'] ?? persona?.['nombre'],
      persona?.['apellidos']
    ].filter(Boolean).join(' ') ||
      `Trabajador ${t.idTrabajador}`;
  }

  trabajadoresPorFuncion(
    funcion: FuncionTrabajador
  ): Trabajador[] {
    return this.trabajadores.filter(t =>
      funcion === 'PREPARADOR'
        ? Number(t.capacidadPreparacion) > 0
        : funcion === 'ASADOR'
          ? Number(t.capacidadAsado) > 0
          : t.puedeRepartir === true
    );
  }

  get repartidoresSeleccionados(): number[] {
    return [
      ...new Set(
        this.filas
          .filter(f =>
            f.funcion === 'REPARTIDOR' &&
            f.idTrabajador != null
          )
          .map(f => Number(f.idTrabajador))
      )
    ];
  }

  nombreRepartidor(id: number): string {
    const trabajador = this.trabajadores.find(t =>
      Number(t.idTrabajador) === Number(id)
    );

    return trabajador
      ? this.nombre(trabajador)
      : `Trabajador ${id}`;
  }

  totalFuncion(funcion: FuncionTrabajador): number {
    return this.filas
      .filter(f =>
        f.funcion === funcion &&
        f.idTrabajador != null
      )
      .reduce(
        (total, f) =>
          total + Number(f.cantidadAsignada || 0),
        0
      );
  }

  totalEntregas(): number {
    return this.entregas
      .filter(e =>
        e.idTrabajador != null &&
        e.idVehiculo != null
      )
      .reduce(
        (total, e) =>
          total + Number(e.cantidadPollos || 0),
        0
      );
  }

  get asignados(): number {
    return this.pestana === 'ENTREGAS'
      ? this.totalEntregas()
      : this.totalFuncion(this.pestana);
  }

  cerrarVentana(): void {
    if (!this.guardando) {
      this.cerrar.emit();
    }
  }

  guardar(): void {
    if (
      this.guardando ||
      this.cargando ||
      !this.idPedido
    ) return;

    this.error = '';

    const personal = this.filas.filter(
      f => f.idTrabajador != null &&
        this.cambioPersonal(f)
    );

    const entregas = this.entregas.filter(
      e => this.cambioEntrega(e) &&
        (e.idEntrega != null ||
          e.idTrabajador != null ||
          e.idVehiculo != null)
    );

    if (!personal.length && !entregas.length) {
      Swal.fire({
        icon: 'info',
        title: 'Sin cambios',
        text: 'No hay nuevas asignaciones ni entregas por guardar.',
        confirmButtonColor: '#7b1e21'
      });
      return;
    }

    // Validar solo trabajadores nuevos o modificados.
    for (const f of personal) {
      if (!f.fechaTrabajo || !f.horaInicio) {
        this.error =
          'Completa la fecha y hora de inicio del trabajador.';
        this.pestana = f.funcion;
        return;
      }

      const fechaCorrecta = f.funcion === 'PREPARADOR'
        ? this.diaAnterior(this.fechaEvento)
        : this.fechaEvento.slice(0, 10);

      if (f.fechaTrabajo !== fechaCorrecta) {
        this.error =
          `La fecha para ${f.funcion} debe ser ${fechaCorrecta}.`;
        this.pestana = f.funcion;
        return;
      }

      if (
        f.funcion !== 'REPARTIDOR' &&
        !(Number(f.cantidadAsignada) > 0)
      ) {
        this.error =
          'Los preparadores y asadores deben tener una cantidad mayor que cero.';
        this.pestana = f.funcion;
        return;
      }

      if (
        f.horaFin &&
        f.horaFin <= f.horaInicio
      ) {
        this.error =
          'La hora de fin debe ser posterior a la hora de inicio.';
        this.pestana = f.funcion;
        return;
      }
    }

    for (
      const funcion of [
        'PREPARADOR',
        'ASADOR'
      ] as FuncionTrabajador[]
    ) {
      if (this.totalFuncion(funcion) > this.totalPollos) {
        this.error =
          `${funcion} supera el total de ${this.totalPollos} pollos.`;
        this.pestana = funcion;
        return;
      }
    }

    if (this.totalEntregas() > this.totalPollos) {
      this.error =
        `Las entregas superan los ${this.totalPollos} pollos del pedido.`;
      this.pestana = 'ENTREGAS';
      return;
    }

    // Validar solamente entregas nuevas o modificadas.
    for (const e of entregas) {
      if (
        !e.idTrabajador ||
        !e.idVehiculo ||
        !e.fechaEntrega ||
        !(Number(e.cantidadPollos) > 0)
      ) {
        this.error =
          'Completa repartidor, vehículo, fecha y cantidad de la entrega.';
        this.pestana = 'ENTREGAS';
        return;
      }

      if (!e.horaSalida || !e.horaLlegada) {
        this.error =
          'Ingresa la hora de salida y la hora estimada de llegada.';
        this.pestana = 'ENTREGAS';
        return;
      }

      if (e.horaLlegada <= e.horaSalida) {
        this.error =
          'La hora de llegada debe ser posterior a la salida.';
        this.pestana = 'ENTREGAS';
        return;
      }

      if (
        e.fechaEntrega !== this.fechaEvento.slice(0, 10)
      ) {
        this.error =
          'La entrega debe realizarse el día del evento.';
        this.pestana = 'ENTREGAS';
        return;
      }

      if (
        !this.repartidoresSeleccionados.includes(
          Number(e.idTrabajador)
        )
      ) {
        this.error =
          'Primero asigna al repartidor en la pestaña Repartidores.';
        this.pestana = 'ENTREGAS';
        return;
      }

      const vehiculo = this.vehiculos.find(v =>
        Number(v.idVehiculo) === Number(e.idVehiculo)
      );

      if (
        vehiculo &&
        Number(e.cantidadPollos) > Number(vehiculo.capacidad)
      ) {
        this.error =
          `El vehículo ${vehiculo.placa} solo puede llevar ${vehiculo.capacidad} pollos por viaje.`;
        this.pestana = 'ENTREGAS';
        return;
      }
    }

    // Detectar cruces dentro del formulario.
    const activas = this.entregas.filter(e =>
      e.idTrabajador != null &&
      e.idVehiculo != null &&
      e.horaSalida &&
      e.horaLlegada &&
      e.estado !== 'CANCELADO'
    );

    for (let i = 0; i < activas.length; i++) {
      for (let j = i + 1; j < activas.length; j++) {
        const a = activas[i];
        const b = activas[j];

        if (a.fechaEntrega !== b.fechaEntrega) {
          continue;
        }

        const cruce =
          a.horaSalida < b.horaLlegada &&
          b.horaSalida < a.horaLlegada;

        if (!cruce) continue;

        if (a.idVehiculo === b.idVehiculo) {
          this.error =
            'El mismo vehículo tiene dos entregas con horarios cruzados.';
          this.pestana = 'ENTREGAS';
          return;
        }

        if (a.idTrabajador === b.idTrabajador) {
          this.error =
            'El mismo repartidor tiene dos entregas con horarios cruzados.';
          this.pestana = 'ENTREGAS';
          return;
        }
      }
    }

    const operaciones: Array<() => Observable<unknown>> = [];

    // Primero guardar trabajadores.
    for (const f of personal) {
      const dto: AsigTrabajador = {
        idPedido: this.idPedido,
        idTrabajador: Number(f.idTrabajador),
        funcion: f.funcion,
        fechaTrabajo: f.fechaTrabajo,
        horaInicio: f.horaInicio.slice(0, 5) + ':00',
        horaFin: f.horaFin
          ? f.horaFin.slice(0, 5) + ':00'
          : undefined,
        cantidadAsignada: f.funcion === 'REPARTIDOR'
          ? undefined
          : Number(f.cantidadAsignada),
        estado: f.estado
      };

      operaciones.push(() =>
        f.idAsig
          ? this.asignacionService.actualizarAsignacion(
              f.idAsig, dto
            )
          : this.asignacionService.crearTrabajador(dto)
      );
    }

    // Después guardar entregas y vehículos.
    for (const e of entregas) {
      const dto: EntregaAsignacion = {
        idPedido: this.idPedido,
        idTrabajador: Number(e.idTrabajador),
        idVehiculo: Number(e.idVehiculo),
        fechaEntrega: e.fechaEntrega,
        horaSalida: e.horaSalida.slice(0, 5),
        horaLlegada: e.horaLlegada.slice(0, 5),
        cantidadPollos: Number(e.cantidadPollos),
        observacion: e.observacion.trim(),
        estado: e.estado
      };

      operaciones.push(() =>
        e.idEntrega
          ? this.entregaService.actualizarEntrega(
              e.idEntrega, dto
            )
          : this.entregaService.crearEntrega(dto)
      );
    }

    this.guardando = true;

    from(operaciones).pipe(
      concatMap(operacion => operacion()),
      toArray()
    ).subscribe({
      next: () => {
        this.guardando = false;

        this.guardado.emit();

        Swal.fire({
          icon: 'success',
          title: 'Avance guardado',
          text: 'Puedes completar las demás asignaciones después.',
          confirmButtonColor: '#7b1e21'
        });
      },
      error: err => {
        this.guardando = false;

        console.error(
          'Error al guardar asignaciones o entregas:',
          err
        );

        this.error = this.mensajeError(err) +
          ' Si algunas operaciones se guardaron, cierra y vuelve a abrir el formulario antes de reintentar.';
      }
    });
  }
}
