import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { TrabajadorService } from '../../../core/services/trabajador.service';
import { CargoService } from '../../../core/services/cargo.service';
import { AlertService } from '../../../core/services/alert.service';
import { Trabajador } from '../../../core/interfaces/trabajador.interface';
import { Cargo } from '../../../core/interfaces/cargo.interface';

@Component({
  selector: 'app-trabajador-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './trabajador-list.component.html',
  styleUrl: './trabajador-list.component.css'
})
export class TrabajadorListComponent implements OnInit {

  private trabajadorService = inject(TrabajadorService);
  private cargoService = inject(CargoService);
  private alertService = inject(AlertService);

  trabajadores: Trabajador[] = [];
  cargos: Cargo[] = [];

  busqueda = '';
  cargando = false;
  procesandoId: number | null = null;

  ngOnInit(): void {
    this.listarTrabajadores();
    this.listarCargos();
  }

  listarTrabajadores(): void {
    this.cargando = true;

    this.trabajadorService.getTrabajadores().subscribe({
      next: (data) => {
        this.trabajadores = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error(err);
        this.cargando = false;
        this.alertService.error('No se pudieron cargar los trabajadores');
      }
    });
  }

  listarCargos(): void {
    this.cargoService.getCargos().subscribe({
      next: (data) => this.cargos = data,
      error: (err) => {
        console.error(err);
        this.alertService.error('No se pudieron cargar los cargos');
      }
    });
  }

  getNombreCargo(idCargo: number): string {
    return this.cargos.find(c => c.idCargo === idCargo)?.nombre
      ?? 'Cargo no encontrado';
  }

  get trabajadoresFiltrados(): Trabajador[] {
    const texto = this.busqueda.toLowerCase().trim();

    return this.trabajadores.filter(t => {
      const nombre = `${t.persona.nombre} ${t.persona.apellidos}`.toLowerCase();

      return nombre.includes(texto)
        || t.persona.dni.includes(texto)
        || t.persona.numTelef.includes(texto)
        || this.getNombreCargo(t.idCargo).toLowerCase().includes(texto);
    });
  }

  async cambiarEstado(trabajador: Trabajador): Promise<void> {
    if (trabajador.idTrabajador == null || this.procesandoId !== null) {
      return;
    }

    const estaActivo = trabajador.estado === 'A';
    const nuevoEstado: 'A' | 'I' = estaActivo ? 'I' : 'A';
    const accion = estaActivo ? 'desactivar' : 'activar';

    const confirmado = await this.alertService.confirm(
      `¿Deseas ${accion} a ${trabajador.persona.nombre} ${trabajador.persona.apellidos}?`,
      estaActivo ? '¿Desactivar trabajador?' : '¿Activar trabajador?'
    );

    if (!confirmado) return;

    this.procesandoId = trabajador.idTrabajador;

    this.trabajadorService
      .cambiarEstado(trabajador.idTrabajador, nuevoEstado)
      .subscribe({
        next: () => {
          this.procesandoId = null;

          this.alertService.toast(
            estaActivo
              ? 'Trabajador desactivado correctamente'
              : 'Trabajador activado correctamente'
          );

          this.listarTrabajadores();
        },
        error: (err) => {
          this.procesandoId = null;
          console.error(err);
          this.alertService.error('No se pudo cambiar el estado del trabajador');
        }
      });
  }
}