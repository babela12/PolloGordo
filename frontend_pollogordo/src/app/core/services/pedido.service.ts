
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pedido } from '../interfaces/pedido';

@Injectable({
  providedIn: 'root'
})
export class PedidoService {

  private http = inject(HttpClient);
  private apiUrl = '/pollogordo_api/pedidos';

  // Listar todos los pedidos
  getPedidos(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // Buscar pedido por ID
  getPedidoById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  // Registrar nuevo pedido
  crearPedido(pedido: Pedido): Observable<any> {
    return this.http.post<any>(this.apiUrl, pedido);
  }

  // Modificar pedido
  actualizarPedido(id: number, pedido: Pedido): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, pedido);
  }

  // Confirmar pedido
  confirmarPedido(id: number): Observable<any> {
    return this.http.put<any>(
      `${this.apiUrl}/${id}/confirmar`,
      {}
    );
  }
}
