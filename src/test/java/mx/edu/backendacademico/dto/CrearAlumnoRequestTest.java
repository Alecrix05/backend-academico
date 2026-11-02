package mx.edu.backendacademico.dto;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import org.junit.jupiter.api.Test;

class CrearAlumnoRequestTest {
    private final Validator validador = Validation.buildDefaultValidatorFactory().getValidator();

    private boolean matriculaTieneErrores(String matricula) {
        var request = new CrearAlumnoRequest(matricula, "Ada Lovelace", "ada@universidad.mx");
        return !validador.validateProperty(request, "matricula").isEmpty();
    }

    @Test
    void rechazaEspaciosInternos() {
        assertTrue(matriculaTieneErrores("A 001"));
        assertTrue(matriculaTieneErrores("A\t001"));
    }

    @Test
    void aceptaEspaciosSoloEnLosExtremos() {
        assertFalse(matriculaTieneErrores("A001"));
        assertFalse(matriculaTieneErrores("  a001  "));
    }

    @Test
    void conservaLosLimitesExistentes() {
        assertTrue(matriculaTieneErrores(""));
        assertTrue(matriculaTieneErrores("   "));
        assertTrue(matriculaTieneErrores("A".repeat(31)));
    }

    @Test
    void elDominioGuardaLaMatriculaCanonica() {
        // La frontera deja pasar "  a001  "; el dominio la guarda recortada y en mayúsculas.
        var alumno = new Alumno(null, "  a001  ", "Ada Lovelace", "ada@universidad.mx", EstatusAlumno.ACTIVO);
        assertEquals("A001", alumno.matricula());
    }
}