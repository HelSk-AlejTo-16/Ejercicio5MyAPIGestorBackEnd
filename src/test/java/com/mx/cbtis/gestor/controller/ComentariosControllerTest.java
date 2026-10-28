package com.mx.cbtis.gestor.controller;

import static org.mockito.ArgumentMatchers.any;
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
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;

@WebMvcTest(ComentariosController.class)
public class ComentariosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComentariosRepository comentariostable;

    @MockBean
    private TareasRepository tareastable;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Obtener comentarios de tarea existente (200)")
    void testGetComentarioExistente() throws Exception {
        Comentarios c = new Comentarios();
        c.setId_tarea(1);

        when(tareastable.existsById(1)).thenReturn(true);
        when(comentariostable.findByTarea(1)).thenReturn(Collections.singletonList(c));

        mockMvc.perform(post("/getComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(c)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Obtener comentarios sin id_tarea (400)")
    void testGetComentarioSinId() throws Exception {
        mockMvc.perform(post("/getComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Obtener comentarios con tarea inexistente (404)")
    void testGetComentarioTareaInexistente() throws Exception {
        when(tareastable.existsById(99)).thenReturn(false);

        mockMvc.perform(get("/getComentario").param("id_tarea", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Buscar comentario por ID existente (200)")
    void testBuscarComentarioExistente() throws Exception {
        Comentarios c = new Comentarios();
        c.setId_comentario(10);
        c.setTexto_comentario("Excelente avance");

        when(comentariostable.findById(10)).thenReturn(Optional.of(c));

        mockMvc.perform(get("/buscarComentario").param("id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.texto_comentario").value("Excelente avance"));
    }

    @Test
    @DisplayName("Buscar comentario sin parámetro (400)")
    void testBuscarComentarioSinParametro() throws Exception {
        mockMvc.perform(get("/buscarComentario"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Buscar comentario inexistente (404)")
    void testBuscarComentarioInexistente() throws Exception {
        when(comentariostable.findById(88)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buscarComentario").param("id_comentario", "88"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Agregar comentario exitoso (200)")
    void testAddComentarioExitoso() throws Exception {
        Comentarios c = new Comentarios();
        c.setId_tarea(1);
        c.setTexto_comentario("Comentario de prueba");

        when(tareastable.existsById(1)).thenReturn(true);
        when(comentariostable.save(any(Comentarios.class))).thenReturn(c);

        mockMvc.perform(post("/addComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(c)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Agregar comentario nulo (400)")
    void testAddComentarioNulo() throws Exception {
        mockMvc.perform(post("/addComentario"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Agregar comentario con tarea inexistente (400)")
    void testAddComentarioTareaInexistente() throws Exception {
        Comentarios c = new Comentarios();
        c.setId_tarea(999);

        when(tareastable.existsById(999)).thenReturn(false);

        mockMvc.perform(post("/addComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(c)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Actualizar comentario exitoso (200)")
    void testUpdateComentarioExitoso() throws Exception {
        Comentarios c = new Comentarios();
        c.setId_comentario(5);
        c.setId_tarea(1);

        when(comentariostable.existsById(5)).thenReturn(true);
        when(tareastable.existsById(1)).thenReturn(true);
        when(comentariostable.save(any(Comentarios.class))).thenReturn(c);

        mockMvc.perform(put("/updateComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(c)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Actualizar comentario sin ID (400)")
    void testUpdateComentarioSinId() throws Exception {
        Comentarios c = new Comentarios();
        mockMvc.perform(post("/updateComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(c)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Actualizar comentario inexistente (404)")
    void testUpdateComentarioInexistente() throws Exception {
        Comentarios c = new Comentarios();
        c.setId_comentario(999);

        when(comentariostable.existsById(999)).thenReturn(false);

        mockMvc.perform(post("/updateComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(c)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Eliminar comentario exitoso (200)")
    void testDeleteComentarioExitoso() throws Exception {
        Comentarios c = new Comentarios();
        c.setId_comentario(3);

        when(comentariostable.existsById(3)).thenReturn(true);
        doNothing().when(comentariostable).deleteById(3);

        mockMvc.perform(delete("/deleteComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(c)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Eliminar comentario inexistente (404)")
    void testDeleteComentarioInexistente() throws Exception {
        when(comentariostable.existsById(888)).thenReturn(false);

        mockMvc.perform(delete("/deleteComentario").param("id", "888"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Agregar comentario con ID duplicado (409)")
    void testAddComentarioDuplicado() throws Exception {
        Comentarios c = new Comentarios();
        c.setId_comentario(10);
        when(comentariostable.existsById(10)).thenReturn(true);

        mockMvc.perform(post("/addComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(c)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Actualizar comentario con tarea inexistente (400)")
    void testUpdateComentarioTareaInexistente() throws Exception {
        Comentarios c = new Comentarios();
        c.setId_comentario(5);
        c.setId_tarea(999);

        when(comentariostable.existsById(5)).thenReturn(true);
        when(tareastable.existsById(999)).thenReturn(false);

        mockMvc.perform(put("/updateComentario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(c)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Eliminar comentario con param id_comentario (200)")
    void testDeleteComentarioParamIdComentario() throws Exception {
        when(comentariostable.existsById(7)).thenReturn(true);
        doNothing().when(comentariostable).deleteById(7);

        mockMvc.perform(delete("/deleteComentario").param("id_comentario", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Listar todos los comentarios (200)")
    void testListarComentarios() throws Exception {
        when(comentariostable.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/buscarComentarios"))
                .andExpect(status().isOk());
    }
}

