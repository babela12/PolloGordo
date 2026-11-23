import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { EntregaAsignacion } from '../interfaces/asig-trabajador.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EntregaService {
  private http = inject(HttpClient);
 private apiUrl = `${environment.apiUrl}/entregas`;
  getEntregas(): Observable<EntregaAsignacion[]> {
    return this.http.get<EntregaAsignacion[]>(this.apiUrl);
  }
  getEntregaPorId(idEntrega: number): Observable<EntregaAsignacion> {
    return this.http.get<EntregaAsignacion>(`${this.apiUrl}/${idEntrega}`);
  }
  crearEntrega(entrega: EntregaAsignacion): Observable<EntregaAsignacion> {
    return this.http.post<EntregaAsignacion>(this.apiUrl, entrega);
  }
  actualizarEntrega(idEntrega: number, entrega: EntregaAsignacion): Observable<EntregaAsignacion> {
    return this.http.put<EntregaAsignacion>(`${this.apiUrl}/${idEntrega}`, entrega);
  }
  eliminarEntrega(idEntrega: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${idEntrega}`);
  }
}