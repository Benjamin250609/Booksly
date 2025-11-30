# Booksly

Booksly es una aplicación de Android que te permite llevar un registro de tus libros, creando una estantería digital personalizada.

## Integrantes

*   Benjamin Andres Perez Hinojosa
*   Constanza ignacia jerez sepulveda

## Funcionalidades

*   Autenticación de usuarios: Regístrate e inicia sesión en la aplicación.
*   Busca libros: Busca libros utilizando la API de Google Books.
*   Agrega libros a tu estantería: Guarda los libros que has leído o estás leyendo
*   Seguimiento del progreso de lectura: Lleva un registro de tu progreso en los libros que estás leyendo actualmente.
*   Ver detalles del libro: Consulta información detallada sobre cada libro.
*   Perfil de usuario: Visualiza tu información de perfil.

## Endpoints Utilizados

### Endpoints Propios

La aplicación se comunica con un servidor backend propio para la autenticación de usuarios.

*   URL Base: `http://100.30.140.229:8081/`
*   Endpoints:
    *   `POST /auth/register`: Registrar un nuevo usuario.
    *   `POST /auth/login`: Iniciar sesión.

### Endpoints Externos

La aplicación utiliza la API de Google Books para buscar información sobre libros.

*   URL Base: `https://www.googleapis.com/books/v1/`
*   Endpoint:
    *   `GET /volumes`: Busca libros por título.

## Instrucciones para Ejecutar el Proyecto

Compilar la aplicacion y darle a ejecutar, el microservicio de autenticacion esta corriendo en aws, en un ec2,
por lo tanto tal vez tiene problema al tratar de iniciar sesion o registrarse, de ser asi, debo iniciar el laboratorio de mi cuenta de aws.

## APK Firmado y Ubicación del Archivo .jks

Se envia el apk firmado y el .jks por la entrega


