package br.com.univesp.econosave.repository;

import br.com.univesp.econosave.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}