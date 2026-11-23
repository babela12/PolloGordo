import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AsigTrabajador } from '../interfaces/asig-trabajador.interface';
import { environment } from '../../../environments/environment';
@Injectable({ providedIn: 'root' })
export class AsigTrabajadorService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/asignaciones`;
  getAsignaciones(): Observable<AsigTrabajador[]> {
    return this.http.get<AsigTrabajador[]>(this.apiUrl);
  }
  getAsignacionesPorPedido(idPedido: number): Observable<AsigTrabajador[]> {
    return this.http.get<AsigTrabajador[]>(`${this.apiUrl}/pedido/${idPedido}`);
  }
  crearAsignacion(asignacion: AsigTrabajador): Observable<AsigTrabajador> {
    return this.http.post<AsigTrabajador>(this.apiUrl, asignacion);
  }
  crearTrabajador(asignacion: AsigTrabajador): Observable<AsigTrabajador> {
    return this.http.post<AsigTrabajador>(`${this.apiUrl}/trabajador`, asignacion);
  }
  actualizarAsignacion(idAsig: number, asignacion: AsigTrabajador): Observable<AsigTrabajador> {
    return this.http.put<AsigTrabajador>(`${this.apiUrl}/${idAsig}`, asignacion);
  }
  eliminarAsignacion(idAsig: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${idAsig}`);
  }
}