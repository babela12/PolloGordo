
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AlertService } from '../../core/services/alert.service';
import { AuthService } from '../../core/services/auth.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  private alertService = inject(AlertService);
  private authService = inject(AuthService);

  username = '';
  password = '';

  mostrarPassword = false;
  isLoading = false;
  loginError = false;

  login(): void {

    if (!this.username.trim() || !this.password.trim()) {
      this.alertService.error('Ingresa tu usuario y contraseña.');
      return;
    }

    this.isLoading = true;
    this.loginError = false;

    this.authService.login({
      usuario: this.username.trim(),
      password: this.password
    })
    .pipe(
      finalize(() => this.isLoading = false)
    )
    .subscribe({
      next: (success) => {

        if (success) {

          // CERRAR MODAL SI ESTÁ ABIERTO
          document.getElementById('closeLoginModal')?.click();

          this.alertService.toast('Bienvenido a Pollo Gordo');

          this.password = '';

        } else {

          this.loginError = true;
          this.alertService.error(
            'Usuario o contraseña incorrecta'
          );

        }
      },
      error: () => {
        this.loginError = true;
        this.alertService.error(
          'No se pudo iniciar sesión.'
        );
      }
    });
  }
}
