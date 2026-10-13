package br.com.univesp.econosave.repository;

import br.com.univesp.econosave.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
}