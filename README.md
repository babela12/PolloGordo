
# Sistema de Gestión de Pedidos - Multiservicios Pollo Gordo

## Descripción del proyecto

El presente proyecto consiste en el desarrollo de un sistema web para la empresa Multiservicios Pollo Gordo, dedicada a la preparación y distribución de alimentos para eventos sociales, deportivos y privados.

El sistema permite gestionar los pedidos, clientes, trabajadores, productos, insumos y otras actividades de la empresa, con la finalidad de mejorar la organización de sus procesos y reducir los errores en el registro de información.

## Datos del proyecto

- Universidad: Universidad Peruana Unión
- Campus: Tarapoto
- Semestre académico: 2026-2
- Línea: Software
- Tipo: Proyecto Integrador (PI)
- Cursos: Lenguaje de Programación II, Análisis y Diseño de Sistemas de Información y Base de Datos

## Tecnologías utilizadas

### Backend
- Java 21
- Spring Boot 3.4.1
- Spring Security
- JWT
- Maven

### Frontend
- Angular
- TypeScript
- HTML
- CSS
- Bootstrap

### Base de datos
- Oracle Database 21c

### Control de versiones
- Git
- GitHub

## Funcionalidades del sistema

### Gestión de clientes
Permite registrar, consultar, actualizar y administrar la información de los clientes de la empresa.

### Gestión de trabajadores
Permite registrar trabajadores y administrar sus datos, cargos y funciones dentro de la empresa.

### Gestión de productos
Permite registrar y administrar los productos que ofrece la empresa, incluyendo sus precios y características.

### Gestión de insumos
Permite administrar los insumos utilizados para la preparación de los pedidos.

### Gestión de pedidos
Permite registrar pedidos de los clientes, seleccionar productos, indicar cantidades, fechas de eventos y direcciones de entrega.

### Asignación de trabajadores
Permite al gerente asignar trabajadores a los pedidos según las funciones de preparador, asador y repartidor.

### Gestión de vehículos
Permite registrar y administrar los vehículos utilizados para la distribución de los pedidos.

### Gestión de pagos
Permite registrar y consultar los pagos relacionados con los pedidos de los clientes.

### Reportes
Permite consultar información de los pedidos y operaciones de la empresa.

### Módulo Mi Trabajo
Permite que los trabajadores ingresen al sistema y visualicen las actividades que tienen asignadas.

### Seguridad y autenticación
Permite el inicio de sesión y el acceso a los módulos del sistema según el rol del usuario.

## Arquitectura del sistema

El proyecto utiliza una arquitectura cliente-servidor.

- Frontend: desarrollado con Angular.
- Backend: API REST desarrollada con Spring Boot.
- Base de datos: Oracle Database.
- Seguridad: Spring Security y JWT.

El frontend realiza peticiones HTTP al backend, donde se procesan las operaciones y se establece la comunicación con la base de datos.

## Estructura del proyecto

El repositorio contiene las siguientes carpetas:

    PolloGordo/
    |
    |-- frontend_pollogordo/
    |   |-- src/
    |   |-- angular.json
    |   |-- package.json
    |
    |-- pollogordo_api/
    |   |-- src/
    |   |-- pom.xml
    |
    |-- .gitignore
    |-- README.md

- `frontend_pollogordo`: contiene la interfaz web desarrollada con Angular.
- `pollogordo_api`: contiene la API REST desarrollada con Spring Boot.

## Ejecución del proyecto

### Requisitos

Para ejecutar el sistema se necesita:

- JDK 21
- Node.js y npm
- Angular CLI
- Oracle Database 21c
- Maven

### Backend

1. Abrir el proyecto `pollogordo_api`.
2. Verificar la configuración de conexión con Oracle Database.
3. Ejecutar la aplicación Spring Boot.

El backend se ejecuta en el puerto 8083.

Para acceder a la documentación de los servicios REST mediante Swagger:

http://localhost:8083/pollogordo_api/swagger-ui/index.html

### Frontend

1. Abrir la carpeta `frontend_pollogordo`.
2. Instalar las dependencias del proyecto:

    npm install

3. Ejecutar Angular en el puerto 4201:

    ng serve --port 4201

4. Ingresar desde el navegador a:

http://localhost:4201

Para el funcionamiento completo del sistema, el backend debe estar ejecutándose y la conexión con Oracle debe estar configurada correctamente.

## Base de datos

El sistema utiliza Oracle Database 21c para almacenar la información de clientes, trabajadores, productos, insumos, pedidos, asignaciones, vehículos y pagos.

La comunicación con la base de datos se realiza desde el backend desarrollado con Spring Boot.

## Seguridad

El sistema utiliza Spring Security y JWT para la autenticación de usuarios y el control de acceso según sus roles.

Se implementaron tres roles:

- GERENTE: administra los pedidos, clientes, trabajadores, productos, insumos, vehículos y asignaciones.
- CONTADOR: tiene acceso a los módulos de pedidos, pagos y reportes.
- TRABAJADOR: accede al módulo Mi Trabajo, donde puede visualizar las actividades que tiene asignadas.

La interfaz muestra las opciones correspondientes al rol de cada usuario.

## Estado del proyecto

Proyecto integrador desarrollado durante el semestre académico 2026-2 de la Universidad Peruana Unión, campus Tarapoto.
