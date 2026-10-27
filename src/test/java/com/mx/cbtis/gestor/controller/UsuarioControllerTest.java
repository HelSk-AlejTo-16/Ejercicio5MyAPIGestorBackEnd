package com.mx.cbtis.gestor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioRepository usuariotable;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Login exitoso con credenciales correctas (200)")
    void testLoginExitoso() throws Exception {
        Usuario u = new Usuario();
        u.setId_usuario(1);
        u.setNombre("Liliana");
        u.setCorreo_electronico("lili@test.com");
        u.setContrasena("pass123");

        when(usuariotable.findByCorreoPass("lili@test.com", "pass123")).thenReturn(Optional.of(u));

        mockMvc.perform(post("/getUsuarioLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Liliana"));
    }

    @Test
    @DisplayName("Login con payload nulo o incompleto (401)")
    void testLoginIncompleto() throws Exception {
        mockMvc.perform(post("/getUsuarioLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.nombre").value("000"));
    }

    @Test
    @DisplayName("Login con correo inexistente (401)")
    void testLoginCorreoInexistente() throws Exception {
        Usuario u = new Usuario();
        u.setCorreo_electronico("noexiste@test.com");
        u.setContrasena("pass123");

        when(usuariotable.findByCorreoPass("noexiste@test.com", "pass123")).thenReturn(Optional.empty());
        when(usuariotable.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/getUsuarioLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    @DisplayName("Login con contraseña incorrecta (401)")
    void testLoginPasswordIncorrecto() throws Exception {
        Usuario u = new Usuario();
        u.setCorreo_electronico("lili@test.com");
        u.setContrasena("wrongpass");

        when(usuariotable.findByCorreoPass("lili@test.com", "wrongpass")).thenReturn(Optional.empty());
        when(usuariotable.findByCorreo("lili@test.com")).thenReturn(Optional.of(new Usuario()));

        mockMvc.perform(post("/getUsuarioLogin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    @DisplayName("Buscar usuario por ID existente (200)")
    void testBuscarUsuarioExistente() throws Exception {
        Usuario u = new Usuario();
        u.setId_usuario(5);
        u.setNombre("Carlos");

        when(usuariotable.findById(5)).thenReturn(Optional.of(u));

        mockMvc.perform(get("/buscarUsuario").param("id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos"));
    }

    @Test
    @DisplayName("Buscar usuario con parámetro faltante (400)")
    void testBuscarUsuarioParametroFaltante() throws Exception {
        mockMvc.perform(get("/buscarUsuario"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Buscar usuario por ID inexistente (404)")
    void testBuscarUsuarioInexistente() throws Exception {
        when(usuariotable.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buscarUsuario").param("id_usuario", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Agregar usuario exitoso (200)")
    void testAddUsuarioExitoso() throws Exception {
        Usuario u = new Usuario();
        u.setNombre("Nuevo");
        u.setCorreo_electronico("nuevo@dominio.com");
        u.setContrasena("segura123");

        when(usuariotable.save(any(Usuario.class))).thenReturn(u);

        mockMvc.perform(post("/addUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Agregar usuario con body nulo (400)")
    void testAddUsuarioNulo() throws Exception {
        mockMvc.perform(post("/addUsuario"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Agregar usuario con ID existente (409)")
    void testAddUsuarioIdExistente() throws Exception {
        Usuario u = new Usuario();
        u.setId_usuario(1);
        u.setCorreo_electronico("user@test.com");
        u.setContrasena("pass123");

        when(usuariotable.existsById(1)).thenReturn(true);

        mockMvc.perform(post("/addUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Agregar usuario con correo inválido (400)")
    void testAddUsuarioCorreoInvalido() throws Exception {
        Usuario u = new Usuario();
        u.setCorreo_electronico("correo_sin_arroba");
        u.setContrasena("pass123");

        mockMvc.perform(post("/addUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Formato de correo inválido"));
    }

    @Test
    @DisplayName("Agregar usuario con contraseña insegura (400)")
    void testAddUsuarioContrasenaInsegura() throws Exception {
        Usuario u = new Usuario();
        u.setCorreo_electronico("valido@test.com");
        u.setContrasena("123"); // muy corta

        mockMvc.perform(post("/addUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Contraseña insegura"));
    }

    @Test
    @DisplayName("Actualizar usuario exitoso con PUT y POST (200)")
    void testUpdateUsuarioExitoso() throws Exception {
        Usuario u = new Usuario();
        u.setId_usuario(2);
        u.setCorreo_electronico("actualizado@test.com");

        when(usuariotable.existsById(2)).thenReturn(true);
        when(usuariotable.save(any(Usuario.class))).thenReturn(u);

        mockMvc.perform(put("/updateUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Actualizar usuario inexistente (404)")
    void testUpdateUsuarioInexistente() throws Exception {
        Usuario u = new Usuario();
        u.setId_usuario(999);

        when(usuariotable.existsById(999)).thenReturn(false);

        mockMvc.perform(post("/updateUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Actualizar usuario con correo inválido (400)")
    void testUpdateUsuarioCorreoInvalido() throws Exception {
        Usuario u = new Usuario();
        u.setId_usuario(2);
        u.setCorreo_electronico("invalido");

        when(usuariotable.existsById(2)).thenReturn(true);

        mockMvc.perform(post("/updateUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Actualizar usuario sin ID (400)")
    void testUpdateUsuarioSinId() throws Exception {
        Usuario u = new Usuario();
        mockMvc.perform(post("/updateUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Eliminar usuario exitoso (200)")
    void testDeleteUsuarioExitoso() throws Exception {
        Usuario u = new Usuario();
        u.setId_usuario(3);

        when(usuariotable.existsById(3)).thenReturn(true);
        doNothing().when(usuariotable).deleteById(3);

        mockMvc.perform(delete("/deleteUsuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(u)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Eliminar usuario inexistente (404)")
    void testDeleteUsuarioInexistente() throws Exception {
        when(usuariotable.existsById(888)).thenReturn(false);

        mockMvc.perform(delete("/deleteUsuario").param("id", "888"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Listar usuarios catálogo completo (200)")
    void testListarUsuarios() throws Exception {
        when(usuariotable.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/buscarUsuarios"))
                .andExpect(status().isOk());
    }
}

