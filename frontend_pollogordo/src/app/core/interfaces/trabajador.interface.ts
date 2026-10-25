import { Persona } from './cliente.interface';

export interface Trabajador {
  idTrabajador?: number;
  persona: Persona;
  idCargo: number;
  direccion: string;
  capacidadPreparacion: number | null;
  capacidadAsado: number | null;
  puedeRepartir: boolean;
  estado: string;
}