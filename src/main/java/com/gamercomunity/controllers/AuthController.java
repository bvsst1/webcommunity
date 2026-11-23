package com.gamercomunity.controllers;

import com.gamercomunity.models.Usuario;
import com.gamercomunity.service.UsuarioService;
import java.nio.charset.StandardCharsets;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam(defaultValue = "") String nombreCompleto,
                            @RequestParam(defaultValue = "") String username,
                            @RequestParam(defaultValue = "") String email,
                            @RequestParam(defaultValue = "") String password,
                            Model model) {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(nombreCompleto.strip());
        usuario.setUsername(username);
        usuario.setEmail(email.strip());
        model.addAttribute("usuario", usuario);

        try {
            if (usuario.getNombreCompleto().isEmpty() || usuario.getNombreCompleto().length() > 120) {
                throw new IllegalArgumentException("Ingresa tu nombre completo, con un máximo de 120 caracteres");
            }
            if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
                throw new IllegalArgumentException(
                        "El nombre de usuario solo puede tener letras, números y guion bajo (3 a 20 caracteres)");
            }
            if (usuario.getEmail().length() > 150 || !usuario.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
                throw new IllegalArgumentException("Ingresa un correo electrónico válido");
            }
            if (password.isBlank() || password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalArgumentException("Ingresa una contraseña de hasta 72 bytes");
            }
            usuario.setPassword(password);
            usuarioService.registrar(usuario);
            return "redirect:/login?registrado";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "El nombre de usuario o el correo ya está registrado");
        }
        usuario.setPassword(null);
        return "registro";
    }
}
