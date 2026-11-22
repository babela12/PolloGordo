
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { ILoginResponse } from '../interfaces/auth.interface';

import {
  BehaviorSubject,
  catchError,
  map,
  Observable,
  of,
  tap
} from 'rxjs';

export type RolUsuario =
  'GERENTE' | 'CONTADOR' | 'TRABAJADOR';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);
  private router = inject(Router);

  private apiUrl = '/pollogordo_api/api/v1/auth';

  private loggedIn = new BehaviorSubject<boolean>(
    !!localStorage.getItem('token_ecommerce')
  );

  isLoggedIn$ = this.loggedIn.asObservable();

  login(credentials: {
    usuario: string;
    password: string;
  }): Observable<boolean> {

    return this.http.post<ILoginResponse>(
      `${this.apiUrl}/login`,
      credentials
    ).pipe(

      tap(response => {

        if (!response?.token) {
          return;
        }

        localStorage.setItem(
          'token_ecommerce',
          response.token
        );

        if (response.idPersona != null) {
          localStorage.setItem(
            'user_id_persona',
            response.idPersona.toString()
          );
        } else {
          localStorage.removeItem('user_id_persona');
        }

        localStorage.setItem(
          'user_role',
          response.rol
        );

        localStorage.setItem(
          'user_name',
          response.nombreMostrar
        );

        this.loggedIn.next(true);

        if (response.rol === 'TRABAJADOR') {
          this.router.navigate(['/mi-trabajo']);
        } else {
          this.router.navigate(['/home']);
        }
      }),

      map(response => !!response?.token),

      catchError(error => {
        console.error('Error al iniciar sesión:', error);
        this.limpiarSesion();
        return of(false);
      })
    );
  }

  private limpiarSesion(): void {
    localStorage.removeItem('token_ecommerce');
    localStorage.removeItem('user_id_persona');
    localStorage.removeItem('user_name');
    localStorage.removeItem('user_role');

    this.loggedIn.next(false);
  }

  logout(): void {
    this.limpiarSesion();
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token_ecommerce');
  }

  getRol(): RolUsuario | null {
    const rol = localStorage.getItem('user_role');

    if (
      rol === 'GERENTE' ||
      rol === 'CONTADOR' ||
      rol === 'TRABAJADOR'
    ) {
      return rol;
    }

    return null;
  }

  esGerente(): boolean {
    return this.getRol() === 'GERENTE';
  }

  esContador(): boolean {
    return this.getRol() === 'CONTADOR';
  }

  esTrabajador(): boolean {
    return this.getRol() === 'TRABAJADOR';
  }

  estaAutenticado(): boolean {
    return !!this.getToken();
  }

  registrar(datos: any): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/registro`,
      datos
    );
  }
}
