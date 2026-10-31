package mx.edu.backendacademico.controller;

import static org.junit.jupiter.api.Assertions.*;

import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.repository.AlumnoRepositoryEnMemoria;
import mx.edu.backendacademico.service.AlumnoService;
import org.junit.jupiter.api.Test;

class AlumnoControllerFiltroTest {

    @Test
    void filtraPorEstatusSinModificarElRepositorio() {
        var repositorio = new AlumnoRepositoryEnMemoria();
        repositorio.guardar(new Alumno(null, "A001", "Ada", "ada@u.mx", EstatusAlumno.ACTIVO));
        repositorio.guardar(new Alumno(null, "A002", "Alan", "alan@u.mx", EstatusAlumno.BAJA));
        var controlador = new AlumnoController(new AlumnoService(repositorio));
        var antes = repositorio.buscarTodos();

        // Sin filtro: el contrato original devuelve todos.
        assertEquals(2, controlador.listar(null).size());

        // Con filtro: solo los alumnos del estatus pedido.
        assertEquals(1, controlador.listar(EstatusAlumno.ACTIVO).size());
        assertEquals("A001", controlador.listar(EstatusAlumno.ACTIVO).get(0).matricula());
        assertEquals(1, controlador.listar(EstatusAlumno.BAJA).size());
        assertEquals("A002", controlador.listar(EstatusAlumno.BAJA).get(0).matricula());

        // Consultar no cambia el almacén: mismos valores y mismo orden.
        assertEquals(antes, repositorio.buscarTodos());
    }
}