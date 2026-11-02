package br.com.univesp.econosave.config;

import br.com.univesp.econosave.model.Categoria;
import br.com.univesp.econosave.repository.CategoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig implements CommandLineRunner {
    private final CategoriaRepository categorias;

    public TestConfig(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Override
    public void run(String... args) {
        for (String nome : new String[]{"Alimentação", "Transporte", "Moradia", "Saúde", "Lazer", "Educação", "Salário", "Outros"}) {
            if (categorias.findAll().stream().noneMatch(c -> c.getNome().equals(nome))) {
                Categoria c = new Categoria();
                c.setNome(nome);
                categorias.save(c);
            }
        }
    }
}