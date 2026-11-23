
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ReporteEventos } from '../interfaces/reporte.interface';
import { environment } from '../../../environments/environment';



@Injectable({
  providedIn: 'root'
})
export class ReporteService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/reportes`;

  getReporteEventos(
    fechaInicio: string,
    fechaFin: string,
    soloConfirmados: boolean
  ) {

    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin)
      .set('soloConfirmados', soloConfirmados);

    return this.http.get<ReporteEventos>(
      `${this.apiUrl}/eventos`,
      { params }
    );
  }
}
