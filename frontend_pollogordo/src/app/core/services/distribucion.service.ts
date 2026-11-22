
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Distribucion {
  idDistribucion: number;
  idPedido: number;
  idInsumo: number;
  cantidad: number;
  estado: string;
}

@Injectable({
  providedIn: 'root'
})
export class DistribucionService {

  private http = inject(HttpClient);

  private apiUrl = '/pollogordo_api/distribuciones';

  listarDistribuciones(): Observable<Distribucion[]> {
    return this.http.get<Distribucion[]>(this.apiUrl);
  }

  registrarConsumo(id: number): Observable<Distribucion> {
    return this.http.put<Distribucion>(
      `${this.apiUrl}/${id}/consumir`,
      {}
    );
  }
}
