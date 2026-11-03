
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Vehiculo } from '../../../core/interfaces/vehiculo.interface';
import { VehiculoService } from '../../../core/services/vehiculo.service';

@Component({
  selector: 'app-vehiculo-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './vehiculo-form.component.html',
  styleUrl: './vehiculo-form.component.css'
})
export class VehiculoFormComponent implements OnInit {

  private vehiculoService = inject(VehiculoService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  vehiculo: Vehiculo = {
    tipo: '',
    placa: '',
    capacidad: 0,
    estado: 'ACTIVO'
  };

  esEdicion = false;
  idVehiculo: number | null = null;

  guardando = false;
  cargando = false;
  error = '';

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id !== null) {
      this.idVehiculo = Number(id);

      if (!Number.isInteger(this.idVehiculo) || this.idVehiculo <= 0) {
        this.error = 'El identificador del vehículo no es válido.';
        return;
      }

      this.esEdicion = true;
      this.cargarVehiculo(this.idVehiculo);
    }
  }

  cargarVehiculo(id: number): void {
    this.cargando = true;
    this.error = '';

    this.vehiculoService.getVehiculoById(id).subscribe({
      next: (datos) => {
        this.vehiculo = datos;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar la información del vehículo.';
        this.cargando = false;
      }
    });
  }

  guardar(): void {
    if (this.guardando || this.cargando) return;

    this.error = '';

    this.vehiculo.tipo = this.vehiculo.tipo.trim();
    this.vehiculo.placa = this.vehiculo.placa.trim().toUpperCase();

    if (!this.vehiculo.tipo || !this.vehiculo.placa) {
      this.error = 'El tipo y la placa son obligatorios.';
      return;
    }

    if (
      !Number.isFinite(this.vehiculo.capacidad) ||
      this.vehiculo.capacidad < 0.01
    ) {
      this.error = 'La capacidad debe ser mayor o igual a 0.01.';
      return;
    }

    this.guardando = true;

    const datos: Vehiculo = {
      tipo: this.vehiculo.tipo,
      placa: this.vehiculo.placa,
      capacidad: this.vehiculo.capacidad,
      estado: this.vehiculo.estado
    };

    const peticion = this.esEdicion && this.idVehiculo !== null
      ? this.vehiculoService.updateVehiculo(this.idVehiculo, datos)
      : this.vehiculoService.createVehiculo(datos);

    peticion.subscribe({
      next: () => {
        this.guardando = false;
        this.router.navigate(['/vehiculos']);
      },
      error: (err) => {
        this.guardando = false;

        this.error =
          err.error?.mensaje ||
          err.error?.message ||
          'No se pudo guardar el vehículo. Verifica los datos e inténtalo nuevamente.';
      }
    });
  }
}
