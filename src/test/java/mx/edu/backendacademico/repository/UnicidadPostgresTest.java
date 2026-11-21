package mx.edu.backendacademico.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.exception.MatriculaDuplicadaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class UnicidadPostgresTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // Es el adaptador JPA real (el perfil jpa es el predeterminado).
    @Autowired AlumnoRepository repositorio;
    @Autowired JdbcTemplate jdbc;

    @Test void postgresRechazaUnaMatriculaRepetida() {
        repositorio.guardar(new Alumno(null, "U1", "Ada", "ada@u.mx", EstatusAlumno.ACTIVO));

        // Se llama directo al repositorio, sin la comprobación previa del servicio:
        // quien debe rechazar el duplicado es la restricción UNIQUE de PostgreSQL.
        assertThrows(MatriculaDuplicadaException.class, () ->
                repositorio.guardar(new Alumno(null, "U1", "Otra", "otra@u.mx", EstatusAlumno.ACTIVO)));

        Integer filas = jdbc.queryForObject("SELECT COUNT(*) FROM alumnos WHERE matricula = 'U1'", Integer.class);
        assertEquals(1, filas);
    }
}