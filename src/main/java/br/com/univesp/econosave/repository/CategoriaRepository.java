package br.com.univesp.econosave.repository;

import br.com.univesp.econosave.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}