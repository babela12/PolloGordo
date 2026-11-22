
import { inject } from '@angular/core';

import {
  CanActivateFn,
  Router
} from '@angular/router';

import {
  AuthService,
  RolUsuario
} from '../services/auth.service';

export function permitirRoles(
  rolesPermitidos: RolUsuario[]
): CanActivateFn {

  return () => {

    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.estaAutenticado()) {
      return router.createUrlTree(['/login']);
    }

    const rol = authService.getRol();

    if (rol && rolesPermitidos.includes(rol)) {
      return true;
    }

    if (rol === 'TRABAJADOR') {
      return router.createUrlTree(['/mi-trabajo']);
    }

    if (rol === 'GERENTE' || rol === 'CONTADOR') {
      return router.createUrlTree(['/home']);
    }

    return router.createUrlTree(['/login']);
  };
}
