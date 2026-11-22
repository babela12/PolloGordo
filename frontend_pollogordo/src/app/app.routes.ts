
import { Routes } from '@angular/router';

import { MainLayoutComponent } from './shared/layout/main-layout/main-layout.component';
import { HomeComponent } from './features/home/home.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { ProductFormComponent } from './features/products/product-form/product-form.component';
import { InsumoListComponent } from './features/insumos/insumo-list/insumo-list.component';
import { InsumoFormComponent } from './features/insumos/insumo-form/insumo-form.component';
import { ClienteListComponent } from './features/clientes/cliente-list/cliente-list.component';
import { ClienteFormComponent } from './features/clientes/cliente-form/cliente-form.component';
import { TrabajadorListComponent } from './features/trabajadores/trabajador-list/trabajador-list.component';
import { TrabajadorFormComponent } from './features/trabajadores/trabajador-form/trabajador-form.component';
import { VehiculoListComponent } from './features/vehiculos/vehiculo-list/vehiculo-list.component';
import { VehiculoFormComponent } from './features/vehiculos/vehiculo-form/vehiculo-form.component';
import { PedidoFormComponent } from './features/pedidos/pedido-form/pedido-form.component';
import { PedidoListComponent } from './features/pedidos/pedido-list/pedido-list.component';
import { AsignacionListComponent } from './features/asignaciones/asignacion-list/asignacion-list.component';
import { CuentasComponent } from './features/cuentas/cuenta/cuentas.component';
import { LoginComponent } from './features/login/login.component';
import { ReportesComponent } from './features/reportes/reportes.component';
import { PagoListComponent } from './features/pagos/pago-list/pago-list.component';
import { MiTrabajoComponent } from './features/mi-trabajo/mi-trabajo.component';

import { permitirRoles } from './core/guards/rol.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: 'home', component: HomeComponent,
        canActivate: [permitirRoles(['GERENTE', 'CONTADOR'])] },

      { path: 'mi-trabajo', component: MiTrabajoComponent,
        canActivate: [permitirRoles(['TRABAJADOR'])] },

      { path: 'productos', component: ProductListComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'productos/nuevo', component: ProductFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'productos/editar/:id', component: ProductFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'insumos', component: InsumoListComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'insumos/nuevo', component: InsumoFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'insumos/editar/:id', component: InsumoFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'clientes', component: ClienteListComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'clientes/nuevo', component: ClienteFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'clientes/editar/:id', component: ClienteFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'trabajadores', component: TrabajadorListComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'trabajadores/nuevo', component: TrabajadorFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'trabajadores/editar/:id', component: TrabajadorFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'vehiculos', component: VehiculoListComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'vehiculos/nuevo', component: VehiculoFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'vehiculos/editar/:id', component: VehiculoFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'pedidos', component: PedidoListComponent,
        canActivate: [permitirRoles(['GERENTE', 'CONTADOR'])] },

      { path: 'pedidos/nuevo', component: PedidoFormComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'asignaciones', component: AsignacionListComponent,
        canActivate: [permitirRoles(['GERENTE', 'CONTADOR'])] },

      { path: 'pagos', component: PagoListComponent,
        canActivate: [permitirRoles(['GERENTE', 'CONTADOR'])] },

      { path: 'cuentas', component: CuentasComponent,
        canActivate: [permitirRoles(['GERENTE'])] },

      { path: 'reportes', component: ReportesComponent,
        canActivate: [permitirRoles(['GERENTE', 'CONTADOR'])] },

      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  { path: '**', redirectTo: 'home' }
];
