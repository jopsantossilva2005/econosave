package br.com.univesp.econosave.repository;

import br.com.univesp.econosave.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
    List<Movimentacao> findByUsuarioIdOrderByDataDescIdDesc(Long usuarioId);
    Optional<Movimentacao> findByIdAndUsuarioId(Long id, Long usuarioId);
}