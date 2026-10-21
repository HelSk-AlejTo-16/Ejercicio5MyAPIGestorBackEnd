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

import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@RestController
public class AsignacionesController {
	
	@Autowired
	AsignacionesRepository asignacionestable;
	
	@Autowired
	UsuarioRepository usuariotable;
	
	@Autowired
	TareasRepository tareastable;
	
	// UT-31, UT-32 (H-02)
	@GetMapping("/buscarAsignacion")
	public ResponseEntity<Object> getAsignacion(
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "id_asignacion", required = false) Integer id_asignacion) {
		
		Integer searchId = (id != null) ? id : id_asignacion;
		if (searchId == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Parámetro faltante");
			error.put("mensaje", "Debe proporcionar el parámetro 'id' o 'id_asignacion' para realizar la búsqueda");
			return ResponseEntity.badRequest().body(error);
		}
		
		Optional<Asignaciones> asignacion = asignacionestable.findById(searchId);
		if (asignacion.isPresent()) {
			Asignaciones a = asignacion.get();
			Map<String, Object> respuesta = new HashMap<>();
			respuesta.put("status", HttpStatus.OK.value());
			respuesta.put("mensaje", "Asignación encontrada exitosamente");
			respuesta.put("id_asignacion", a.getId_asignacion());
			respuesta.put("id_usuario", a.getId_usuario());
			respuesta.put("id_tarea", a.getId_tarea());
			respuesta.put("fecha_asignacion", a.getFecha_asignacion());
			return ResponseEntity.ok(respuesta);
		}
		
		Map<String, Object> error = new HashMap<>();
		error.put("status", HttpStatus.NOT_FOUND.value());
		error.put("error", "Asignación no encontrada");
		error.put("mensaje", "No se encontró ninguna asignación con el ID especificado: " + searchId);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	// UT-33 (H-06, H-09, H-11)
	@PostMapping("/addAsignacion")
	public ResponseEntity<Object> addAsignacion(@RequestBody(required = false) Asignaciones asignacion) {
		if (asignacion == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Datos incompletos");
			error.put("mensaje", "El cuerpo de la solicitud no contiene datos de la asignación");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-09: No permitir duplicar/sobrescribir ID si ya existe
		if (asignacion.getId_asignacion() != null && asignacionestable.existsById(asignacion.getId_asignacion())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.CONFLICT.value());
			error.put("error", "ID existente");
			error.put("mensaje", "Ya existe una asignación con el ID especificado");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
		}
		
		// H-11: Validar claves foráneas id_usuario y id_tarea
		if (asignacion.getId_usuario() == null || !usuariotable.existsById(asignacion.getId_usuario())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Usuario inexistente");
			error.put("mensaje", "El usuario asociado (id_usuario: " + (asignacion.getId_usuario() != null ? asignacion.getId_usuario() : "nulo") + ") no existe");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		if (asignacion.getId_tarea() == null || !tareastable.existsById(asignacion.getId_tarea())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Tarea inexistente");
			error.put("mensaje", "La tarea asociada (id_tarea: " + (asignacion.getId_tarea() != null ? asignacion.getId_tarea() : "nulo") + ") no existe");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		asignacionestable.save(asignacion);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Asignación registrada exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-34 (H-05, H-06, H-07, H-11)
	@RequestMapping(value = "/updateAsignacion", method = {RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Object> updateAsignacion(@RequestBody(required = false) Asignaciones asignacion) {
		if (asignacion == null || asignacion.getId_asignacion() == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Datos incompletos");
			error.put("mensaje", "Se requiere el ID de la asignación para actualizar");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-06: Validar existencia antes de actualizar
		if (!asignacionestable.existsById(asignacion.getId_asignacion())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Asignación no encontrada");
			error.put("mensaje", "No existe una asignación con el ID proporcionado");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		// H-11: Validar claves foráneas si se modifican
		if (asignacion.getId_usuario() != null && !usuariotable.existsById(asignacion.getId_usuario())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Usuario inexistente");
			error.put("mensaje", "El usuario asociado especificado no existe");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		if (asignacion.getId_tarea() != null && !tareastable.existsById(asignacion.getId_tarea())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Tarea inexistente");
			error.put("mensaje", "La tarea asociada especificada no existe");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		asignacionestable.save(asignacion);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Asignación actualizada exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-35 (H-07)
	@DeleteMapping("/deleteAsignacion")
	public ResponseEntity<Object> deleteAsignacion(
			@RequestBody(required = false) Asignaciones asignacion,
			@RequestParam(value = "id_asignacion", required = false) Integer id_asignacion,
			@RequestParam(value = "id", required = false) Integer id) {
		
		Integer deleteId = null;
		if (asignacion != null && asignacion.getId_asignacion() != null) {
			deleteId = asignacion.getId_asignacion();
		} else if (id_asignacion != null) {
			deleteId = id_asignacion;
		} else if (id != null) {
			deleteId = id;
		}
		
		if (deleteId == null || !asignacionestable.existsById(deleteId)) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Asignación no encontrada");
			error.put("mensaje", "No se encontró ninguna asignación con el ID especificado para eliminar");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		asignacionestable.deleteById(deleteId);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Asignación eliminada exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}   
	
	// UT-36 (H-08)
	@RequestMapping(value = "/buscarAsignaciones", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<List<Asignaciones>> getAsignaciones() {
		List<Asignaciones> listaAsignaciones = new ArrayList<>();
		asignacionestable.findAll().forEach(listaAsignaciones::add);
		return ResponseEntity.ok()
				.header("X-Mensaje", "Asignaciones consultadas exitosamente. Total: " + listaAsignaciones.size())
				.body(listaAsignaciones);
	}
}
