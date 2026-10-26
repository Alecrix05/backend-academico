package mx.edu.backendacademico.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.exception.MatriculaDuplicadaException;
import org.junit.jupiter.api.Test;

class AlumnoRepositoryConcurrenciaTest {

    @Test
    void dosHilosGuardanLaMismaMatriculaYSoloUnoLoLogra() throws Exception {
        var repositorio = new AlumnoRepositoryEnMemoria();
        var listos = new CountDownLatch(2);   // cuenta las tareas que ya están preparadas
        var salida = new CountDownLatch(1);   // señal para que ambas arranquen juntas
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> resultados = new ArrayList<>();
            for (int i = 1; i <= 2; i++) {
                final int numero = i;
                resultados.add(ejecutor.submit(() -> {
                    listos.countDown();
                    salida.await();
                    try {
                        repositorio.guardar(new Alumno(null, "A001", "Ada " + numero,
                                "ada" + numero + "@u.mx", EstatusAlumno.ACTIVO));
                        return true;
                    } catch (MatriculaDuplicadaException e) {
                        return false;
                    }
                }));
            }
            // Espera con tiempo límite hasta que las dos tareas estén listas.
            assertTrue(listos.await(5, TimeUnit.SECONDS), "Las tareas no quedaron listas");
            salida.countDown();

            int guardados = 0;
            int conflictos = 0;
            for (Future<Boolean> resultado : resultados) {
                if (resultado.get(5, TimeUnit.SECONDS)) guardados++; else conflictos++;
            }
            int registros = repositorio.buscarTodos().size();
            System.out.println("Guardados: " + guardados + ", conflictos: " + conflictos
                    + ", registros en el repositorio: " + registros);

            assertEquals(1, guardados);
            assertEquals(1, conflictos);
            assertEquals(1, registros);
        } finally {
            ejecutor.shutdownNow();
        }
    }
}