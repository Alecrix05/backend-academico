package mx.edu.backendacademico.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.service.AlumnoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AlumnoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlumnoControllerCreacionTest {
    @Autowired MockMvc mvc;
    @MockitoBean AlumnoService service;

    @Test void crearResponde201ConLocationDelRecursoNuevo() throws Exception {
        // El servicio simulado devuelve un alumno con id 5.
        when(service.crear(anyString(), anyString(), anyString()))
                .thenReturn(new Alumno(5L, "A1", "Ada", "ada@u.mx", EstatusAlumno.ACTIVO));

        mvc.perform(post("/api/v1/alumnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"matricula\":\"A1\",\"nombre\":\"Ada\",\"correo\":\"ada@u.mx\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/alumnos/5"))
                .andExpect(jsonPath("$.id").value(5));
    }
}