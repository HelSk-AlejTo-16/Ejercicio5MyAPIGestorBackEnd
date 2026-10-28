package com.mx.cbtis.gestor.modelo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ModelosTest {

    @Test
    @DisplayName("Test modelo Usuario: constructores, getters y setters")
    void testUsuarioModel() {
        Usuario u1 = new Usuario();
        u1.setId_usuario(1);
        u1.setNombre("Juan");
        u1.setCorreo_electronico("juan@correo.com");
        u1.setContrasena("pass123");
        u1.setRol("Desarrollador");
        u1.setImagen_usuario("avatar.png");

        assertEquals(1, u1.getId_usuario());
        assertEquals("Juan", u1.getNombre());
        assertEquals("juan@correo.com", u1.getCorreo_electronico());
        assertEquals("pass123", u1.getContrasena());
        assertEquals("Desarrollador", u1.getRol());
        assertEquals("avatar.png", u1.getImagen_usuario());

        Usuario u2 = new Usuario(2, "Maria", "maria@correo.com", "pass456", "Admin", "maria.png");
        assertEquals(2, u2.getId_usuario());
        assertEquals("Maria", u2.getNombre());
        assertEquals("maria@correo.com", u2.getCorreo_electronico());
        assertEquals("pass456", u2.getContrasena());
        assertEquals("Admin", u2.getRol());
        assertEquals("maria.png", u2.getImagen_usuario());
    }

    @Test
    @DisplayName("Test modelo Proyectos: constructores, getters y setters")
    void testProyectosModel() {
        Proyectos p1 = new Proyectos();
        p1.setId_proyecto(10);
        p1.setNombre("Sistema Web");
        p1.setDescripcion("Descripcion del sistema");
        p1.setFecha_inicio("2026-01-01");
        p1.setFecha_finalizacion("2026-12-31");
        p1.setEstado_proyecto("En progreso");
        p1.setFondo("fondo.jpg");

        assertEquals(10, p1.getId_proyecto());
        assertEquals("Sistema Web", p1.getNombre());
        assertEquals("Descripcion del sistema", p1.getDescripcion());
        assertEquals("2026-01-01", p1.getFecha_inicio());
        assertEquals("2026-12-31", p1.getFecha_finalizacion());
        assertEquals("En progreso", p1.getEstado_proyecto());
        assertEquals("fondo.jpg", p1.getFondo());

        Proyectos p2 = new Proyectos(20, "App Movil", "App iOS/Android", "2026-02-01", "2026-08-01", "Finalizado", "fondo2.jpg");
        assertEquals(20, p2.getId_proyecto());
        assertEquals("App Movil", p2.getNombre());
        assertEquals("App iOS/Android", p2.getDescripcion());
        assertEquals("2026-02-01", p2.getFecha_inicio());
        assertEquals("2026-08-01", p2.getFecha_finalizacion());
        assertEquals("Finalizado", p2.getEstado_proyecto());
        assertEquals("fondo2.jpg", p2.getFondo());
    }

    @Test
    @DisplayName("Test modelo Tareas: constructores, getters y setters")
    void testTareasModel() {
        Tareas t1 = new Tareas();
        t1.setId_tarea(100);
        t1.setNombre("Crear API");
        t1.setDescripcion("Crear endpoints REST");
        t1.setFecha_vencimiento("2026-05-15");
        t1.setLista("Backlog");
        t1.setEstado("Pendiente");
        t1.setId_proyecto(10);

        assertEquals(100, t1.getId_tarea());
        assertEquals("Crear API", t1.getNombre());
        assertEquals("Crear endpoints REST", t1.getDescripcion());
        assertEquals("2026-05-15", t1.getFecha_vencimiento());
        assertEquals("Backlog", t1.getLista());
        assertEquals("Pendiente", t1.getEstado());
        assertEquals(10, t1.getId_proyecto());

        Tareas t2 = new Tareas(200, "Diseno UI", "Mockups Figma", "2026-04-10", "En progreso", "En proceso", 20);
        assertEquals(200, t2.getId_tarea());
        assertEquals("Diseno UI", t2.getNombre());
        assertEquals("Mockups Figma", t2.getDescripcion());
        assertEquals("2026-04-10", t2.getFecha_vencimiento());
        assertEquals("En progreso", t2.getLista());
        assertEquals("En proceso", t2.getEstado());
        assertEquals(20, t2.getId_proyecto());
    }

    @Test
    @DisplayName("Test modelo Comentarios: constructores, getters y setters")
    void testComentariosModel() {
        Comentarios c1 = new Comentarios();
        c1.setId_comentario(50);
        c1.setId_usuario("1");
        c1.setId_tarea(100);
        c1.setTexto_comentario("Comentario de prueba");
        c1.setFecha_hora("2026-03-01 10:00:00");

        assertEquals(50, c1.getId_comentario());
        assertEquals("1", c1.getId_usuario());
        assertEquals(100, c1.getId_tarea());
        assertEquals("Comentario de prueba", c1.getTexto_comentario());
        assertEquals("2026-03-01 10:00:00", c1.getFecha_hora());

        Comentarios c2 = new Comentarios(60, "2", 200, "Segundo comentario", "2026-03-02 11:30:00");
        assertEquals(60, c2.getId_comentario());
        assertEquals("2", c2.getId_usuario());
        assertEquals(200, c2.getId_tarea());
        assertEquals("Segundo comentario", c2.getTexto_comentario());
        assertEquals("2026-03-02 11:30:00", c2.getFecha_hora());
    }

    @Test
    @DisplayName("Test modelo Asignaciones: constructores, getters y setters")
    void testAsignacionesModel() {
        Asignaciones a1 = new Asignaciones();
        a1.setId_asignacion(30);
        a1.setId_usuario(1);
        a1.setId_tarea(100);
        a1.setFecha_asignacion("2026-03-01");

        assertEquals(30, a1.getId_asignacion());
        assertEquals(1, a1.getId_usuario());
        assertEquals(100, a1.getId_tarea());
        assertEquals("2026-03-01", a1.getFecha_asignacion());

        Asignaciones a2 = new Asignaciones(40, 2, 200, "2026-03-02");
        assertEquals(40, a2.getId_asignacion());
        assertEquals(2, a2.getId_usuario());
        assertEquals(200, a2.getId_tarea());
        assertEquals("2026-03-02", a2.getFecha_asignacion());
    }
}

