
# Sistema Web de Gestión - Multiservicios Pollo Gordo

## Descripción del proyecto

Multiservicios Pollo Gordo es una empresa dedicada a la preparación y distribución de alimentos para eventos sociales, deportivos y privados.

El presente proyecto consiste en el desarrollo de un sistema web que permite gestionar los pedidos, clientes, trabajadores, productos, insumos y otras operaciones de la empresa.

El sistema busca mejorar la organización de los pedidos, reducir los errores en el registro de información y facilitar el control de las actividades que realiza la empresa.

## Información académica

- Universidad: Universidad Peruana Unión
- Campus: Tarapoto
- Semestre académico: 2026-2
- Línea: Software
- Tipo de proyecto: Proyecto Integrador (PI)
- Cursos: Lenguaje de Programación II, Análisis y Diseño de Sistemas de Información y Base de Datos

## Tecnologías utilizadas

| Tecnología | Uso |
|------------|-----|
| Java 21 | Desarrollo del backend |
| Spring Boot 3.4.1 | API REST |
| Spring Security | Seguridad y autorización |
| JWT | Autenticación de usuarios |
| Angular | Desarrollo del frontend |
| TypeScript | Programación del frontend |
| HTML y CSS | Estructura y estilos |
| Bootstrap | Diseño de interfaces |
| Oracle Database 21c | Base de datos |
| Maven | Gestión de dependencias |
| Git y GitHub | Control de versiones |

## Arquitectura del sistema

El proyecto utiliza una arquitectura cliente-servidor, separando el frontend, backend y base de datos.

- Frontend: aplicación web desarrollada con Angular.
- Backend: API REST desarrollada con Spring Boot.
- Base de datos: Oracle Database 21c.
- Seguridad: autenticación JWT y control de acceso mediante Spring Security.

La aplicación Angular realiza peticiones HTTP al backend, donde se procesan las operaciones y se establece la comunicación con Oracle.

## Módulos del sistema

### Clientes
Permite registrar, consultar, actualizar y administrar la información de los clientes.

### Productos
Permite administrar los productos que ofrece la empresa, incluyendo sus precios y características.

### Insumos
Permite registrar y administrar los insumos utilizados para la preparación de los pedidos.

### Pedidos
Permite registrar pedidos, seleccionar productos, indicar cantidades, fechas de eventos, horarios y direcciones de entrega.

### Trabajadores
Permite registrar trabajadores y administrar sus datos, cargos y funciones.

### Asignaciones
Permite al gerente asignar trabajadores a los pedidos según las funciones de preparador, asador y repartidor.

### Vehículos
Permite registrar y administrar los vehículos utilizados para la distribución de pedidos.

### Pagos
Permite registrar y consultar los pagos relacionados con los pedidos de los clientes.

### Reportes
Permite consultar información relacionada con los pedidos y las operaciones de la empresa.

### Mi Trabajo
Permite que los trabajadores ingresen al sistema y visualicen las actividades que tienen asignadas.

### Usuarios
Permite gestionar las cuentas de acceso al sistema y los roles correspondientes.

## Roles del sistema

El sistema cuenta con tres roles principales:

| Rol | Funciones |
|-----|-----------|
| GERENTE | Administración de pedidos, clientes, productos, insumos, trabajadores, vehículos y asignaciones |
| CONTADOR | Acceso a los módulos de pedidos, pagos y reportes |
| TRABAJADOR | Acceso al módulo Mi Trabajo para consultar sus actividades asignadas |

La interfaz muestra los módulos correspondientes a cada rol. El backend utiliza Spring Security para controlar los permisos de acceso a los servicios.

## Estructura del proyecto

El repositorio contiene dos aplicaciones principales: el frontend desarrollado con Angular y el backend desarrollado con Spring Boot.

### Backend

    pollogordo_api/
    |
    |-- .mvn/
    |-- images/
    |-- src/
    |   |-- main/
    |   |   |-- java/
    |   |   |   |-- pe.edu.upeu.pollogordo_api/
    |   |   |       |-- config/
    |   |   |       |-- controller/
    |   |   |       |-- dto/
    |   |   |       |-- entity/
    |   |   |       |-- exception/
    |   |   |       |-- repository/
    |   |   |       |-- security/
    |   |   |       |-- services/
    |   |   |       |-- PollogordoApiApplication.java
    |   |   |
    |   |   |-- resources/
    |   |       |-- static/
    |   |       |-- templates/
    |   |       |-- application.yaml
    |   |
    |   |-- test/
    |
    |-- uploads/
    |-- pom.xml

Las principales carpetas del backend son:

- `config`: configuración de seguridad y de la aplicación.
- `controller`: controladores de los servicios REST.
- `dto`: objetos utilizados para transferir información.
- `entity`: entidades relacionadas con las tablas de la base de datos.
- `exception`: manejo de excepciones.
- `repository`: acceso y consultas a la base de datos.
- `security`: componentes relacionados con la autenticación JWT.
- `services`: lógica de negocio del sistema.

