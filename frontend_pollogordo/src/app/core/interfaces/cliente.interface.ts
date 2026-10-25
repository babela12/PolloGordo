export interface Persona {
  idPersona?: number;
  nombre: string;
  apellidos: string;
  dni: string;
  numTelef: string;
}

export interface Cliente {
  idCliente?: number;
  persona: Persona;
  direccion: string;
}