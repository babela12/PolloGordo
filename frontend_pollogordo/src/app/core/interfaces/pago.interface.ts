
export interface Pago {
  idPago: number;
  idPedido: number;
  metodo: 'YAPE' | 'PLIN' | 'TRANSFERENCIA' | 'EFECTIVO';
  monto: number;
  fechaPago: string;
  referencia: string | null;
  estado: string;
  evidencia: string | null;
}

export interface PagoRequest {
  idPedido: number;
  metodo: 'YAPE' | 'PLIN' | 'TRANSFERENCIA' | 'EFECTIVO';
  monto: number;
  fechaPago?: string;
  referencia?: string | null;
}

export interface ResumenPago {
  idPedido: number;
  totalPedido: number;
  totalAbonado: number;
  saldoPendiente: number;
  estadoPago: 'PENDIENTE' | 'PARCIAL' | 'PAGADO';
}
