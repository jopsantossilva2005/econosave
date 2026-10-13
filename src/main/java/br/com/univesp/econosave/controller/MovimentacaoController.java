package br.com.univesp.econosave.controller;

import br.com.univesp.econosave.model.Movimentacao;
import br.com.univesp.econosave.repository.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class MovimentacaoController {

    @Autowired
    private MovimentacaoRepository repository;

    @GetMapping
    public List<Movimentacao> listarTodas() {
        return repository.findAll();
    }
    @PostMapping
    public ResponseEntity<Movimentacao> salvar(@RequestBody Movimentacao movimentacao) {
        Movimentacao novaMovimentacao = repository.save(movimentacao);
        return ResponseEntity.ok(novaMovimentacao);
    }
}