
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { InsumoService } from '../../../core/services/insumo.service';
import { Insumo } from '../../../core/interfaces/insumo.interface';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-insumo-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './insumo-list.component.html',
  styleUrl: './insumo-list.component.css'
})
export class InsumoListComponent implements OnInit {

  private insumoService = inject(InsumoService);
  private alertService = inject(AlertService);

  insumos: Insumo[] = [];
  busqueda: string = '';
  cargando: boolean = false;
  error: string = '';

  ngOnInit(): void {
    this.listarInsumos();
  }

  listarInsumos(): void {
    this.cargando = true;
    this.error = '';

    this.insumoService.getInsumos().subscribe({
      next: (data) => {
        this.insumos = data;
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error al cargar insumos:', err);
        this.error = 'No se pudieron cargar los insumos';
        this.cargando = false;
      }
    });
  }

  get insumosFiltrados(): Insumo[] {
    return this.insumos.filter(insumo =>
      insumo.nombre.toLowerCase().includes(
        this.busqueda.toLowerCase()
      )
    );
  }

  getImagen(insumo: Insumo): string {
    return insumo.imagen
      ? '/pollogordo_api/images/' + insumo.imagen
      : '/pollogordo_api/images/no-image.png';
  }

  getStock(insumo: Insumo): string {
    if (insumo.controlaStock === 'N') {
      return 'No aplica';
    }

    return `${insumo.cantidadStock ?? 0} ${insumo.unidad}`;
  }

  async eliminar(id: number): Promise<void> {

    const confirmado = await this.alertService.confirm(
      'Esta acción no se puede deshacer. ¿Deseas eliminar este insumo?'
    );

    if (confirmado) {

      this.insumoService.eliminarInsumo(id).subscribe({
        next: () => {
          this.alertService.toast('Insumo eliminado correctamente');
          this.listarInsumos();
        },
        error: (err) => {
          console.error('Error al eliminar el insumo:', err);
          this.alertService.error(
            'No se pudo eliminar el insumo. Puede estar relacionado con otros registros.'
          );
        }
      });

    }
  }
}
