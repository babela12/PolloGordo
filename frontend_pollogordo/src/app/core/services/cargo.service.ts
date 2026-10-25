import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Cargo } from '../interfaces/cargo.interface';

@Injectable({
  providedIn: 'root'
})
export class CargoService {

  private http = inject(HttpClient);
  private apiUrl = '/pollogordo_api/cargos';

  getCargos(): Observable<Cargo[]> {
    return this.http.get<Cargo[]>(this.apiUrl);
  }
}