
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ReporteEventos } from '../interfaces/reporte.interface';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {

  private http = inject(HttpClient);
  private apiUrl = '/pollogordo_api/reportes';

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
