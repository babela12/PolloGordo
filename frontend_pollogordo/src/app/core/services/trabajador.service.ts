import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Trabajador } from '../interfaces/trabajador.interface';

@Injectable({
  providedIn: 'root'
})
export class TrabajadorService {

  private http = inject(HttpClient);
  private apiUrl = '/pollogordo_api/trabajadores';

  getTrabajadores(): Observable<Trabajador[]> {
    return this.http.get<Trabajador[]>(this.apiUrl);
  }

  getTrabajadorById(id: number): Observable<Trabajador> {
    return this.http.get<Trabajador>(`${this.apiUrl}/${id}`);
  }

  crearTrabajador(trabajador: Trabajador): Observable<Trabajador> {
    return this.http.post<Trabajador>(this.apiUrl, trabajador);
  }

  actualizarTrabajador(id: number, trabajador: Trabajador): Observable<Trabajador> {
    return this.http.put<Trabajador>(`${this.apiUrl}/${id}`, trabajador);
  }

  cambiarEstado(id: number, estado: 'A' | 'I'): Observable<Trabajador> {
    const params = new HttpParams().set('estado', estado);

    return this.http.patch<Trabajador>(
      `${this.apiUrl}/${id}/estado`,
      null,
      { params }
    );
  }
}