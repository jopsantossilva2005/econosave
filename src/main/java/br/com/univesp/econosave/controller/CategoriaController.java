package br.com.univesp.econosave.controller;

import br.com.univesp.econosave.model.Categoria;
import br.com.univesp.econosave.repository.CategoriaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoriaController {
    private final CategoriaRepository categorias;

    public CategoriaController(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @GetMapping
    public List<Categoria> listar(HttpSession session) {
        AuthController.usuarioId(session);
        return categorias.findAll(Sort.by("nome"));
    }
}