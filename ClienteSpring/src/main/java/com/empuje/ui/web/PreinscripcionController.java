package com.empuje.ui.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PreinscripcionController {

    @GetMapping("/preinscripcion")
    public String preinscripcionPage(Model model) {
        return "preinscripcion";
    }
}
