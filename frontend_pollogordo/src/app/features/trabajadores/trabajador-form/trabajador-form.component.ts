
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { TrabajadorService } from '../../../core/services/trabajador.service';
import { CargoService } from '../../../core/services/cargo.service';
import { AlertService } from '../../../core/services/alert.service';
import { Cargo } from '../../../core/interfaces/cargo.interface';
import { Trabajador } from '../../../core/interfaces/trabajador.interface';

@Component({
  selector: 'app-trabajador-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './trabajador-form.component.html',
  styleUrl: './trabajador-form.component.css'
})
export class TrabajadorFormComponent implements OnInit {

  private fb = inject(FormBuilder);
  private trabajadorService = inject(TrabajadorService);
  private cargoService = inject(CargoService);
  private alertService = inject(AlertService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  form: FormGroup;

  cargos: Cargo[] = [];

  idTrabajador: number | null = null;
  idCargoOriginal: number | null = null;
  estadoOriginal: string = 'A';

  guardando = false;
  cargando = false;
  cargandoCargos = false;

  private todosLosCargos: Cargo[] = [];

  constructor() {
    this.form = this.fb.group({
      nombre: ['', Validators.required],

      apellidos: ['', Validators.required],

      dni: ['', [
        Validators.required,
        Validators.pattern(/^\d{8}$/)
      ]],

      numTelef: ['', [
        Validators.required,
        Validators.pattern(/^9\d{8}$/)
      ]],

      idCargo: [null, Validators.required],

      direccion: ['', [
        Validators.required,
        Validators.maxLength(200)
      ]],

      capacidadPreparacion: [null, [
        Validators.min(1),
        Validators.pattern(/^\d+$/)
      ]],

      capacidadAsado: [null, [
        Validators.min(1),
        Validators.pattern(/^\d+$/)
      ]],

      puedeRepartir: [false, Validators.required]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id !== null) {
      this.idTrabajador = Number(id);
      this.cargarTrabajador(this.idTrabajador);
    }

    this.cargarCargos();
  }

  // IDENTIFICAR CARGO GERENTE GENERAL
  private esCargoGerente(cargo: Cargo): boolean {
    return cargo.nombre.trim().toLowerCase() === 'gerente general';
  }

  // FILTRAR LOS CARGOS DISPONIBLES
  private actualizarCargosDisponibles(): void {
    this.cargos = this.todosLosCargos.filter(cargo => {
      if (!this.esCargoGerente(cargo)) {
        return true;
      }

      // Solo conservar Gerente General cuando el
      // trabajador que se está editando ya tiene ese cargo.
      return (
        this.idTrabajador !== null &&
        cargo.idCargo === this.idCargoOriginal
      );
    });
  }

  // CARGAR CARGOS DESDE EL BACKEND
  cargarCargos(): void {
    this.cargandoCargos = true;

    this.cargoService.getCargos().subscribe({
      next: (data) => {
        this.todosLosCargos = data;
        this.actualizarCargosDisponibles();
        this.cargandoCargos = false;
      },
      error: (err) => {
        console.error('Error al cargar cargos:', err);
        this.cargandoCargos = false;
        this.alertService.error('No se pudieron cargar los cargos');
      }
    });
  }

  // CARGAR DATOS DEL TRABAJADOR AL EDITAR
  cargarTrabajador(id: number): void {
    this.cargando = true;

    this.trabajadorService.getTrabajadorById(id).subscribe({
      next: (trabajador) => {
        this.idCargoOriginal = trabajador.idCargo;
        this.estadoOriginal = trabajador.estado ?? 'A';

        this.form.patchValue({
          nombre: trabajador.persona.nombre,
          apellidos: trabajador.persona.apellidos,
          dni: trabajador.persona.dni,
          numTelef: trabajador.persona.numTelef,
          idCargo: trabajador.idCargo,
          direccion: trabajador.direccion,
          capacidadPreparacion: trabajador.capacidadPreparacion,
          capacidadAsado: trabajador.capacidadAsado,
          puedeRepartir: trabajador.puedeRepartir
        });

        // Actualiza el selector después de conocer
        // el cargo original del trabajador.
        this.actualizarCargosDisponibles();

        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar trabajador:', err);
        this.cargando = false;
        this.alertService.error('No se pudo cargar el trabajador');
      }
    });
  }

  // GUARDAR NUEVO TRABAJADOR O ACTUALIZAR
  guardar(): void {
    if (
      this.form.invalid ||
      this.guardando ||
      this.cargando ||
      this.cargandoCargos
    ) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.value;

    const preparacion = this.convertirCapacidad(
      valores.capacidadPreparacion
    );

    const asado = this.convertirCapacidad(
      valores.capacidadAsado
    );

    const cargoSeleccionado = this.cargos.find(
      cargo => cargo.idCargo === Number(valores.idCargo)
    );

    if (!cargoSeleccionado) {
      this.alertService.error('Selecciona un cargo válido');
      return;
    }

    const esGerente = this.esCargoGerente(cargoSeleccionado);

    // Validar que el trabajador operativo tenga
    // al menos una función habilitada.
    if (
      !esGerente &&
      preparacion === null &&
      asado === null &&
      !valores.puedeRepartir
    ) {
      this.alertService.error(
        'El trabajador debe tener al menos una función habilitada'
      );
      return;
    }

    // Evitar registrar un gerente nuevo.
    if (this.idTrabajador === null && esGerente) {
      this.alertService.error(
        'El cargo Gerente General no está disponible para nuevos trabajadores'
      );
      return;
    }

    const trabajador: Trabajador = {
      persona: {
        nombre: valores.nombre.trim(),
        apellidos: valores.apellidos.trim(),
        dni: valores.dni,
        numTelef: valores.numTelef
      },

      idCargo: Number(valores.idCargo),

      direccion: valores.direccion.trim(),

      capacidadPreparacion: preparacion,

      capacidadAsado: asado,

      puedeRepartir: Boolean(valores.puedeRepartir),

      // Al crear: A (Activo).
      // Al editar: conservar el estado existente.
      estado: this.idTrabajador !== null
        ? this.estadoOriginal
        : 'A'
    };

    const request = this.idTrabajador !== null
      ? this.trabajadorService.actualizarTrabajador(
          this.idTrabajador,
          trabajador
        )
      : this.trabajadorService.crearTrabajador(trabajador);

    this.guardando = true;

    request.subscribe({
      next: () => {
        this.guardando = false;

        this.alertService.success(
          this.idTrabajador !== null
            ? 'Trabajador actualizado correctamente'
            : 'Trabajador registrado correctamente'
        );

        this.router.navigate(['/trabajadores']);
      },
      error: (err) => {
        this.guardando = false;

        console.error('Error al guardar trabajador:', err);

        let mensaje =
          'No se pudo guardar el trabajador. Verifica los datos ingresados.';

        if (typeof err.error?.message === 'string') {
          mensaje = err.error.message;
        } else if (typeof err.error === 'string') {
          mensaje = err.error;
        }

        this.alertService.error(mensaje);
      }
    });
  }

  // CONVERTIR CAPACIDADES VACÍAS A NULL
  private convertirCapacidad(valor: unknown): number | null {
    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {
      return null;
    }

    return Number(valor);
  }
}
