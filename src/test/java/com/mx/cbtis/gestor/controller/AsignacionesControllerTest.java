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
import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@WebMvcTest(AsignacionesController.class)
public class AsignacionesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AsignacionesRepository asignacionestable;

    @MockBean
    private UsuarioRepository usuariotable;

    @MockBean
    private TareasRepository tareastable;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Buscar asignación por ID existente (200)")
    void testBuscarAsignacionExistente() throws Exception {
        Asignaciones a = new Asignaciones();
        a.setId_asignacion(1);
        a.setId_usuario(2);

        when(asignacionestable.findById(1)).thenReturn(Optional.of(a));

        mockMvc.perform(get("/buscarAsignacion").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_usuario").value(2));
    }

    @Test
    @DisplayName("Buscar asignación sin parámetro (400)")
    void testBuscarAsignacionSinParametro() throws Exception {
        mockMvc.perform(get("/buscarAsignacion"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Buscar asignación inexistente (404)")
    void testBuscarAsignacionInexistente() throws Exception {
        when(asignacionestable.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/buscarAsignacion").param("id", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Agregar asignación exitosa (200)")
    void testAddAsignacionExitosa() throws Exception {
        Asignaciones a = new Asignaciones();
        a.setId_usuario(1);
        a.setId_tarea(2);

        when(usuariotable.existsById(1)).thenReturn(true);
        when(tareastable.existsById(2)).thenReturn(true);
        when(asignacionestable.save(any(Asignaciones.class))).thenReturn(a);

        mockMvc.perform(post("/addAsignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(a)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Agregar asignación nula (400)")
    void testAddAsignacionNula() throws Exception {
        mockMvc.perform(post("/addAsignacion"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Agregar asignación con usuario o tarea inexistente (400)")
    void testAddAsignacionUsuarioInexistente() throws Exception {
        Asignaciones a = new Asignaciones();
        a.setId_usuario(999);
        a.setId_tarea(2);

        when(usuariotable.existsById(999)).thenReturn(false);

        mockMvc.perform(post("/addAsignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(a)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Actualizar asignación exitosa (200)")
    void testUpdateAsignacionExitosa() throws Exception {
        Asignaciones a = new Asignaciones();
        a.setId_asignacion(5);
        a.setId_usuario(1);
        a.setId_tarea(2);

        when(asignacionestable.existsById(5)).thenReturn(true);
        when(usuariotable.existsById(1)).thenReturn(true);
        when(tareastable.existsById(2)).thenReturn(true);
        when(asignacionestable.save(any(Asignaciones.class))).thenReturn(a);

        mockMvc.perform(put("/updateAsignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(a)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Actualizar asignación sin ID (400)")
    void testUpdateAsignacionSinId() throws Exception {
        Asignaciones a = new Asignaciones();
        mockMvc.perform(post("/updateAsignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(a)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Actualizar asignación inexistente (404)")
    void testUpdateAsignacionInexistente() throws Exception {
        Asignaciones a = new Asignaciones();
        a.setId_asignacion(999);

        when(asignacionestable.existsById(999)).thenReturn(false);

        mockMvc.perform(post("/updateAsignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(a)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Eliminar asignación exitosa (200)")
    void testDeleteAsignacionExitosa() throws Exception {
        Asignaciones a = new Asignaciones();
        a.setId_asignacion(3);

        when(asignacionestable.existsById(3)).thenReturn(true);
        doNothing().when(asignacionestable).deleteById(3);

        mockMvc.perform(delete("/deleteAsignacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(a)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(1));
    }

    @Test
    @DisplayName("Eliminar asignación inexistente (404)")
    void testDeleteAsignacionInexistente() throws Exception {
        when(asignacionestable.existsById(888)).thenReturn(false);

        mockMvc.perform(delete("/deleteAsignacion").param("id", "888"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Listar todas las asignaciones (200)")
    void testListarAsignaciones() throws Exception {
        when(asignacionestable.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/buscarAsignaciones"))
                .andExpect(status().isOk());
    }
}

