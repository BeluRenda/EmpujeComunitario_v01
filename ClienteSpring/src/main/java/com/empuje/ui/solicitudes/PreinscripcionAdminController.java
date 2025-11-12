package com.empuje.ui.solicitudes;

import com.empuje.ui.entity.Preinscripcion;
import com.empuje.ui.repo.PreinscripcionRepo;
import com.empuje.ui.service.PreinscripcionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequestMapping("/ui/preinscripciones")
public class PreinscripcionAdminController {

    private final PreinscripcionRepo repo;
    private final PreinscripcionService service;

    public PreinscripcionAdminController(PreinscripcionRepo repo, PreinscripcionService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        // Only PRESIDENTE can view
        if (session.getAttribute("rol") == null || !"PRESIDENTE".equals(session.getAttribute("rol").toString())) {
            return "redirect:/home";
        }

        List<Preinscripcion> items = StreamSupport.stream(repo.findAll().spliterator(), false)
                .collect(Collectors.toList());
        model.addAttribute("preinscripciones", items);
        return "preinscripciones/list";
    }

    @PostMapping("/{id}/denegar")
    public String denegar(@PathVariable Long id,
                         @RequestParam(required = false) String motivoRechazo,
                         RedirectAttributes ra,
                         HttpSession session) {
        if (session.getAttribute("rol") == null || !"PRESIDENTE".equals(session.getAttribute("rol").toString())) {
            ra.addFlashAttribute("error", "No autorizado");
            return "redirect:/home";
        }

        try {
            // Borro la preinscripción
            service.deleteById(id);
            ra.addFlashAttribute("msg", "Preinscripción denegada y removida.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo denegar: " + e.getMessage());
        }
        return "redirect:/ui/preinscripciones";
    }
}
