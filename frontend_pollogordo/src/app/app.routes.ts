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

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: 'home', component: HomeComponent },

      { path: 'productos', component: ProductListComponent },
      { path: 'productos/nuevo', component: ProductFormComponent },
      { path: 'productos/editar/:id', component: ProductFormComponent },

      { path: 'insumos', component: InsumoListComponent },
      { path: 'insumos/nuevo', component: InsumoFormComponent },
      { path: 'insumos/editar/:id', component: InsumoFormComponent },

      { path: 'clientes', component: ClienteListComponent },
      { path: 'clientes/nuevo', component: ClienteFormComponent },
      { path: 'clientes/editar/:id', component: ClienteFormComponent },

      { path: 'trabajadores', component: TrabajadorListComponent },
      { path: 'trabajadores/nuevo', component: TrabajadorFormComponent },
      { path: 'trabajadores/editar/:id', component: TrabajadorFormComponent },


      { path: 'vehiculos', component: VehiculoListComponent },
      { path: 'vehiculos/nuevo', component: VehiculoFormComponent },
      { path: 'vehiculos/editar/:id', component: VehiculoFormComponent }

    ]
  },
  {
    path: '**',
    redirectTo: 'home'
  }
];