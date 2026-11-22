
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const router = inject(Router);
  const token = localStorage.getItem('token_ecommerce');

  let authReq = req;

  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error) => {

      // 401: Sesión no válida o token vencido
      if (error.status === 401) {

        // Eliminar solamente los datos de autenticación
        localStorage.removeItem('token_ecommerce');
        localStorage.removeItem('user_id_persona');
        localStorage.removeItem('user_name');
        localStorage.removeItem('user_role');

        // Conservar pedidos_temporales
        // Conservar los IDs de pedidos registrados

        router.navigate(['/login']);
      }

      // 403: Acceso denegado
      // No cerrar sesión ni borrar el carrito
      if (error.status === 403) {
        console.error(
          'Acceso denegado: no tienes permisos para esta operación.'
        );
      }

      return throwError(() => error);
    })
  );
};
