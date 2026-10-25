
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Insumo } from '../interfaces/insumo.interface';

@Injectable({
  providedIn: 'root'
})
export class InsumoService {

  private http = inject(HttpClient);

  private apiUrl = '/pollogordo_api/insumos';

  getInsumos(): Observable<Insumo[]> {
    return this.http.get<Insumo[]>(this.apiUrl);
  }

  getInsumoById(id: number): Observable<Insumo> {
    return this.http.get<Insumo>(
      `${this.apiUrl}/${id}`
    );
  }

  crearInsumo(formData: FormData): Observable<Insumo> {
    return this.http.post<Insumo>(
      this.apiUrl,
      formData
    );
  }

  updateInsumo(id: number, formData: FormData): Observable<Insumo> {
    return this.http.put<Insumo>(
      `${this.apiUrl}/${id}`,
      formData
    );
  }

  eliminarInsumo(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }
}
