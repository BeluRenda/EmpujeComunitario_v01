package com.empuje.ui.repo;

import com.empuje.ui.entity.Preinscripcion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PreinscripcionRepo extends CrudRepository<Preinscripcion, Long> {
    Optional<Preinscripcion> findByEmail(String email);
    List<Preinscripcion> findByEstado(String estado);
}
