
import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  success(message: string, title: string = '¡Logrado!') {
    Swal.fire({
      title: title,
      text: message,
      icon: 'success',
      confirmButtonColor: '#7B1E21'
    });
  }

  error(message: string, title: string = 'Error') {
    Swal.fire({
      title: title,
      text: message,
      icon: 'error',
      confirmButtonColor: '#7B1E21'
    });
  }

  async confirm(
    message: string,
    title: string = '¿Estás seguro?'
  ): Promise<boolean> {

    const result = await Swal.fire({
      title: title,
      text: message,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#7B1E21',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Sí, continuar',
      cancelButtonText: 'Cancelar'
    });

    return result.isConfirmed;
  }

  toast(
    message: string,
    icon: 'success' | 'error' | 'info' = 'success'
  ) {
    const Toast = Swal.mixin({
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true
    });

    Toast.fire({
      icon: icon,
      title: message
    });
  }
}
