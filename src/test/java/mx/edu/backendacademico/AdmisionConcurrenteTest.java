package mx.edu.backendacademico;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.exception.OperacionAcademicaException;
import mx.edu.backendacademico.repository.jpa.AlumnoEntity;
import mx.edu.backendacademico.repository.jpa.AlumnoSpringDataRepository;
import mx.edu.backendacademico.repository.jpa.MateriaEntity;
import mx.edu.backendacademico.repository.jpa.MateriaRepository;
import mx.edu.backendacademico.service.InscripcionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// Usa el PostgreSQL real de compose. Solo corre si RUN_PG_TESTS=true,
// así el build normal no depende de Docker.
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_PG_TESTS", matches = "true")
class AdmisionConcurrenteTest {

    @Autowired InscripcionService servicio;
    @Autowired MateriaRepository materias;
    @Autowired AlumnoSpringDataRepository alumnos;
    @Autowired JdbcTemplate jdbc;

    private Long materiaId;
    private final List<Long> alumnoIds = new ArrayList<>();

    @Test
    void dosAdmisionesSimultaneasParaUnSoloLugar() throws Exception {
        String sufijo = Long.toString(System.nanoTime(), 36).toUpperCase();

        // Materia con un solo lugar y dos alumnos activos distintos.
        materiaId = materias.save(new MateriaEntity("CONC" + sufijo, "Materia de un lugar", 1, 1)).getId();
        for (int i = 1; i <= 2; i++) {
            var alumno = new Alumno(null, "T" + i + sufijo, "Alumno " + i,
                    "t" + i + "@correo.com", EstatusAlumno.ACTIVO);
            alumnoIds.add(alumnos.save(new AlumnoEntity(alumno)).getId());
        }

        // Dos tareas que se preparan y arrancan al mismo tiempo.
        var listos = new CountDownLatch(2);
        var salida = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        List<Future<String>> futuros = new ArrayList<>();
        for (Long alumnoId : alumnoIds) {
            futuros.add(pool.submit(() -> {
                listos.countDown();
                salida.await();
                try {
                    servicio.inscribir(alumnoId, materiaId);
                    return "OK";
                } catch (OperacionAcademicaException e) {
                    return e.getCodigo();
                }
            }));
        }
        listos.await();
        salida.countDown();

        List<String> resultados = new ArrayList<>();
        for (var futuro : futuros) resultados.add(futuro.get(20, TimeUnit.SECONDS));
        pool.shutdown();

        assertEquals(1, resultados.stream().filter("OK"::equals).count(), resultados.toString());
        assertEquals(1, resultados.stream().filter("SIN_CUPO"::equals).count(), resultados.toString());
        Long filas = jdbc.queryForObject(
                "SELECT COUNT(*) FROM inscripciones WHERE materia_id = ?", Long.class, materiaId);
        assertEquals(1L, filas);
    }

    @AfterEach
    void limpiar() {
        // Deja la base como estaba: inscripciones, alumnos y materia de la prueba.
        if (materiaId != null) jdbc.update("DELETE FROM inscripciones WHERE materia_id = ?", materiaId);
        alumnoIds.forEach(alumnos::deleteById);
        if (materiaId != null) materias.deleteById(materiaId);
    }
}