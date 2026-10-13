package br.com.univesp.econosave.config;

import br.com.univesp.econosave.model.*;
import br.com.univesp.econosave.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class TestConfig implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Override
    public void run(String... args) throws Exception {
        Usuario u1 = new Usuario();
        u1.setNome("João");
        u1.setEmail("joao@econosave.com");
        u1.setSenha("123456");
        usuarioRepository.save(u1);

        Categoria c1 = new Categoria();
        c1.setNome("Alimentação");
        categoriaRepository.save(c1);

        Categoria c2 = new Categoria();
        c2.setNome("Salário");
        categoriaRepository.save(c2);

        Movimentacao m1 = new Movimentacao();
        m1.setDescricao("Supermercado");
        m1.setValor(new BigDecimal("320.00"));
        m1.setTipo(TipoMovimentacao.DESPESA);
        m1.setData(LocalDate.now());
        m1.setUsuario(u1);
        m1.setCategoria(c1);
        movimentacaoRepository.save(m1);

        Movimentacao m2 = new Movimentacao();
        m2.setDescricao("Pagamento da Empresa");
        m2.setValor(new BigDecimal("4500.00"));
        m2.setTipo(TipoMovimentacao.RECEITA);
        m2.setData(LocalDate.now());
        m2.setUsuario(u1);
        m2.setCategoria(c2);
        movimentacaoRepository.save(m2);
    }
}