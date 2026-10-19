# Reto U01: ejecutar en el puerto 8081

## Cómo lo hice
Definí la variable de entorno `SERVER_PORT=8081` en la terminal y ejecuté
`.\mvnw.cmd spring-boot:run`. No modifiqué la clase principal ni
`application.properties`. El log mostró que Tomcat inició en el puerto 8081.

## Por qué no hace falta recompilar
El puerto es un dato de configuración, no lógica del programa. Spring Boot lo
lee al arrancar, y una variable de entorno tiene prioridad sobre el valor
`server.port=8080` de `application.properties`. El bytecode compilado es el
mismo en ambos casos; solo cambia el valor que se lee al iniciar. Por eso un
mismo JAR puede ejecutarse en distintos puertos o ambientes sin recompilar.

## Evidencia
Las versiones de Java y Maven y el resultado de las pruebas están en
`evidencia-u01.txt`.