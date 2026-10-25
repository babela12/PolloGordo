import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ClienteService } from '../../../core/services/cliente.service';
import { AlertService } from '../../../core/services/alert.service';
import { Cliente } from '../../../core/interfaces/cliente.interface';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './cliente-form.component.html',
  styleUrl: './cliente-form.component.css'
})
export class ClienteFormComponent implements OnInit {

  private fb = inject(FormBuilder);
  private clienteService = inject(ClienteService);
  private alertService = inject(AlertService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form: FormGroup;
  idCliente: number | null = null;
  guardando = false;
  cargando = false;

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
      direccion: ['']
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id !== null) {
      this.idCliente = Number(id);
      this.cargando = true;

      this.clienteService.getClienteById(this.idCliente).subscribe({
        next: (cliente) => {
          this.form.patchValue({
            nombre: cliente.persona.nombre,
            apellidos: cliente.persona.apellidos,
            dni: cliente.persona.dni,
            numTelef: cliente.persona.numTelef,
            direccion: cliente.direccion
          });

          this.cargando = false;
        },
        error: (err) => {
          console.error(err);
          this.cargando = false;
          this.alertService.error('No se pudo cargar el cliente');
        }
      });
    }
  }

  guardar(): void {
    if (this.form.invalid || this.guardando || this.cargando) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.value;

    const cliente: Cliente = {
      persona: {
        nombre: valores.nombre.trim(),
        apellidos: valores.apellidos.trim(),
        dni: valores.dni,
        numTelef: valores.numTelef
      },
      direccion: valores.direccion?.trim() ?? ''
    };

    const request = this.idCliente !== null
      ? this.clienteService.actualizarCliente(this.idCliente, cliente)
      : this.clienteService.crearCliente(cliente);

    this.guardando = true;

    request.subscribe({
      next: () => {
        this.guardando = false;

        this.alertService.success(
          this.idCliente !== null
            ? 'Cliente actualizado correctamente'
            : 'Cliente registrado correctamente'
        );

        this.router.navigate(['/clientes']);
      },
      error: (err) => {
        this.guardando = false;
        console.error('Error al guardar cliente:', err);

        if (err.status === 400 || err.status === 409) {
          this.alertService.error(
            err.error?.message ||
            'Verifica los datos. Es posible que el DNI ya esté registrado.'
          );
        } else {
          this.alertService.error('No se pudo guardar el cliente');
        }
      }
    });
  }
}