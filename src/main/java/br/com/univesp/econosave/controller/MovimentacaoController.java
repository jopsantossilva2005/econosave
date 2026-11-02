package br.com.univesp.econosave.controller;

import br.com.univesp.econosave.model.*;
import br.com.univesp.econosave.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class MovimentacaoController {
    private final MovimentacaoRepository movimentos;
    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;

    public MovimentacaoController(MovimentacaoRepository m, UsuarioRepository u, CategoriaRepository c) {
        movimentos = m;
        usuarios = u;
        categorias = c;
    }

    @GetMapping
    public List<MovimentoDTO> listar(HttpSession session) {
        return movimentos.findByUsuarioIdOrderByDataDescIdDesc(AuthController.usuarioId(session)).stream().map(this::dto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentoDTO criar(@Valid @RequestBody Entrada dados, HttpSession session) {
        Long usuarioId = AuthController.usuarioId(session);
        Movimentacao m = new Movimentacao();
        m.setUsuario(usuarios.findById(usuarioId).orElseThrow());
        preencher(m, dados);
        return dto(movimentos.save(m));
    }

    @PutMapping("/{id}")
    public MovimentoDTO editar(@PathVariable Long id, @Valid @RequestBody Entrada dados, HttpSession session) {
        Movimentacao m = buscar(id, session);
        preencher(m, dados);
        return dto(movimentos.save(m));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, HttpSession session) {
        movimentos.delete(buscar(id, session));
    }

    private Movimentacao buscar(Long id, HttpSession session) {
        return movimentos.findByIdAndUsuarioId(id, AuthController.usuarioId(session))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lançamento não encontrado."));
    }

    private void preencher(Movimentacao m, Entrada d) {
        Categoria categoria = categorias.findById(d.categoriaId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma categoria válida."));
        m.setDescricao(d.descricao().trim());
        m.setValor(d.valor());
        m.setData(d.data());
        m.setTipo(d.tipo());
        m.setCategoria(categoria);
    }

    private MovimentoDTO dto(Movimentacao m) {
        return new MovimentoDTO(m.getId(), m.getDescricao(), m.getValor(), m.getTipo(), m.getData(), m.getCategoria());
    }

    public record Entrada(@NotBlank @Size(max=120) String descricao,
                          @NotNull @DecimalMin("0.01") @Digits(integer=10, fraction=2) BigDecimal valor,
                          @NotNull TipoMovimentacao tipo, @NotNull LocalDate data, @NotNull Long categoriaId) {}

    public record MovimentoDTO(Long id, String descricao, BigDecimal valor, TipoMovimentacao tipo, LocalDate data, Categoria categoria) {}
}