
export interface ReportePedido {
  idPedido: number;
  cliente: string;
  fechaEvento: string;
  horaEntrega: string | null;
  direccionEvento: string | null;
  estado: string | null;
  confirmado: string | null;
  total: number;
}

export interface ReporteInsumo {
  idInsumo: number;
  insumo: string;
  unidad: string;
  cantidad: number;
  pedidos: number;
}

export interface ReporteEventos {
  fechaInicio: string;
  fechaFin: string;
  soloConfirmados: boolean;
  pedidos: ReportePedido[];
  insumos: ReporteInsumo[];
  totalPollos: number;
}
