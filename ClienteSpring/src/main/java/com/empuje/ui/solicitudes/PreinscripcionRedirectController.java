package com.empuje.ui.solicitudes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller de redirección para reenviar /preinscripciones a la ruta administrativa
 * bajo /ui. Mantiene un mapeo pequeño y explícito para que los usuarios que
 * accedan a /preinscripciones desde marcadores o enlaces externos sean redirigidos
 */
@Controller
@RequestMapping("/preinscripciones")
public class PreinscripcionRedirectController {

    @GetMapping
    public String redirectToUi() {
        // Se hace redirect a la ruta administrativa bajo /ui
        return "redirect:/ui/preinscripciones";
    }

}
