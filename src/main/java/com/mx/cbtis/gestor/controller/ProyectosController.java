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

import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProyectosRepository;

@RestController
public class ProyectosController {
	
	@Autowired 
	ProyectosRepository proyectostable;
	
	// UT-09, UT-10 (H-02)
	@GetMapping("/buscarProyecto")
	public ResponseEntity<Object> getProyecto(
			@RequestParam(value = "id_proyecto", required = false) Integer id_proyecto,
			@RequestParam(value = "id", required = false) Integer id) {
		
		Integer searchId = (id_proyecto != null) ? id_proyecto : id;
		if (searchId == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Parámetro faltante");
			error.put("mensaje", "Debe proporcionar el parámetro 'id_proyecto' o 'id' para realizar la búsqueda");
			return ResponseEntity.badRequest().body(error);
		}
		
		Optional<Proyectos> proyecto = proyectostable.findById(searchId);
		if (proyecto.isPresent()) {
			Proyectos p = proyecto.get();
			Map<String, Object> respuesta = new HashMap<>();
			respuesta.put("status", HttpStatus.OK.value());
			respuesta.put("mensaje", "Proyecto encontrado exitosamente");
			respuesta.put("id_proyecto", p.getId_proyecto());
			respuesta.put("nombre", p.getNombre());
			respuesta.put("descripcion", p.getDescripcion());
			respuesta.put("fecha_inicio", p.getFecha_inicio());
			respuesta.put("fecha_finalizacion", p.getFecha_finalizacion());
			respuesta.put("estado_proyecto", p.getEstado_proyecto());
			respuesta.put("fondo", p.getFondo());
			return ResponseEntity.ok(respuesta);
		}
		
		Map<String, Object> error = new HashMap<>();
		error.put("status", HttpStatus.NOT_FOUND.value());
		error.put("error", "Proyecto no encontrado");
		error.put("mensaje", "No se encontró ningún proyecto con el ID especificado: " + searchId);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	// UT-11, UT-12 (H-03, H-04, H-09, H-11)
	@PostMapping("/addProyecto")
	public ResponseEntity<Object> addProyecto(@RequestBody(required = false) Proyectos proyecto) {
		if (proyecto == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Datos incompletos");
			error.put("mensaje", "El cuerpo de la solicitud no contiene datos del proyecto");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-09: Si el ID ya existe en la base de datos, rechazar para no sobrescribir o duplicar indebidamente
		if (proyecto.getId_proyecto() != null && proyectostable.existsById(proyecto.getId_proyecto())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.CONFLICT.value());
			error.put("error", "ID existente");
			error.put("mensaje", "Ya existe un proyecto con el ID especificado");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
		}
		
		// Validación de campos requeridos
		if (proyecto.getNombre() == null || proyecto.getNombre().trim().isEmpty()) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Nombre requerido");
			error.put("mensaje", "El nombre del proyecto es obligatorio");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		proyectostable.save(proyecto);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Proyecto registrado exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-13 (H-05, H-06, H-07, H-11)
	@RequestMapping(value = "/updateProyecto", method = {RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Object> updateProyecto(@RequestBody(required = false) Proyectos proyecto) {
		if (proyecto == null || proyecto.getId_proyecto() == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Datos incompletos");
			error.put("mensaje", "Se requiere el ID del proyecto para actualizar");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-06: Verificar que el registro exista antes de modificar
		if (!proyectostable.existsById(proyecto.getId_proyecto())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Proyecto no encontrado");
			error.put("mensaje", "No existe un proyecto con el ID proporcionado");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		proyectostable.save(proyecto);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Proyecto actualizado exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-14 (H-07)
	@DeleteMapping("/deleteProyecto")
	public ResponseEntity<Object> deleteProyecto(
			@RequestBody(required = false) Proyectos proyecto,
			@RequestParam(value = "id_proyecto", required = false) Integer id_proyecto,
			@RequestParam(value = "id", required = false) Integer id) {
		
		Integer deleteId = null;
		if (proyecto != null && proyecto.getId_proyecto() != null) {
			deleteId = proyecto.getId_proyecto();
		} else if (id_proyecto != null) {
			deleteId = id_proyecto;
		} else if (id != null) {
			deleteId = id;
		}
		
		if (deleteId == null || !proyectostable.existsById(deleteId)) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Proyecto no encontrado");
			error.put("mensaje", "No se encontró ningún proyecto con el ID especificado para eliminar");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		proyectostable.deleteById(deleteId);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Proyecto eliminado exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}   
	
	// UT-15 (H-08)
	@RequestMapping(value = "/buscarProyectos", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<List<Proyectos>> getProyectos() {
		List<Proyectos> listaProyectos = new ArrayList<>();
		proyectostable.findAll().forEach(listaProyectos::add);
		return ResponseEntity.ok()
				.header("X-Mensaje", "Proyectos consultados exitosamente. Total: " + listaProyectos.size())
				.body(listaProyectos);
	}
}
