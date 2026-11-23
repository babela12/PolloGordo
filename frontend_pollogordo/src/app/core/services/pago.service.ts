
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';


import {
  Pago,
  PagoRequest,
  ResumenPago
} from '../interfaces/pago.interface';

@Injectable({
  providedIn: 'root'
})
export class PagoService {

  private readonly apiUrl = `${environment.apiUrl}/pagos`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Pago[]> {
    return this.http.get<Pago[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Pago> {
    return this.http.get<Pago>(`${this.apiUrl}/${id}`);
  }

  listarPorPedido(idPedido: number): Observable<Pago[]> {
    return this.http.get<Pago[]>(
      `${this.apiUrl}/pedido/${idPedido}`
    );
  }

  obtenerResumen(idPedido: number): Observable<ResumenPago> {
    return this.http.get<ResumenPago>(
      `${this.apiUrl}/pedido/${idPedido}/resumen`
    );
  }

  crear(pago: PagoRequest): Observable<Pago> {
    return this.http.post<Pago>(this.apiUrl, pago);
  }

  actualizar(id: number, pago: PagoRequest): Observable<Pago> {
    return this.http.put<Pago>(
      `${this.apiUrl}/${id}`,
      pago
    );
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  subirEvidencia(id: number, archivo: File): Observable<Pago> {
    const formData = new FormData();
    formData.append('archivo', archivo);

    return this.http.post<Pago>(
      `${this.apiUrl}/${id}/evidencia`,
      formData
    );
  }

  obtenerEvidencia(id: number): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/${id}/evidencia`,
      { responseType: 'blob' }
    );
  }
}
