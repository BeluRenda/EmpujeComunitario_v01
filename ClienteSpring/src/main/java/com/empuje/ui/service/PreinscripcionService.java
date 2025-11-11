package com.empuje.ui.service;

import com.empuje.ui.entity.Preinscripcion;
import com.empuje.ui.repo.PreinscripcionRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class PreinscripcionService {

    private final PreinscripcionRepo repo;

    public PreinscripcionService(PreinscripcionRepo repo) {
        this.repo = repo;
    }

    @Transactional
    public Preinscripcion create(String nombre, String apellido, LocalDate fechaNacimiento, String telefono, String email, String motivo) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("nombre requerido");
        if (fechaNacimiento == null)
            throw new IllegalArgumentException("fechaNacimiento requerida");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("email requerido");

        //Validacion: unicidad email
        Optional<Preinscripcion> exists = repo.findByEmail(email.trim().toLowerCase());
        if (exists.isPresent())
            throw new IllegalArgumentException("Ya existe una preinscripción con ese mail");

        Preinscripcion p = new Preinscripcion();
        p.setNombre(nombre.trim());
        p.setApellido(apellido == null || apellido.isBlank() ? null : apellido.trim());
        p.setFechaNacimiento(fechaNacimiento);
        p.setTelefono(telefono == null || telefono.isBlank() ? null : telefono.trim());
        p.setEmail(email.trim().toLowerCase());
        p.setMotivo(motivo == null || motivo.isBlank() ? null : motivo.trim());
        //Por defecto la preinscripcion queda VIGENTE
        p.setEstado("VIGENTE");

        repo.save(p);
        return p;
    }

    @Transactional
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public java.util.List<Preinscripcion> listByEstado(String estado) {
        if (estado == null) estado = "VIGENTE"; //muestro los vigentes
        return repo.findByEstado(estado);
    }

    @Transactional
    public Preinscripcion deny(Long id, String motivoRechazo) {
        var opt = repo.findById(id);
        if (opt.isEmpty()) throw new IllegalArgumentException("Preinscripción no encontrada: " + id);
        Preinscripcion p = opt.get();
        p.setEstado("DENEGADA");
        p.setMotivoRechazo(motivoRechazo == null ? null : motivoRechazo.trim());
        repo.save(p);
        return p;
    }
}
