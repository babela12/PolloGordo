
import { Injectable, computed, signal } from '@angular/core';
import { Pedido } from '../interfaces/pedido';

export interface PedidoTemporal {
  idTemporal: number;
  nombreCliente: string;
  pedido: Pedido;
}

@Injectable({
  providedIn: 'root'
})
export class PedidoTemporalService {

  private pedidos = signal<PedidoTemporal[]>(this.cargarPedidos());

  totalPedidos = computed(() => this.pedidos().length);

  private cargarPedidos(): PedidoTemporal[] {
    const datos = localStorage.getItem('pedidos_temporales');
    return datos ? JSON.parse(datos) : [];
  }

  getPedidos() {
    return this.pedidos();
  }

  private guardarCambios() {
    localStorage.setItem(
      'pedidos_temporales',
      JSON.stringify(this.pedidos())
    );
  }

  guardarPedido(nombreCliente: string, pedido: Pedido) {
    const nuevoPedido: PedidoTemporal = {
      idTemporal: Date.now(),
      nombreCliente: nombreCliente,
      pedido: pedido
    };

    this.pedidos.set([...this.pedidos(), nuevoPedido]);
    this.guardarCambios();
  }

  buscarPedido(id: number) {
    return this.pedidos().find(
      item => item.idTemporal === id
    );
  }

  editarPedido(id: number, pedido: Pedido) {
    const lista = this.pedidos().map(item => {
      if (item.idTemporal === id) {
        return { ...item, pedido: pedido };
      }

      return item;
    });

    this.pedidos.set(lista);
    this.guardarCambios();
  }

  eliminarPedido(id: number) {
    this.pedidos.set(
      this.pedidos().filter(item => item.idTemporal !== id)
    );

    this.guardarCambios();
  }
}
