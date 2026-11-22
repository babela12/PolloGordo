export interface ILoginResponse {
  token: string;
  usuario: string;
  rol: string;
  idPersona: number | null;
  nombreMostrar: string;
}
