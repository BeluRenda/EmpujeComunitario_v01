package com.empuje.graphql;

import com.empuje.ui.service.PreinscripcionService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class PreinscripcionResolver {

    private final PreinscripcionService service;
    private final Logger logger = LoggerFactory.getLogger(PreinscripcionResolver.class);

    public PreinscripcionResolver(PreinscripcionService service) {
        this.service = service;
    }

    @MutationMapping
    public Preinscripcion createPreinscripcion(@Argument("input") PreinscripcionInput input) {
        // Fecha en formato DD-MM-YYYY (como 01-11-1990)
        LocalDate fecha;
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            fecha = LocalDate.parse(input.getFechaNacimiento(), f);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("fechaNacimiento inválida. Usar formato DD-MM-YYYY (como 01-11-1990)");
        }
    com.empuje.ui.entity.Preinscripcion saved = service.create(
                input.getNombre(),
                input.getApellido(),
                fecha,
                input.getTelefono(),
                input.getEmail(),
                input.getMotivo());

        Preinscripcion dto = new Preinscripcion();
        dto.setId(saved.getId());
        dto.setNombre(saved.getNombre());
        dto.setApellido(saved.getApellido());
        dto.setFechaNacimiento(saved.getFechaNacimiento().toString());
        dto.setTelefono(saved.getTelefono());
        dto.setEmail(saved.getEmail());
        dto.setMotivo(saved.getMotivo());
        dto.setCreatedAt(saved.getCreatedAt() == null ? null : saved.getCreatedAt().toString());
        dto.setEstado(saved.getEstado());
        dto.setMotivoRechazo(saved.getMotivoRechazo());
        return dto;
    }

    @QueryMapping
    public List<Preinscripcion> preinscripciones(@Argument("estado") String estado) {
        List<com.empuje.ui.entity.Preinscripcion> list = service.listByEstado(estado);
        return list.stream().map(saved -> {
            Preinscripcion dto = new Preinscripcion();
            dto.setId(saved.getId());
            dto.setNombre(saved.getNombre());
            dto.setApellido(saved.getApellido());
            dto.setFechaNacimiento(saved.getFechaNacimiento() == null ? null : saved.getFechaNacimiento().toString());
            dto.setTelefono(saved.getTelefono());
            dto.setEmail(saved.getEmail());
            dto.setMotivo(saved.getMotivo());
            dto.setCreatedAt(saved.getCreatedAt() == null ? null : saved.getCreatedAt().toString());
            dto.setEstado(saved.getEstado());
            dto.setMotivoRechazo(saved.getMotivoRechazo());
            return dto;
        }).collect(Collectors.toList());
    }

    @QueryMapping
    public Preinscripcion preinscripcion(@Argument("id") Long id) {
        var opt = service.listByEstado(null).stream().filter(p -> p.getId().equals(id)).findFirst();
        if (opt.isEmpty()) return null;
        var saved = opt.get();
        Preinscripcion dto = new Preinscripcion();
        dto.setId(saved.getId());
        dto.setNombre(saved.getNombre());
        dto.setApellido(saved.getApellido());
        dto.setFechaNacimiento(saved.getFechaNacimiento() == null ? null : saved.getFechaNacimiento().toString());
        dto.setTelefono(saved.getTelefono());
        dto.setEmail(saved.getEmail());
        dto.setMotivo(saved.getMotivo());
        dto.setCreatedAt(saved.getCreatedAt() == null ? null : saved.getCreatedAt().toString());
        dto.setEstado(saved.getEstado());
        dto.setMotivoRechazo(saved.getMotivoRechazo());
        return dto;
    }

    @MutationMapping
    public Preinscripcion denegarPreinscripcion(@Argument("id") Long id, @Argument("motivoRechazo") String motivoRechazo) {
        // Server-side role check: only PRESIDENTE may denegar
        Object rolAttr = RequestContextHolder.currentRequestAttributes()
                .getAttribute("rol", RequestAttributes.SCOPE_SESSION);
        if (rolAttr == null || !"PRESIDENTE".equals(rolAttr.toString())) {
            throw new RuntimeException("Acceso denegado: sólo PRESIDENTE puede denegar preinscripciones.");
        }

        com.empuje.ui.entity.Preinscripcion updated;
        try {
            updated = service.deny(id, motivoRechazo);
        } catch (Exception e) {
            // Log full stacktrace for server-side debugging and rethrow a controlled exception
            logger.error("Error al denegar preinscripción id={}", id, e);
            throw new IllegalArgumentException("No se pudo denegar la preinscripción: " + e.getMessage());
        }
        Preinscripcion dto = new Preinscripcion();
        dto.setId(updated.getId());
        dto.setNombre(updated.getNombre());
        dto.setApellido(updated.getApellido());
        dto.setFechaNacimiento(updated.getFechaNacimiento() == null ? null : updated.getFechaNacimiento().toString());
        dto.setTelefono(updated.getTelefono());
        dto.setEmail(updated.getEmail());
        dto.setMotivo(updated.getMotivo());
        dto.setCreatedAt(updated.getCreatedAt() == null ? null : updated.getCreatedAt().toString());
        dto.setEstado(updated.getEstado());
        dto.setMotivoRechazo(updated.getMotivoRechazo());
        return dto;
    }
}
