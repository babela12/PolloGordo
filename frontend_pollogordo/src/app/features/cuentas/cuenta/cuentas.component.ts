
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { forkJoin } from 'rxjs';

import {
  Cuenta,
  CuentaService,
  NuevaCuenta
} from '../../../core/services/cuenta.service';

interface Trabajador {
  idTrabajador: number;
  persona?: {
    idPersona: number;
    nombre: string;
    apellidos: string;
  };
}

@Component({
  selector: 'app-cuentas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cuentas.component.html',
  styleUrl: './cuentas.component.css'
})
export class CuentasComponent implements OnInit {

  private cuentaService = inject(CuentaService);
  private http = inject(HttpClient);

  cuentas: Cuenta[] = [];
  trabajadores: Trabajador[] = [];

  cargando = false;
  cargandoTrabajadores = false;
  guardando = false;

  error = '';
  errorTrabajadores = '';
  mensaje = '';

  nuevaCuenta: NuevaCuenta = {
    usuario: '',
    password: '',
    rol: 'TRABAJADOR',
    idPersona: 0
  };

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {

    this.cargando = true;
    this.cargandoTrabajadores = true;

    this.error = '';
    this.errorTrabajadores = '';

    forkJoin({
      cuentas: this.cuentaService.listar(),
      trabajadores: this.http.get<Trabajador[]>(
        '/pollogordo_api/trabajadores'
      )
    }).subscribe({

      next: datos => {

        this.cuentas = Array.isArray(datos.cuentas)
          ? datos.cuentas
          : [];

        this.trabajadores = Array.isArray(datos.trabajadores)
          ? datos.trabajadores
          : [];

        const idsDisponibles = new Set(
          this.trabajadoresSinCuenta.map(
            t => Number(t.persona?.idPersona)
          )
        );

        if (!idsDisponibles.has(Number(this.nuevaCuenta.idPersona))) {
          this.nuevaCuenta.idPersona = 0;
        }

        this.cargando = false;
        this.cargandoTrabajadores = false;
      },

      error: err => {

        console.error('Error al cargar cuentas:', err);

        this.error =
          'No se pudieron cargar las cuentas y los trabajadores.';

        this.cargando = false;
        this.cargandoTrabajadores = false;
      }
    });
  }

  listarCuentas(): void {
    this.cargarDatos();
  }

  cargarTrabajadores(): void {
    this.cargarDatos();
  }

  get trabajadoresSinCuenta(): Trabajador[] {

    const personasConCuenta = new Set(
      this.cuentas
        .filter(c => c.idPersona != null)
        .map(c => Number(c.idPersona))
    );

    return this.trabajadores.filter(t => {

      const idPersona = t.persona?.idPersona;

      if (idPersona == null) {
        return false;
      }

      return !personasConCuenta.has(Number(idPersona));
    });
  }

  crearCuenta(): void {

    this.error = '';
    this.mensaje = '';

    const usuario = this.nuevaCuenta.usuario.trim();

    if (!usuario) {
      this.error = 'Ingresa un nombre de usuario.';
      return;
    }

    if (this.nuevaCuenta.password.length < 8) {
      this.error =
        'La contraseña debe tener mínimo 8 caracteres.';
      return;
    }

    if (!this.nuevaCuenta.idPersona) {
      this.error = 'Selecciona un trabajador.';
      return;
    }

    const disponible = this.trabajadoresSinCuenta.some(
      t => Number(t.persona?.idPersona) ===
        Number(this.nuevaCuenta.idPersona)
    );

    if (!disponible) {
      this.error =
        'Este trabajador ya tiene una cuenta o no está disponible.';
      return;
    }

    this.guardando = true;

    const datos: NuevaCuenta = {
      usuario,
      password: this.nuevaCuenta.password,
      rol: this.nuevaCuenta.rol,
      idPersona: Number(this.nuevaCuenta.idPersona)
    };

    this.cuentaService.crear(datos).subscribe({

      next: () => {

        this.guardando = false;

        this.mensaje = 'Cuenta creada correctamente.';

        this.nuevaCuenta = {
          usuario: '',
          password: '',
          rol: 'TRABAJADOR',
          idPersona: 0
        };

        this.cargarDatos();
      },

      error: err => {

        console.error('Error al crear cuenta:', err);

        this.guardando = false;

        this.error =
          err?.error?.message ||
          'No se pudo crear la cuenta.';
      }
    });
  }
}
