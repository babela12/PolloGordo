
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';


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

  private apiUrl = `${environment.apiUrl}/distribuciones`;

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
