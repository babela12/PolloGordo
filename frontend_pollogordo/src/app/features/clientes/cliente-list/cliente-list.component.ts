
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ClienteService } from '../../../core/services/cliente.service';
import { Cliente } from '../../../core/interfaces/cliente.interface';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-cliente-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './cliente-list.component.html',
  styleUrl: './cliente-list.component.css'
})
export class ClienteListComponent implements OnInit {

  private clienteService = inject(ClienteService);
  private alertService = inject(AlertService);

  clientes: Cliente[] = [];
  busqueda = '';
  cargando = false;

  clienteSeleccionado: Cliente | null = null;
  mostrarDetalles = false;

  ngOnInit(): void {
    this.listarClientes();
  }

  listarClientes(): void {
    this.cargando = true;

    this.clienteService.getClientes().subscribe({
      next: (data) => {
        this.clientes = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar clientes:', err);
        this.cargando = false;
        this.alertService.error('No se pudieron cargar los clientes');
      }
    });
  }

  get clientesFiltrados(): Cliente[] {
    const texto = this.busqueda.toLowerCase().trim();

    return this.clientes.filter(cliente => {
      const persona = cliente.persona;

      return (
        persona.nombre.toLowerCase().includes(texto) ||
        persona.apellidos.toLowerCase().includes(texto) ||
        persona.dni.includes(texto) ||
        persona.numTelef.includes(texto)
      );
    });
  }

  verDetalles(cliente: Cliente): void {
    this.clienteSeleccionado = cliente;
    this.mostrarDetalles = true;
  }

  cerrarDetalles(): void {
    this.mostrarDetalles = false;
    this.clienteSeleccionado = null;
  }
}
