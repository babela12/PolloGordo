
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';


export interface PedidoHome {
  idPedido: number;
  cliente: string;
  total: number;
  estado: string;
  fecha: string;
  fechaEvento: string;
  horaEntrega: string;
  direccionEvento: string;
  telefonoContacto: string;
  confirmado: string;
  fechaConfirmacion: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class HomeService {

  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/pedidos`;

  listarPedidos(): Observable<PedidoHome[]> {
    return this.http.get<PedidoHome[]>(this.apiUrl);
  }
}
