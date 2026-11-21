package mx.edu.backendacademico.controller;

import java.util.Optional;
import mx.edu.backendacademico.domain.Alumno;
import mx.edu.backendacademico.service.AlumnoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExpedienteController {
    private final AlumnoService service;

    public ExpedienteController(AlumnoService service) {
        this.service = service;
    }

    // La identidad sale del principal autenticado, nunca de un parámetro del cliente.
    @GetMapping("/api/v1/mi-expediente")
    public ResponseEntity<Alumno> miExpediente(Authentication auth) {
        return respuesta(service.buscarPorMatricula(auth.getName()));
    }

    // Regla por objeto: el propietario o un ADMIN.
    @GetMapping("/api/v1/expedientes/{matricula}")
    public ResponseEntity<Alumno> expediente(@PathVariable String matricula, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean propietario = auth.getName().equals(matricula);
        if (!admin && !propietario) {
            return ResponseEntity.status(403).build();
        }
        return respuesta(service.buscarPorMatricula(matricula));
    }

    private ResponseEntity<Alumno> respuesta(Optional<Alumno> alumno) {
        return alumno.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}