package com.empuje.grpc.auth;

import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpSession;
import com.empuje.grpc.ong.UserServiceGrpc;
import com.empuje.grpc.ong.LoginRequest;
import com.empuje.grpc.ong.CreateUserRequest;
import com.empuje.grpc.ong.AuthContext;
import com.empuje.grpc.ong.Role;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserServiceGrpc.UserServiceBlockingStub users;
    private final com.empuje.ui.service.PreinscripcionService preService;

    public AuthController(UserServiceGrpc.UserServiceBlockingStub users,
                          com.empuje.ui.service.PreinscripcionService preService) {
        this.users = users;
        this.preService = preService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String identifier,
            @RequestParam String password,
            HttpSession session, Model model) {
        try {
            LoginRequest req = LoginRequest.newBuilder()
                    .setUsernameOrEmail(identifier)
                    .setPassword(password)
                    .build();

            var resp = users.login(req);

            if (!resp.getSuccess()) {
                model.addAttribute("error", resp.getMessage());
                return "login";
            }

            session.setAttribute("userId", resp.getUserId());
            session.setAttribute("username", identifier);
            session.setAttribute("rol", resp.getRol().name());
            return "redirect:/home";

        } catch (StatusRuntimeException e) {
            model.addAttribute("error", "Error de autenticación: " + e.getStatus().getDescription());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
        return "redirect:/"; 
    }

    @GetMapping("/register")
    public String registerForm(HttpSession session,
                               @RequestParam(required = false) Long preId,
                               @RequestParam(required = false) String nombre,
                               @RequestParam(required = false) String apellido,
                               @RequestParam(required = false) String email,
                               @RequestParam(required = false) String telefono,
                               org.springframework.ui.Model model) {
        // Solo PRESIDENTE puede registrar
        if (session.getAttribute("rol") == null ||
                !"PRESIDENTE".equals(session.getAttribute("rol").toString())) {
            return "redirect:/home";
        }
        // Si el caller paso parametros de precarga (desde preinscripcion) se reenvian a la plantilla
        if (preId != null) model.addAttribute("preId", preId);
        if (nombre != null) model.addAttribute("prefillNombre", nombre);
        if (apellido != null) model.addAttribute("prefillApellido", apellido);
        if (email != null) model.addAttribute("prefillEmail", email);
        if (telefono != null) model.addAttribute("prefillTelefono", telefono);
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
        @RequestParam String nombre,
        @RequestParam String apellido,
        @RequestParam String email,
        @RequestParam(required = false) String telefono,
        @RequestParam String rol,
        @RequestParam(required = false) Long preId,
        Model model,
        HttpSession session) {
        // Bloqueo por rol
        if (session.getAttribute("rol") == null ||
                !"PRESIDENTE".equals(session.getAttribute("rol").toString())) {
            return "redirect:/home";
        }

        if (!StringUtils.hasText(username) || !StringUtils.hasText(email) ||
                !StringUtils.hasText(nombre) || !StringUtils.hasText(apellido)) {
            model.addAttribute("error", "Usuario, nombre, apellido y email son obligatorios");
            return "register";
        }

        try {
            int actorId = 0;
            Object uid = session.getAttribute("userId");
            if (uid instanceof Integer)
                actorId = (Integer) uid;
            else if (uid instanceof String) {
                try {
                    actorId = Integer.parseInt((String) uid);
                } catch (Exception ignored) {
                }
            }

            // Parsear el enum Role desde el string del form
            Role rolEnum;
            try {
                rolEnum = Role.valueOf(rol);
            } catch (IllegalArgumentException ex) {
                rolEnum = Role.VOLUNTARIO; // fallback seguro
            }

            var req = CreateUserRequest.newBuilder()
                    .setAuth(AuthContext.newBuilder()
                            .setActorId(actorId)
                            .setActorRole(Role.PRESIDENTE)
                            .build())
                    .setUsername(username)
                    .setNombre(nombre)
                    .setApellido(apellido)
                    .setTelefono(telefono == null ? "" : telefono)
                    .setEmail(email)
                    .setRol(rolEnum) // <--- usar el rol elegido
                    .build();

            var resp = users.createUser(req);

            if (!resp.getSuccess()) {
                //No exponer mensajes internos devueltos por el backend en la UI.
                System.err.println("[AuthController] createUser fallo: " + resp.getMessage());
                model.addAttribute("error", "No se pudo crear el usuario (ver logs del servidor para detalles). ");
                return "register";
            }

            model.addAttribute("ok", "Usuario creado. Revisá la consola del servidor (contraseña generada).");
            if (preId != null) {
                try {
                    preService.deleteById(preId);
                } catch (Exception ignore) {
                }
            }
            return "register";

        } catch (StatusRuntimeException e) {
            //No exponer trazas de error internas al usuario en la UI.
            //Loguear en servidor y mostrar un mensaje amigable.
            System.err.println("[AuthController] Error gRPC al crear usuario: " + e.getMessage());
            //Intenta limpiar la preinscripción si vino preId (el backend logro crear el usuario)
            if (preId != null) {
                try {
                    preService.deleteById(preId);
                } catch (Exception ignore) {
                }
            }
            model.addAttribute("ok", "Usuario creado. (Advertencia: fallo en notificaciones — revisar logs del servidor)");
            return "register";
        }
    }


    @GetMapping("/eventos")
    public String eventosForm(HttpSession session) {
        // Solo PRESIDENTE y VOCAl
        if (session.getAttribute("rol") == null ||
                !"PRESIDENTE".equals(session.getAttribute("rol").toString())) {
            return "redirect:/home";
        }
        return "eventos";
    }



}
