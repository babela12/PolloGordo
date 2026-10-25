
export interface Insumo {
  idInsumo: number;
  nombre: string;
  unidad: string;
  perecible: string;
  controlaStock: string;
  cantidadStock: number | null;
  estado: string;
  imagen: string | null;
}
