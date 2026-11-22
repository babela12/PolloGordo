
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {

  public authService = inject(AuthService);

  userName: string = '';
  userRole: string = '';
  isAdmin: boolean = false;

  ngOnInit(): void {

    this.authService.isLoggedIn$.subscribe(isLoggedIn => {

      if (isLoggedIn) {

        this.userName =
          localStorage.getItem('user_name') || 'Usuario';

        this.userRole =
          (localStorage.getItem('user_role') || '')
            .trim()
            .toUpperCase();

        this.isAdmin = this.userRole === 'GERENTE';

      } else {

        this.userName = '';
        this.userRole = '';
        this.isAdmin = false;

      }

    });
  }

  logout(): void {
    this.authService.logout();
  }
}
