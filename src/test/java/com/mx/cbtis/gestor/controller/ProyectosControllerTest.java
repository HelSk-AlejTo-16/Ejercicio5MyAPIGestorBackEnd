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
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProyectosRepository;

@WebMvcTest(ProyectosController.class)
public class ProyectosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProyectosRepository proyectostable;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Buscar proyecto existente (200)")
    void testBuscarProyectoExistente() throws Exception {
        Proyectos p = new Proyectos();
        p.setId_proyecto(1);
        p.setNombre("Proyecto Alpha");

        when(proyectostable.findById(1)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/buscarProyecto").param("id_proyecto", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Proyecto Alpha"));
    }

    @Test
    @DisplayName("Buscar proyecto sin parámetro (400)")
    void testBuscarProyectoSinParametro() throws Exception {
        mockMvc.perform(get("/buscarProyecto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Buscar proyecto inexistente (404)")
    void testBuscarProyectoInexistente() throws Exception {
        when(proyectostable.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buscarProyecto").param("id", "99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Agregar proyecto exitoso (200)")
    void testAddProyectoExitoso() throws Exception {
        Proyectos p = new Proyectos();
        p.setNombre("Nuevo Proyecto");

        when(proyectostable.save(any(Proyectos.class))).thenReturn(p);

        mockMvc.perform(post("/addProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Agregar proyecto nulo (400)")
    void testAddProyectoNulo() throws Exception {
        mockMvc.perform(post("/addProyecto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Agregar proyecto con ID existente (409)")
    void testAddProyectoIdExistente() throws Exception {
        Proyectos p = new Proyectos();
        p.setId_proyecto(10);
        p.setNombre("Conflicto");

        when(proyectostable.existsById(10)).thenReturn(true);

        mockMvc.perform(post("/addProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(p)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Agregar proyecto sin nombre (400)")
    void testAddProyectoSinNombre() throws Exception {
        Proyectos p = new Proyectos();
        p.setNombre("");

        mockMvc.perform(post("/addProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Actualizar proyecto exitoso (200)")
    void testUpdateProyectoExitoso() throws Exception {
        Proyectos p = new Proyectos();
        p.setId_proyecto(5);
        p.setNombre("Actualizado");

        when(proyectostable.existsById(5)).thenReturn(true);
        when(proyectostable.save(any(Proyectos.class))).thenReturn(p);

        mockMvc.perform(put("/updateProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Actualizar proyecto sin ID (400)")
    void testUpdateProyectoSinId() throws Exception {
        Proyectos p = new Proyectos();
        mockMvc.perform(post("/updateProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Actualizar proyecto inexistente (404)")
    void testUpdateProyectoInexistente() throws Exception {
        Proyectos p = new Proyectos();
        p.setId_proyecto(999);

        when(proyectostable.existsById(999)).thenReturn(false);

        mockMvc.perform(post("/updateProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(p)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Eliminar proyecto exitoso (200)")
    void testDeleteProyectoExitoso() throws Exception {
        Proyectos p = new Proyectos();
        p.setId_proyecto(7);

        when(proyectostable.existsById(7)).thenReturn(true);
        doNothing().when(proyectostable).deleteById(7);

        mockMvc.perform(delete("/deleteProyecto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Eliminar proyecto inexistente (404)")
    void testDeleteProyectoInexistente() throws Exception {
        when(proyectostable.existsById(888)).thenReturn(false);

        mockMvc.perform(delete("/deleteProyecto").param("id", "888"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Listar proyectos catálogo completo (200)")
    void testListarProyectos() throws Exception {
        when(proyectostable.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/buscarProyectos"))
                .andExpect(status().isOk());
    }
}

