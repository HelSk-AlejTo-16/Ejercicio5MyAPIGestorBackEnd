package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.ProyectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;

@RestController
public class TareasController {
	
	@Autowired
	TareasRepository tareastable;
	
	@Autowired
	ProyectosRepository proyectostable;
	
	// UT-16, UT-17 (H-08, H-10)
	@RequestMapping(value = "/getTarea", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<Object> getTarea(
			@RequestBody(required = false) Tareas body,
			@RequestParam(value = "id_proyecto", required = false) Integer idProyectoParam) {
		
		Integer idProyecto = null;
		if (body != null && body.getId_proyecto() != null) {
			idProyecto = body.getId_proyecto();
		} else if (idProyectoParam != null) {
			idProyecto = idProyectoParam;
		}
		
		if (idProyecto == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Parámetro faltante");
			error.put("mensaje", "Debe proporcionar el ID del proyecto");
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-10: Si el proyecto no existe en la base de datos
		if (!proyectostable.existsById(idProyecto)) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Proyecto inexistente");
			error.put("mensaje", "No se encontró ningún proyecto con el ID especificado: " + idProyecto);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		List<Tareas> listaTareas = tareastable.findByIdProyecto(idProyecto);
		return ResponseEntity.ok()
				.header("X-Mensaje", "Tareas del proyecto consultadas exitosamente. Total: " + listaTareas.size())
				.body(listaTareas);
	}
	
	// UT-18, UT-19 (H-02)
	@GetMapping("/buscarTarea")
	public ResponseEntity<Object> getTarea(
			@RequestParam(value = "id_tarea", required = false) Integer id_tarea,
			@RequestParam(value = "id", required = false) Integer id) {
		
		Integer searchId = (id_tarea != null) ? id_tarea : id;
		if (searchId == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Parámetro faltante");
			error.put("mensaje", "Debe proporcionar el parámetro 'id_tarea' o 'id' para realizar la búsqueda");
			return ResponseEntity.badRequest().body(error);
		}
		
		Optional<Tareas> tarea = tareastable.findById(searchId);
		if (tarea.isPresent()) {
			Tareas t = tarea.get();
			Map<String, Object> respuesta = new HashMap<>();
			respuesta.put("status", HttpStatus.OK.value());
			respuesta.put("mensaje", "Tarea encontrada exitosamente");
			respuesta.put("id_tarea", t.getId_tarea());
			respuesta.put("nombre", t.getNombre());
			respuesta.put("descripcion", t.getDescripcion());
			respuesta.put("fecha_vencimiento", t.getFecha_vencimiento());
			respuesta.put("lista", t.getLista());
			respuesta.put("estado", t.getEstado());
			respuesta.put("id_proyecto", t.getId_proyecto());
			return ResponseEntity.ok(respuesta);
		}
		
		Map<String, Object> error = new HashMap<>();
		error.put("status", HttpStatus.NOT_FOUND.value());
		error.put("error", "Tarea no encontrada");
		error.put("mensaje", "No se encontró ninguna tarea con el ID especificado: " + searchId);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	// UT-20 (H-09, H-11)
	@PostMapping("/addTareas")
	public ResponseEntity<Object> addTareas(@RequestBody(required = false) Tareas tareas) {
		if (tareas == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Datos incompletos");
			error.put("mensaje", "El cuerpo de la solicitud no contiene datos de la tarea");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-09: Evitar duplicar o sobrescribir un ID ya existente
		if (tareas.getId_tarea() != null && tareastable.existsById(tareas.getId_tarea())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.CONFLICT.value());
			error.put("error", "ID existente");
			error.put("mensaje", "Ya existe una tarea con el ID especificado");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
		}
		
		// H-11: Validar integridad referencial de clave foránea id_proyecto
		if (tareas.getId_proyecto() == null || !proyectostable.existsById(tareas.getId_proyecto())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Proyecto inexistente");
			error.put("mensaje", "El proyecto asociado (id_proyecto: " + (tareas.getId_proyecto() != null ? tareas.getId_proyecto() : "nulo") + ") no existe");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		tareastable.save(tareas);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Tarea registrada exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-21 (H-05, H-06, H-07, H-11)
	@RequestMapping(value = "/updateTareas", method = {RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Object> updateTareas(@RequestBody(required = false) Tareas tareas) {
		if (tareas == null || tareas.getId_tarea() == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Datos incompletos");
			error.put("mensaje", "Se requiere el ID de la tarea para actualizar");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-06: Verificar que el registro exista antes de modificar
		if (!tareastable.existsById(tareas.getId_tarea())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Tarea no encontrada");
			error.put("mensaje", "No existe una tarea con el ID proporcionado");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		// H-11: Validar integridad referencial de clave foránea si se especifica
		if (tareas.getId_proyecto() != null && !proyectostable.existsById(tareas.getId_proyecto())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Proyecto inexistente");
			error.put("mensaje", "El proyecto asociado especificado no existe");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		tareastable.save(tareas);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Tarea actualizada exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-22 (H-07)
	@DeleteMapping("/deleteTareas")
	public ResponseEntity<Object> deleteTareas(
			@RequestBody(required = false) Tareas tareas,
			@RequestParam(value = "id_tarea", required = false) Integer id_tarea,
			@RequestParam(value = "id", required = false) Integer id) {
		
		Integer deleteId = null;
		if (tareas != null && tareas.getId_tarea() != null) {
			deleteId = tareas.getId_tarea();
		} else if (id_tarea != null) {
			deleteId = id_tarea;
		} else if (id != null) {
			deleteId = id;
		}
		
		if (deleteId == null || !tareastable.existsById(deleteId)) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Tarea no encontrada");
			error.put("mensaje", "No se encontró ninguna tarea con el ID especificado para eliminar");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		tareastable.deleteById(deleteId);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Tarea eliminada exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-23 (H-08)
	@RequestMapping(value = "/buscarTareas", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<List<Tareas>> getTareas() {
		List<Tareas> listaTareas = new ArrayList<>();
		tareastable.findAll().forEach(listaTareas::add);
		return ResponseEntity.ok()
				.header("X-Mensaje", "Tareas consultadas exitosamente. Total: " + listaTareas.size())
				.body(listaTareas);
	}
}
