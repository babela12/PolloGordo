import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent {

  public autor: any = {
    nombre: 'Multiservicios',
    apellidos: 'Pollo Gordo',
    anio: '2026'
  }

}