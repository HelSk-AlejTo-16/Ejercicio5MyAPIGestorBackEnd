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

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;

@RestController
public class ComentariosController {
	
	@Autowired
	ComentariosRepository comentariostable;
	
	@Autowired
	TareasRepository tareastable;
	
	// UT-24 (H-08, H-10)
	@RequestMapping(value = "/getComentario", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<Object> getComentario(
			@RequestBody(required = false) Comentarios idComentario,
			@RequestParam(value = "id_tarea", required = false) Integer idTareaParam) {
		
		Integer idTarea = null;
		if (idComentario != null && idComentario.getId_tarea() != null) {
			idTarea = idComentario.getId_tarea();
		} else if (idTareaParam != null) {
			idTarea = idTareaParam;
		}
		
		if (idTarea == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Parámetro faltante");
			error.put("mensaje", "Debe proporcionar el ID de la tarea");
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-10: Si la tarea no existe en base de datos
		if (!tareastable.existsById(idTarea)) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Tarea inexistente");
			error.put("mensaje", "No se encontró ninguna tarea con el ID especificado: " + idTarea);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		List<Comentarios> listaComentarios = comentariostable.findByTarea(idTarea);
		return ResponseEntity.ok()
				.header("X-Mensaje", "Comentarios de la tarea consultados exitosamente. Total: " + listaComentarios.size())
				.body(listaComentarios);
	}
	
	// UT-25, UT-26 (H-02)
	@GetMapping("/buscarComentario")
	public ResponseEntity<Object> getComentario(
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "id_comentario", required = false) Integer id_comentario) {
		
		Integer searchId = (id != null) ? id : id_comentario;
		if (searchId == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Parámetro faltante");
			error.put("mensaje", "Debe proporcionar el parámetro 'id' o 'id_comentario' para realizar la búsqueda");
			return ResponseEntity.badRequest().body(error);
		}
		
		Optional<Comentarios> comentario = comentariostable.findById(searchId);
		if (comentario.isPresent()) {
			Comentarios c = comentario.get();
			Map<String, Object> respuesta = new HashMap<>();
			respuesta.put("status", HttpStatus.OK.value());
			respuesta.put("mensaje", "Comentario encontrado exitosamente");
			respuesta.put("id_comentario", c.getId_comentario());
			respuesta.put("id_usuario", c.getId_usuario());
			respuesta.put("id_tarea", c.getId_tarea());
			respuesta.put("texto_comentario", c.getTexto_comentario());
			respuesta.put("fecha_hora", c.getFecha_hora());
			return ResponseEntity.ok(respuesta);
		}
		
		Map<String, Object> error = new HashMap<>();
		error.put("status", HttpStatus.NOT_FOUND.value());
		error.put("error", "Comentario no encontrado");
		error.put("mensaje", "No se encontró ningún comentario con el ID especificado: " + searchId);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	// UT-27 (H-06, H-09, H-11)
	@PostMapping("/addComentario")
	public ResponseEntity<Object> addComentario(@RequestBody(required = false) Comentarios comentario) {
		if (comentario == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Datos incompletos");
			error.put("mensaje", "El cuerpo de la solicitud no contiene datos del comentario");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-09: No permitir crear si ya existe con ese ID
		if (comentario.getId_comentario() != null && comentariostable.existsById(comentario.getId_comentario())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.CONFLICT.value());
			error.put("error", "ID existente");
			error.put("mensaje", "Ya existe un comentario con el ID especificado");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
		}
		
		// H-11: Validar clave foránea id_tarea
		if (comentario.getId_tarea() == null || !tareastable.existsById(comentario.getId_tarea())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Tarea inexistente");
			error.put("mensaje", "La tarea asociada (id_tarea: " + (comentario.getId_tarea() != null ? comentario.getId_tarea() : "nulo") + ") no existe");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		comentariostable.save(comentario);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Comentario registrado exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-28 (H-05, H-06, H-07, H-11)
	@RequestMapping(value = "/updateComentario", method = {RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Object> updateComentario(@RequestBody(required = false) Comentarios comentario) {
		if (comentario == null || comentario.getId_comentario() == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Datos incompletos");
			error.put("mensaje", "Se requiere el ID del comentario para actualizar");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-06: Validar existencia antes de actualizar
		if (!comentariostable.existsById(comentario.getId_comentario())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Comentario no encontrado");
			error.put("mensaje", "No existe un comentario con el ID proporcionado");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		// H-11: Validar clave foránea id_tarea si se actualiza
		if (comentario.getId_tarea() != null && !tareastable.existsById(comentario.getId_tarea())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Tarea inexistente");
			error.put("mensaje", "La tarea asociada especificada no existe");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		comentariostable.save(comentario);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Comentario actualizado exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-29 (H-07)
	@DeleteMapping("/deleteComentario")
	public ResponseEntity<Object> deleteComentario(
			@RequestBody(required = false) Comentarios comentario,
			@RequestParam(value = "id_comentario", required = false) Integer id_comentario,
			@RequestParam(value = "id", required = false) Integer id) {
		
		Integer deleteId = null;
		if (comentario != null && comentario.getId_comentario() != null) {
			deleteId = comentario.getId_comentario();
		} else if (id_comentario != null) {
			deleteId = id_comentario;
		} else if (id != null) {
			deleteId = id;
		}
		
		if (deleteId == null || !comentariostable.existsById(deleteId)) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Comentario no encontrado");
			error.put("mensaje", "No se encontró ningún comentario con el ID especificado para eliminar");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		comentariostable.deleteById(deleteId);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Comentario eliminado exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-30 (H-08)
	@RequestMapping(value = "/buscarComentarios", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<List<Comentarios>> getComentarios() {
		List<Comentarios> listaComentarios = new ArrayList<>();
		comentariostable.findAll().forEach(listaComentarios::add);
		return ResponseEntity.ok()
				.header("X-Mensaje", "Comentarios consultados exitosamente. Total: " + listaComentarios.size())
				.body(listaComentarios);
	}
}
