package com.irenewhub.backend.repository;

// ---- REPOSITORIO DE MENSAJES DE CONTACTO ----
// Interfaz que gestiona las consultas a la tabla contact_messages.
// No necesita metodos extra porque solo se usan operaciones basicas
// que JpaRepository ya incluye de serie sin necesidad de declararlas:
//   save(contactMessage)  -> INSERT en la tabla al recibir el formulario
//   findAll()             -> SELECT * para listar todos los mensajes en el panel admin
//   findById(id)          -> SELECT por id para ver un mensaje concreto

import com.irenewhub.backend.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // registra esta interfaz como componente de Spring para inyectarla con @Autowired
public interface ContactRepository extends JpaRepository<ContactMessage, Long> {
// ContactRepository solo necesita guardar y listar
// eso JpaRepository ya lo hace -> no hace falta declarar nada
}