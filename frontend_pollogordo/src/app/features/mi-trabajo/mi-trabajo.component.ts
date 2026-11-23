
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface MiTrabajo {
  idAsig: number;
  idPedido: number;
  funcion: string;
  fechaTrabajo: string;
  horaInicio: string | null;
  horaFin: string | null;
  cantidadAsignada: number | null;
  cantidadRealizada: number | null;
  estado: string;
}

interface GrupoTrabajo {
  idPedido: number;
  trabajos: MiTrabajo[];
}

@Component({
  selector: 'app-mi-trabajo',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mi-trabajo.component.html',
  styleUrl: './mi-trabajo.component.css'
})
export class MiTrabajoComponent implements OnInit {

  private http = inject(HttpClient);

  trabajos: MiTrabajo[] = [];

  cargando = false;
  error = '';

  fechaBusqueda = '';
  estadoBusqueda = 'TODOS';
  numeroPedido = '';

  ngOnInit(): void {
    this.cargar();
  }

  get nombreUsuario(): string {
    return localStorage.getItem('user_name') || 'Trabajador';
  }

  cargar(): void {

    if (this.cargando) return;

    this.cargando = true;
    this.error = '';

    this.http.get<MiTrabajo[]>(
      `${environment.apiUrl}/api/v1/mi-trabajo`
    ).subscribe({

      next: datos => {
        this.trabajos = Array.isArray(datos)
          ? datos.filter(t =>
              String(t.estado || '').toUpperCase() !== 'CANCELADO'
            )
          : [];

        this.cargando = false;
      },

      error: err => {
        console.error('Error al cargar mi trabajo:', err);
        this.error = 'No se pudieron cargar tus trabajos asignados.';
        this.cargando = false;
      }
    });
  }

  get totalPedidos(): number {
    return new Set(
      this.trabajos.map(t => t.idPedido)
    ).size;
  }

  get totalAsignaciones(): number {
    return this.trabajos.length;
  }

  get totalPendientes(): number {
    return this.trabajos.filter(t =>
      ['PROGRAMADO', 'EN_PROCESO'].includes(
        String(t.estado || '').toUpperCase()
      )
    ).length;
  }

  get trabajosFiltrados(): MiTrabajo[] {

    return this.trabajos.filter(t => {

      const coincideFecha =
        !this.fechaBusqueda ||
        this.soloFecha(t.fechaTrabajo) === this.fechaBusqueda;

      const coincideEstado =
        this.estadoBusqueda === 'TODOS' ||
        String(t.estado || '').toUpperCase() === this.estadoBusqueda;

      const coincidePedido =
        !this.numeroPedido.trim() ||
        String(t.idPedido).includes(this.numeroPedido.trim());

      return coincideFecha &&
        coincideEstado &&
        coincidePedido;
    });
  }

  get gruposFiltrados(): GrupoTrabajo[] {

    const mapa = new Map<number, GrupoTrabajo>();

    for (const trabajo of this.trabajosFiltrados) {

      const id = Number(trabajo.idPedido);

      if (!mapa.has(id)) {
        mapa.set(id, {
          idPedido: id,
          trabajos: []
        });
      }

      mapa.get(id)!.trabajos.push(trabajo);
    }

    const grupos = [...mapa.values()];

    for (const grupo of grupos) {
      grupo.trabajos.sort((a, b) =>
        this.soloFecha(a.fechaTrabajo).localeCompare(
          this.soloFecha(b.fechaTrabajo)
        ) ||
        String(a.horaInicio || '').localeCompare(
          String(b.horaInicio || '')
        )
      );
    }

    return grupos.sort((a, b) => {

      const fechaA = a.trabajos[0]?.fechaTrabajo || '';
      const fechaB = b.trabajos[0]?.fechaTrabajo || '';

      return fechaB.localeCompare(fechaA);
    });
  }

  limpiarFiltros(): void {
    this.fechaBusqueda = '';
    this.estadoBusqueda = 'TODOS';
    this.numeroPedido = '';
  }

  soloFecha(fecha: string | null | undefined): string {
    return String(fecha || '').slice(0, 10);
  }

  mostrarFecha(fecha: string | null | undefined): string {

    const valor = this.soloFecha(fecha);

    if (!valor) return '—';

    const partes = valor.split('-');

    if (partes.length !== 3) return valor;

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
  }

  mostrarHora(hora: string | null | undefined): string {
    return hora ? String(hora).slice(0, 5) : '—';
  }

  mostrarCantidad(trabajo: MiTrabajo): string {

    if (trabajo.cantidadAsignada == null) {
      return 'Pendiente';
    }

    return `${trabajo.cantidadAsignada} pollos`;
  }

  nombreFuncion(funcion: string): string {

    switch (String(funcion || '').toUpperCase()) {
      case 'PREPARADOR':
        return 'Preparación';

      case 'ASADOR':
        return 'Asado';

      case 'REPARTIDOR':
        return 'Reparto';

      default:
        return funcion || 'Sin función';
    }
  }

  claseEstado(estado: string): string {

    switch (String(estado || '').toUpperCase()) {
      case 'COMPLETADO':
        return 'completado';

      case 'EN_PROCESO':
        return 'en-proceso';

      case 'CANCELADO':
        return 'cancelado';

      default:
        return 'programado';
    }
  }

  estadoGrupo(grupo: GrupoTrabajo): string {

    if (grupo.trabajos.every(t =>
      String(t.estado).toUpperCase() === 'COMPLETADO'
    )) {
      return 'COMPLETADO';
    }

    if (grupo.trabajos.some(t =>
      String(t.estado).toUpperCase() === 'EN_PROCESO'
    )) {
      return 'EN_PROCESO';
    }

    return 'PROGRAMADO';
  }
}
