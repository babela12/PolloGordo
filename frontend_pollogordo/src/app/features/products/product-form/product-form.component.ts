
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ProductoService } from '../../../core/services/producto.service';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css'
})
export class ProductFormComponent implements OnInit {

  private fb = inject(FormBuilder);
  private productoService = inject(ProductoService);
  private alertService = inject(AlertService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form: FormGroup;
  idProducto: number | null = null;
  imagenSeleccionada: File | null = null;
  imagenActual: string | null = null;
  guardando = false;

  constructor() {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      descripcion: [''],
      tipo: [''],
      precio: [null, [Validators.required, Validators.min(0.01)]],
      estado: ['A', Validators.required]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id !== null) {
      this.idProducto = Number(id);

      this.productoService.getProductoById(this.idProducto).subscribe({
        next: (producto) => {
          this.form.patchValue({
            nombre: producto.nombre,
            descripcion: producto.descripcion,
            tipo: producto.tipo,
            precio: producto.precio,
            estado: producto.estado
          });

          this.imagenActual = producto.imagen;
        },
        error: (err) => {
          console.error(err);
          this.alertService.error('No se pudo cargar el producto');
        }
      });
    }
  }

  seleccionarImagen(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      this.imagenSeleccionada = input.files[0];
    }
  }

  guardar(): void {
    if (this.form.invalid || this.guardando) {
      this.form.markAllAsTouched();
      return;
    }

    const formData = new FormData();

    formData.append('nombre', this.form.value.nombre);
    formData.append('descripcion', this.form.value.descripcion ?? '');
    formData.append('tipo', this.form.value.tipo ?? '');
    formData.append('precio', String(this.form.value.precio));
    formData.append('estado', this.form.value.estado);

    if (this.imagenSeleccionada) {
      formData.append('file', this.imagenSeleccionada);
    }

    const request = this.idProducto !== null
      ? this.productoService.updateProduct(this.idProducto, formData)
      : this.productoService.createProduct(formData);

    this.guardando = true;

    request.subscribe({
      next: () => {
        this.guardando = false;

        this.alertService.success(
          this.idProducto !== null
            ? 'Producto actualizado correctamente'
            : 'Producto registrado correctamente'
        );

        this.router.navigate(['/productos']);
      },
      error: (err) => {
        this.guardando = false;
        console.error(err);
        this.alertService.error('No se pudo guardar el producto');
      }
    });
  }

  getImagenActual(): string {
    return this.imagenActual
      ? '/pollogordo_api/images/' + this.imagenActual
      : '/pollogordo_api/images/no-image.png';
  }
}
