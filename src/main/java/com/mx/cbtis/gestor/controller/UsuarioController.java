package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@RestController
public class UsuarioController {
	
	@Autowired
	UsuarioRepository usuariotable;
	
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private boolean esCorreoValido(String correo) {
		return correo != null && EMAIL_PATTERN.matcher(correo.trim()).matches();
	}

	private boolean esContrasenaSegura(String pass) {
		if (pass == null || pass.trim().length() < 6) {
			return false;
		}
		boolean tieneLetra = pass.matches(".*[a-zA-Z].*");
		boolean tieneNumero = pass.matches(".*[0-9].*");
		return tieneLetra && tieneNumero;
	}
	
	// UT-01, UT-02 (H-01)
	@PostMapping("/getUsuarioLogin")
	public ResponseEntity<Object> getUsuarioLogin(@RequestBody(required = false) Usuario userParam) {
		if (userParam == null || userParam.getCorreo_electronico() == null || userParam.getContrasena() == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.UNAUTHORIZED.value());
			error.put("error", "Credenciales incompletas");
			error.put("mensaje", "Debe proporcionar correo electrónico y contraseña");
			error.put("nombre", "000");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
		}
		
		Optional<Usuario> optionaluser = usuariotable.findByCorreoPass(
				userParam.getCorreo_electronico(),
				userParam.getContrasena());
		
		if (optionaluser.isPresent()) {
			return ResponseEntity.ok(optionaluser.get());
		} else {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.UNAUTHORIZED.value());
			error.put("nombre", "000");
			// H-01: Indicar la causa específica del fallo
			Optional<Usuario> usuarioPorCorreo = usuariotable.findByCorreo(userParam.getCorreo_electronico());
			if (usuarioPorCorreo.isEmpty()) {
				error.put("error", "Usuario no encontrado");
				error.put("mensaje", "El correo electrónico no se encuentra registrado");
			} else {
				error.put("error", "Credenciales inválidas");
				error.put("mensaje", "La contraseña es incorrecta");
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
		}
	}
	
	// UT-03, UT-04 (H-02)
	@GetMapping("/buscarUsuario")
	public ResponseEntity<Object> getUsuario(@RequestParam(value = "id", required = false) Integer id,
	                                          @RequestParam(value = "id_usuario", required = false) Integer id_usuario) {
		Integer searchId = (id != null) ? id : id_usuario;
		if (searchId == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Parámetro faltante");
			error.put("mensaje", "Debe proporcionar el parámetro 'id' o 'id_usuario' para realizar la búsqueda");
			return ResponseEntity.badRequest().body(error);
		}
		
		Optional<Usuario> usuario = usuariotable.findById(searchId);
		if (usuario.isPresent()) {
			Usuario u = usuario.get();
			Map<String, Object> respuesta = new HashMap<>();
			respuesta.put("status", HttpStatus.OK.value());
			respuesta.put("mensaje", "Usuario encontrado exitosamente");
			respuesta.put("id_usuario", u.getId_usuario());
			respuesta.put("nombre", u.getNombre());
			respuesta.put("correo_electronico", u.getCorreo_electronico());
			respuesta.put("contrasena", u.getContrasena());
			respuesta.put("rol", u.getRol());
			respuesta.put("imagen_usuario", u.getImagen_usuario());
			return ResponseEntity.ok(respuesta);
		}
		Map<String, Object> error = new HashMap<>();
		error.put("status", HttpStatus.NOT_FOUND.value());
		error.put("error", "Usuario no encontrado");
		error.put("mensaje", "No se encontró ningún usuario con el ID especificado: " + searchId);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	// UT-05 (H-03, H-04, H-09)
	@PostMapping("/addUsuario")
	public ResponseEntity<Object> addUsuario(@RequestBody(required = false) Usuario usuario) {
		if (usuario == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Datos incompletos");
			error.put("mensaje", "El cuerpo de la solicitud no contiene datos de usuario");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-09: No permitir crear un registro con un ID ya existente
		if (usuario.getId_usuario() != null && usuariotable.existsById(usuario.getId_usuario())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.CONFLICT.value());
			error.put("error", "ID existente");
			error.put("mensaje", "Ya existe un usuario con el ID especificado");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
		}
		
		// H-03: Validación de formato de correo
		if (!esCorreoValido(usuario.getCorreo_electronico())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Formato de correo inválido");
			error.put("mensaje", "El correo electrónico ingresado no tiene un formato válido (ejemplo: usuario@dominio.com)");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-04: Validación de contraseña segura
		if (!esContrasenaSegura(usuario.getContrasena())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Contraseña insegura");
			error.put("mensaje", "La contraseña debe tener al minímo 6 caracteres y combinar letras y números");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		usuariotable.save(usuario);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Usuario registrado exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-06 (H-05, H-06, H-07, H-11)
	@RequestMapping(value = "/updateUsuario", method = {RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Object> updateUsuario(@RequestBody(required = false) Usuario usuario) {
		if (usuario == null || usuario.getId_usuario() == null) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Datos incompletos");
			error.put("mensaje", "Se requiere el ID del usuario para actualizar");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		// H-06: Verificar que el ID exista antes de modificar
		if (!usuariotable.existsById(usuario.getId_usuario())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Usuario no encontrado");
			error.put("mensaje", "No existe un usuario con el ID proporcionado");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		// H-03: Si actualiza correo, validar formato
		if (usuario.getCorreo_electronico() != null && !esCorreoValido(usuario.getCorreo_electronico())) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.BAD_REQUEST.value());
			error.put("error", "Formato de correo inválido");
			error.put("mensaje", "El correo electrónico ingresado no tiene un formato válido (ejemplo: usuario@dominio.com)");
			error.put("resultado", 0);
			return ResponseEntity.badRequest().body(error);
		}
		
		usuariotable.save(usuario);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Usuario actualizado exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-07 (H-07)
	@DeleteMapping("/deleteUsuario")
	public ResponseEntity<Object> deleteUsuario(@RequestBody(required = false) Usuario usuario,
	                                             @RequestParam(value = "id", required = false) Integer id,
	                                             @RequestParam(value = "id_usuario", required = false) Integer id_usuario) {
		Integer deleteId = null;
		if (usuario != null && usuario.getId_usuario() != null) {
			deleteId = usuario.getId_usuario();
		} else if (id != null) {
			deleteId = id;
		} else if (id_usuario != null) {
			deleteId = id_usuario;
		}
		
		if (deleteId == null || !usuariotable.existsById(deleteId)) {
			Map<String, Object> error = new HashMap<>();
			error.put("status", HttpStatus.NOT_FOUND.value());
			error.put("error", "Usuario no encontrado");
			error.put("mensaje", "No se encontró ningún usuario con el ID especificado para eliminar");
			error.put("resultado", 0);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
		}
		
		usuariotable.deleteById(deleteId);
		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("status", HttpStatus.OK.value());
		respuesta.put("mensaje", "Usuario eliminado exitosamente");
		respuesta.put("resultado", 1);
		return ResponseEntity.ok(respuesta);
	}
	
	// UT-08 (H-08)
	@RequestMapping(value = "/buscarUsuarios", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<List<Usuario>> getUsuarios() {
		List<Usuario> listaUsuarios = new ArrayList<>(); 
		usuariotable.findAll().forEach(listaUsuarios::add);
		return ResponseEntity.ok()
				.header("X-Mensaje", "Usuarios consultados exitosamente. Total: " + listaUsuarios.size())
				.body(listaUsuarios);
	}		
}
