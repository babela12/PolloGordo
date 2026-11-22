
export type FuncionTrabajador =
  'PREPARADOR' | 'ASADOR' | 'REPARTIDOR';

export type EstadoAsignacion =
  'PROGRAMADO' | 'EN_PROCESO' | 'COMPLETADO' | 'CANCELADO';

export interface EntregaAsignacion {
  idEntrega?: number;
  idPedido: number;
  idVehiculo: number;
  idTrabajador: number;
  fechaEntrega: string;
  horaSalida?: string;
  horaLlegada?: string;
  cantidadPollos: number;
  estado?: string;
  observacion?: string;
}

export interface AsigTrabajador {
  idAsig?: number;
  idPedido: number;
  idTrabajador?: number;
  funcion?: FuncionTrabajador;
  fechaTrabajo?: string;
  horaInicio?: string;
  horaFin?: string;
  cantidadAsignada?: number;
  cantidadRealizada?: number;
  tarifaAplicada?: number;
  pagoAdicional?: number;
  estado?: EstadoAsignacion;
  trabajadores?: AsigTrabajador[];
  entregas?: EntregaAsignacion[];
}
