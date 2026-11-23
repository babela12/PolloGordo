import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Cargo } from '../interfaces/cargo.interface';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CargoService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/cargos`;

  getCargos(): Observable<Cargo[]> {
    return this.http.get<Cargo[]>(this.apiUrl);
  }
}