### Frontend

    frontend_pollogordo/
    |
    |-- public/
    |-- src/
    |   |-- app/
    |   |   |-- core/
    |   |   |-- features/
    |   |   |-- interceptors/
    |   |   |-- shared/
    |   |   |-- app.component.css
    |   |   |-- app.component.html
    |   |   |-- app.component.ts
    |   |   |-- app.config.ts
    |   |   |-- app.routes.ts
    |   |
    |   |-- index.html
    |   |-- main.ts
    |   |-- styles.css
    |
    |-- angular.json
    |-- package.json

Las principales carpetas del frontend son:

- `core`: servicios y funcionalidades principales de la aplicación.
- `features`: módulos y funcionalidades del sistema.
- `interceptors`: manejo de peticiones HTTP y autenticación.
- `shared`: componentes reutilizables de la interfaz.
- `app.routes.ts`: configuración de las rutas de navegación.
- `app.config.ts`: configuración general de Angular.

## Requisitos para ejecutar el proyecto

Para ejecutar el sistema se necesita:

- JDK 21
- Node.js y npm
- Angular CLI
- Oracle Database 21c
- Maven
- Un editor de código o IDE compatible con Java y Angular

## Instalación y ejecución

### 1. Clonar el repositorio

Abrir una terminal y ejecutar:

    git clone https://github.com/babela12/PolloGordo.git

    cd PolloGordo

### 2. Configurar la base de datos

El sistema utiliza Oracle Database 21c.

La configuración de conexión se encuentra en:

    pollogordo_api/src/main/resources/application.yaml

Para ejecutar el proyecto se debe:

1. Tener Oracle Database en funcionamiento.
2. Contar con el esquema de base de datos correspondiente.
3. Configurar el usuario, contraseña y dirección de conexión en el backend.
4. Verificar que la aplicación pueda conectarse correctamente.

El esquema utilizado durante el desarrollo es `POLLOGORDO_BD`.

Por seguridad, no se incluyen credenciales de acceso a la base de datos en este documento.

### 3. Ejecutar el backend

Abrir la carpeta:

    pollogordo_api

Verificar la configuración de Oracle y ejecutar la clase principal de Spring Boot:

    PollogordoApiApplication

El backend utiliza el puerto 8083 y el contexto:

    /pollogordo_api

Para acceder a la documentación de los servicios REST mediante Swagger:

http://localhost:8083/pollogordo_api/swagger-ui/index.html

### 4. Ejecutar el frontend

Abrir otra terminal e ingresar a la carpeta:

    cd frontend_pollogordo

Instalar las dependencias:

    npm install

Ejecutar Angular en el puerto 4201:

    ng serve --port 4201

Ingresar desde el navegador a:

http://localhost:4201

Para utilizar las funcionalidades del sistema, el backend debe estar ejecutándose y la conexión con Oracle debe estar configurada correctamente.

Las direcciones indicadas corresponden al entorno de desarrollo local.

## Seguridad y autenticación

El sistema utiliza Spring Security y JWT para controlar el acceso de los usuarios.

El proceso de autenticación consiste en:

1. El usuario ingresa sus credenciales.
2. El backend valida la información.
3. Si las credenciales son correctas, se genera un token JWT.
4. Angular utiliza el token para realizar las peticiones protegidas.
5. El backend verifica los permisos según el rol del usuario.

Los roles implementados son GERENTE, CONTADOR y TRABAJADOR.

## Servicios REST

El backend cuenta con servicios para la gestión de los principales módulos del sistema.

Entre ellos se encuentran:

- Autenticación de usuarios.
- Gestión de clientes.
- Gestión de productos.
- Gestión de insumos.
- Gestión de trabajadores.
- Gestión de pedidos.
- Asignación de trabajadores.
- Gestión de vehículos.
- Gestión de pagos.
- Consulta de reportes.

Los servicios pueden revisarse y probarse mediante Swagger.

## Equipo

<table align="center">
  <tr>
    <td align="center" width="250">
      <a href="https://github.com/babela12">
        <img src="https://github.com/babela12.png" width="100" alt="Dannae Gabriela">
        <br>
        <b>Dannae Gabriela Ortiz Hushiñahua</b>
      </a>
      <br>
      @babela12
    </td>
    <td align="center" width="250">
      <a href="https://github.com/JhunelyC-chules">
        <img src="https://github.com/JhunelyC-chules.png" width="100" alt="Helooida Jhunely">
        <br>
        <b>Heloida Jhunely Castillo Chules</b>
      </a>
      <br>
      @JhunelyC-chules
    </td>
  </tr>
</table>

## Estado del proyecto

Proyecto integrador desarrollado durante el semestre académico 2026-2 de la Universidad Peruana Unión, campus Tarapoto, para la gestión de las operaciones de Multiservicios Pollo Gordo.
