package mx.edu.backendacademico.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Optional;
import mx.edu.backendacademico.config.SecurityConfig;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.domain.EstatusAlumno;
import mx.edu.backendacademico.service.AlumnoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExpedienteController.class)
@Import(SecurityConfig.class)
class ExpedienteControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean AlumnoService service;

    private static Alumno alumno(long id, String matricula) {
        return new Alumno(id, matricula, "Nombre " + matricula, matricula + "@u.mx", EstatusAlumno.ACTIVO);
    }

    @Test void propietarioVeSuExpediente() throws Exception {
        when(service.buscarPorMatricula("A001")).thenReturn(Optional.of(alumno(1L, "A001")));
        mvc.perform(get("/api/v1/expedientes/A001").with(httpBasic("A001", "a001pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value("A001"));
    }

    @Test void usuarioAjenoRecibe403SinTocarElServicio() throws Exception {
        mvc.perform(get("/api/v1/expedientes/A002").with(httpBasic("A001", "a001pass")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void administradorVeCualquierExpediente() throws Exception {
        when(service.buscarPorMatricula("A002")).thenReturn(Optional.of(alumno(2L, "A002")));
        mvc.perform(get("/api/v1/expedientes/A002").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value("A002"));
    }

    @Test void miExpedienteUsaLaIdentidadDelPrincipal() throws Exception {
        when(service.buscarPorMatricula("A001")).thenReturn(Optional.of(alumno(1L, "A001")));
        mvc.perform(get("/api/v1/mi-expediente").with(httpBasic("A001", "a001pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value("A001"));
        verify(service).buscarPorMatricula("A001");
    }

    @Test void sinCredencialesRecibe401() throws Exception {
        mvc.perform(get("/api/v1/mi-expediente")).andExpect(status().isUnauthorized());
    }
}