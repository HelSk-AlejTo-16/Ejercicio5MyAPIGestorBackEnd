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
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.ProyectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;

@WebMvcTest(TareasController.class)
public class TareasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TareasRepository tareastable;

    @MockBean
    private ProyectosRepository proyectostable;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Obtener tareas por id_proyecto existente (200)")
    void testGetTareaProyectoExistente() throws Exception {
        Tareas t = new Tareas();
        t.setId_proyecto(1);

        when(proyectostable.existsById(1)).thenReturn(true);
        when(tareastable.findByIdProyecto(1)).thenReturn(Collections.singletonList(t));

        mockMvc.perform(post("/getTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(t)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Obtener tareas sin id_proyecto (400)")
    void testGetTareaSinParametro() throws Exception {
        mockMvc.perform(post("/getTarea")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Obtener tareas con id_proyecto inexistente (404)")
    void testGetTareaProyectoInexistente() throws Exception {
        when(proyectostable.existsById(99)).thenReturn(false);

        mockMvc.perform(get("/getTarea").param("id_proyecto", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Buscar tarea por ID existente (200)")
    void testBuscarTareaExistente() throws Exception {
        Tareas t = new Tareas();
        t.setId_tarea(10);
        t.setNombre("Diseño UI");

        when(tareastable.findById(10)).thenReturn(Optional.of(t));

        mockMvc.perform(get("/buscarTarea").param("id_tarea", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Diseño UI"));
    }

    @Test
    @DisplayName("Buscar tarea sin parámetro (400)")
    void testBuscarTareaSinParametro() throws Exception {
        mockMvc.perform(get("/buscarTarea"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Buscar tarea inexistente (404)")
    void testBuscarTareaInexistente() throws Exception {
        when(tareastable.findById(88)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buscarTarea").param("id", "88"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Agregar tarea exitosa (200)")
    void testAddTareaExitosa() throws Exception {
        Tareas t = new Tareas();
        t.setNombre("Nueva Tarea");
        t.setId_proyecto(1);

        when(proyectostable.existsById(1)).thenReturn(true);
        when(tareastable.save(any(Tareas.class))).thenReturn(t);

        mockMvc.perform(post("/addTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(t)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Agregar tarea con cuerpo nulo (400)")
    void testAddTareaNula() throws Exception {
        mockMvc.perform(post("/addTareas"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Agregar tarea con ID existente (409)")
    void testAddTareaIdExistente() throws Exception {
        Tareas t = new Tareas();
        t.setId_tarea(5);
        t.setId_proyecto(1);

        when(tareastable.existsById(5)).thenReturn(true);

        mockMvc.perform(post("/addTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(t)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Agregar tarea con proyecto inexistente (400)")
    void testAddTareaProyectoInexistente() throws Exception {
        Tareas t = new Tareas();
        t.setId_proyecto(999);

        when(proyectostable.existsById(999)).thenReturn(false);

        mockMvc.perform(post("/addTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(t)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Actualizar tarea exitosa (200)")
    void testUpdateTareaExitosa() throws Exception {
        Tareas t = new Tareas();
        t.setId_tarea(3);
        t.setId_proyecto(1);

        when(tareastable.existsById(3)).thenReturn(true);
        when(proyectostable.existsById(1)).thenReturn(true);
        when(tareastable.save(any(Tareas.class))).thenReturn(t);

        mockMvc.perform(put("/updateTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(t)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Actualizar tarea sin ID (400)")
    void testUpdateTareaSinId() throws Exception {
        Tareas t = new Tareas();
        mockMvc.perform(post("/updateTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(t)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Actualizar tarea inexistente (404)")
    void testUpdateTareaInexistente() throws Exception {
        Tareas t = new Tareas();
        t.setId_tarea(999);

        when(tareastable.existsById(999)).thenReturn(false);

        mockMvc.perform(post("/updateTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(t)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Eliminar tarea exitosa (200)")
    void testDeleteTareaExitosa() throws Exception {
        Tareas t = new Tareas();
        t.setId_tarea(4);

        when(tareastable.existsById(4)).thenReturn(true);
        doNothing().when(tareastable).deleteById(4);

        mockMvc.perform(delete("/deleteTareas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(t)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Eliminar tarea inexistente (404)")
    void testDeleteTareaInexistente() throws Exception {
        when(tareastable.existsById(888)).thenReturn(false);

        mockMvc.perform(delete("/deleteTareas").param("id", "888"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Listar todas las tareas (200)")
    void testListarTareas() throws Exception {
        when(tareastable.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/buscarTareas"))
                .andExpect(status().isOk());
    }
}

