
export interface ItemPedido {
  idProducto: number;
  cantidad: number;
  unidad: string;
  acompanamiento?: string;
  tipoArroz?: string;
  tipoEnsalada?: string;
}

export interface Pedido {
  idCliente: number;
  items: ItemPedido[];
  fechaEvento: string;
  horaEntrega: string;
  direccionEvento: string;
  telefonoContacto: string;
  observacion?: string;
}
     