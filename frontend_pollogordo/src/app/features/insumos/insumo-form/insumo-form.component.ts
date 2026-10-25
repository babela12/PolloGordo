
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { InsumoService } from '../../../core/services/insumo.service';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-insumo-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './insumo-form.component.html',
  styleUrl: './insumo-form.component.css'
})
export class InsumoFormComponent implements OnInit {

  private fb = inject(FormBuilder);
  private insumoService = inject(InsumoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private alertService = inject(AlertService);

  form: FormGroup;
  idInsumo: number | null = null;
  fotoSeleccionada: File | null = null;
  guardando = false;

  constructor() {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      unidad: ['', Validators.required],
      perecible: ['N', Validators.required],
      controlaStock: ['N', Validators.required],
      cantidadStock: [null],
      estado: ['ACTIVO', Validators.required]
    });

    this.form.get('controlaStock')?.valueChanges.subscribe(valor => {
      const stock = this.form.get('cantidadStock');

      if (valor === 'S') {
        stock?.setValidators([
          Validators.required,
          Validators.min(0)
        ]);
      } else {
        stock?.clearValidators();
        stock?.setValue(null, { emitEvent: false });
      }

      stock?.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.idInsumo = idParam ? Number(idParam) : null;

    if (this.idInsumo !== null && !Number.isNaN(this.idInsumo)) {
      this.insumoService.getInsumoById(this.idInsumo).subscribe({
        next: (insumo) => {
          this.form.patchValue({
            nombre: insumo.nombre,
            unidad: insumo.unidad,
            perecible: insumo.perecible,
            controlaStock: insumo.controlaStock,
            cantidadStock: insumo.cantidadStock,
            estado: insumo.estado
          });
        },
        error: (err) => {
          console.error(err);
          this.alertService.error('Error al cargar el insumo');
        }
      });
    }
  }

  onFileSeleccionada(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      this.fotoSeleccionada = input.files[0];
    }
  }

  guardar(): void {
    if (this.form.invalid || this.guardando) {
      this.form.markAllAsTouched();
      return;
    }

    const formData = new FormData();

    formData.append('nombre', this.form.value.nombre);
    formData.append('unidad', this.form.value.unidad);
    formData.append('perecible', this.form.value.perecible);
    formData.append('controlaStock', this.form.value.controlaStock);
    formData.append('estado', this.form.value.estado);

    if (this.form.value.controlaStock === 'S') {
      formData.append(
        'cantidadStock',
        String(this.form.value.cantidadStock)
      );
    }

    if (this.fotoSeleccionada) {
      formData.append('file', this.fotoSeleccionada);
    }

    const request = this.idInsumo !== null
      ? this.insumoService.updateInsumo(this.idInsumo, formData)
      : this.insumoService.crearInsumo(formData);

    this.guardando = true;

    request.subscribe({
      next: () => {
        this.guardando = false;

        this.alertService.success(
          this.idInsumo !== null
            ? 'Insumo actualizado correctamente'
            : 'Insumo creado correctamente'
        );

        this.router.navigate(['/insumos']);
      },
      error: (err) => {
        this.guardando = false;
        console.error(err);
        this.alertService.error('Ocurrió un error al guardar el insumo');
      }
    });
  }
}
