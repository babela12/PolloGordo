
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Vehiculo } from '../../../core/interfaces/vehiculo.interface';
import { VehiculoService } from '../../../core/services/vehiculo.service';

@Component({
  selector: 'app-vehiculo-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './vehiculo-list.component.html',
  styleUrl: './vehiculo-list.component.css'
})
export class VehiculoListComponent implements OnInit {

  private vehiculoService = inject(VehiculoService);

  vehiculos: Vehiculo[] = [];
  vehiculosFiltrados: Vehiculo[] = [];

  vehiculoSeleccionado: Vehiculo | null = null;

  cargando = false;
  error = '';
  busqueda = '';

  ngOnInit(): void {
    this.cargarVehiculos();
  }

  cargarVehiculos(): void {
    this.cargando = true;
    this.error = '';

    this.vehiculoService.getVehiculos().subscribe({
      next: (datos) => {
        this.vehiculos = datos;
        this.filtrarVehiculos();
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar los vehículos.';
        this.cargando = false;
      }
    });
  }

  filtrarVehiculos(): void {
    const texto = this.busqueda.toLowerCase().trim();

    this.vehiculosFiltrados = this.vehiculos.filter(v =>
      v.tipo?.toLowerCase().includes(texto) ||
      v.placa?.toLowerCase().includes(texto) ||
      v.estado?.toLowerCase().includes(texto)
    );
  }

  buscar(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.busqueda = input.value;
    this.filtrarVehiculos();
  }

  verDetalles(vehiculo: Vehiculo): void {
    this.vehiculoSeleccionado = vehiculo;
  }

  cerrarDetalles(): void {
    this.vehiculoSeleccionado = null;
  }

  eliminarVehiculo(vehiculo: Vehiculo): void {
    if (!vehiculo.idVehiculo) return;

    const confirmar = window.confirm(
      `¿Estás segura de eliminar el vehículo con placa ${vehiculo.placa}?`
    );

    if (!confirmar) return;

    this.vehiculoService.deleteVehiculo(vehiculo.idVehiculo).subscribe({
      next: () => {
        this.cargarVehiculos();
      },
      error: () => {
        window.alert(
          'No se pudo eliminar el vehículo. Puede estar asociado a otros registros.'
        );
      }
    });
  }
}
