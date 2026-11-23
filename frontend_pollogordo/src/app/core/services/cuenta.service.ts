
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Cuenta {
  idUsuario: number;
  usuario: string;
  rol: string;
  nombrePersona: string;
  apellidos: string;
  idPersona: number;
}

export interface NuevaCuenta {
  usuario: string;
  password: string;
  rol: 'GERENTE' | 'CONTADOR' | 'TRABAJADOR';
  idPersona: number;
}

@Injectable({
  providedIn: 'root'
})
export class CuentaService {

  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/api/v1`;

  listar(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(
      `${this.apiUrl}/cuentas`
    );
  }

  crear(datos: NuevaCuenta): Observable<Cuenta> {
    return this.http.post<Cuenta>(
      `${this.apiUrl}/auth/registro`,
      datos
    );
  }
}
