package com.gamercomunity.web;

import com.gamercomunity.controllers.ApiAuthController.LoginRequest;
import com.gamercomunity.models.Evento;
import com.gamercomunity.repository.EventoRepository;
import com.gamercomunity.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:webcommunity;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "APP_JWT_SECRET=dGVzdC1vbmx5LXNpZ25pbmcta2V5LWZvci1qd3QtMDE=",
        "app.demo.enabled=true",
        "DEMO_ADMIN_PASSWORD=test-password-1",
        "DEMO_USUARIO_PASSWORD=test-password-1",
        "DEMO_USUARIO2_PASSWORD=test-password-1"
})
@AutoConfigureMockMvc
class WebApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private EventoRepository eventos;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void inicioEsPublico() throws Exception {
        mvc.perform(get("/gamer")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Bienvenido a Gamer Community")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "usuario", "usuario2"})
    void usuariosPuedenObtenerTokenYAccederApiPrivada(String username) throws Exception {
        mvc.perform(get("/api/perfil").header(HttpHeaders.AUTHORIZATION, token(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    private String token(String username) throws Exception {
        String response = mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new LoginRequest(username, "test-password-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + jsonMapper.readTree(response).get("token").asString();
    }

    @Test
    void rutaPrivadaRechazaAccesoSinTokenOConTokenInvalido() throws Exception {
        mvc.perform(get("/api/perfil")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/perfil").header(HttpHeaders.AUTHORIZATION, "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unaSesionWebNoAutorizaEscriturasEnLaApi() throws Exception {
        long id = nuevoEvento();
        String ruta = "/api/eventos/" + id + "/compartir";
        var login = mvc.perform(post("/login").with(csrf())
                        .param("username", "usuario").param("password", "test-password-1"))
                .andExpect(redirectedUrl("/home")).andReturn();
        var sesion = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(post(ruta).session(sesion)).andExpect(status().isForbidden());
        mvc.perform(post(ruta).session(sesion).with(csrf())).andExpect(status().isUnauthorized());
        mvc.perform(get("/eventos/compartidos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void loginRechazaCredencialesIncorrectas() throws Exception {
        mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"usuario","password":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroGuardaNombreCompletoYProtegeLaContrasena() throws Exception {
        mvc.perform(get("/registro")).andExpect(status().isOk());
        mvc.perform(post("/registro")
                        .with(csrf())
                        .param("nombreCompleto", "Andrea García")
                        .param("username", "andrea")
                        .param("email", "andrea@example.com")
                        .param("password", "test-password-2")
                        .param("id", "1"))
                .andExpect(redirectedUrl("/login?registrado"));

        var usuario = usuarios.findByUsername("andrea").orElseThrow();
        assertEquals("Andrea García", usuario.getNombreCompleto());
        assertTrue(passwordEncoder.matches("test-password-2", usuario.getPassword()));
        assertTrue(usuarios.existsByUsername("admin"));
    }

    @Test
    void comentariosValoracionYEnlaceCompartido() throws Exception {
        long id = nuevoEvento();
        String evento = "/eventos/" + id;
        String api = "/api" + evento;
        String enlace = "/eventos/compartidos/" + id;
        String usuarioToken = token("usuario");

        mvc.perform(get(enlace)).andExpect(status().isNotFound());
        mvc.perform(post(api + "/comentarios")
                        .header(HttpHeaders.AUTHORIZATION, usuarioToken)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"texto":"Torneo de <equipo>"}
                                """))
                .andExpect(status().isNoContent());
        mvc.perform(get(evento).with(user("usuario")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Torneo de &lt;equipo&gt;")));

        for (int puntaje : new int[] {3, 5}) {
            mvc.perform(post(api + "/valoracion")
                            .header(HttpHeaders.AUTHORIZATION, usuarioToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"puntaje\":" + puntaje + "}"))
                    .andExpect(status().isNoContent());
        }
        mvc.perform(get(api).header(HttpHeaders.AUTHORIZATION, usuarioToken))
                .andExpect(jsonPath("$.valoracion").value(5.0));
        mvc.perform(post(api + "/compartir").header(HttpHeaders.AUTHORIZATION, usuarioToken))
                .andExpect(jsonPath("$.ruta").value(enlace));
        mvc.perform(get(enlace)).andExpect(status().isOk());
        mvc.perform(get("/eventos/compartidos")).andExpect(status().isOk())
                .andExpect(content().string(containsString(enlace)));
    }

    @Test
    void administradorAgregaMultimediaYUsuarioPuedeVerla() throws Exception {
        long id = nuevoEvento();
        String ruta = "/eventos/" + id + "/multimedia";
        byte[] png = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aPhkAAAAASUVORK5CYII=");
        var foto = new MockMultipartFile("archivo", "torneo.png", "image/png", png);
        String adminToken = token("admin");
        mvc.perform(multipart("/api" + ruta).file(foto).header(HttpHeaders.AUTHORIZATION, token("usuario")))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/api" + ruta).file(foto).header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isNoContent());
        var archivoInvalido = new MockMultipartFile("archivo", "falso.png", "image/png", new byte[] {1, 2, 3});
        mvc.perform(multipart("/api" + ruta).file(archivoInvalido).header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/eventos/" + id).with(user("usuario")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(ruta + "/")));
    }

    @Test
    void soloAdministradorPublicaYElFormularioConservaElEstadoDeCompartir() throws Exception {
        String ruta = "/admin/evento/guardar";
        mvc.perform(post(ruta).with(user("usuario")).with(csrf()).param("nombre", "Encuentro"))
                .andExpect(status().isForbidden());
        mvc.perform(post(ruta).with(user("admin").roles("ADMIN")).with(csrf())
                        .param("nombre", "Encuentro de comunidad")
                        .param("fechaHora", "2026-12-01T18:00")
                        .param("tieneComidas", "true")
                        .param("compartido", "true"))
                .andExpect(redirectedUrl("/admin"));
        var evento = eventos.findAll().stream()
                .filter(e -> "Encuentro de comunidad".equals(e.getNombre())).findFirst().orElseThrow();
        assertEquals(LocalDateTime.of(2026, 12, 1, 18, 0), evento.getFechaHora());
        assertTrue(evento.isTieneComidas());
        mvc.perform(get("/eventos/compartidos/" + evento.getId())).andExpect(status().isNotFound());
    }

    @Test
    void formularioDePerfilModificaSoloPreferencias() throws Exception {
        mvc.perform(post("/perfil").with(user("usuario2")).with(csrf())
                        .param("email", "otro@example.com")
                        .param("juegosInteres", "StarCraft")
                        .param("notificarEmail", "true"))
                .andExpect(redirectedUrl("/perfil?actualizado"));
        var usuario = usuarios.findByUsername("usuario2").orElseThrow();
        assertEquals("gamer2@example.com", usuario.getEmail());
        assertEquals("StarCraft", usuario.getJuegosInteres());
        assertTrue(usuario.isNotificarEmail());
    }

    private long nuevoEvento() {
        Evento evento = new Evento();
        evento.setNombre("Torneo de prueba");
        evento.setFechaHora(LocalDateTime.of(2026, 12, 1, 18, 0));
        return eventos.save(evento).getId();
    }
}
