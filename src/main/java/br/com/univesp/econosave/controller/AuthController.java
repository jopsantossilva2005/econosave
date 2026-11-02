package br.com.univesp.econosave.controller;

import br.com.univesp.econosave.model.Usuario;
import br.com.univesp.econosave.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioRepository usuarios;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public AuthController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDTO cadastrar(@Valid @RequestBody RegisterDTO dados) {
        validarSenha(dados.senha());
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.findByEmail(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(encoder.encode(dados.senha()));
        return dto(usuarios.save(usuario));
    }

    @PostMapping("/login")
    public UsuarioDTO login(@Valid @RequestBody LoginDTO dados, HttpServletRequest request) {
        validarSenha(dados.senha());
        Usuario usuario = usuarios.findByEmail(dados.email().trim().toLowerCase(Locale.ROOT))
                .filter(u -> encoder.matches(dados.senha(), u.getSenha()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos."));
        HttpSession anterior = request.getSession(false);
        if (anterior != null) anterior.invalidate();
        request.getSession(true).setAttribute("usuarioId", usuario.getId());
        return dto(usuario);
    }

    @GetMapping("/me")
    public UsuarioDTO me(HttpSession session) {
        return dto(usuarios.findById(usuarioId(session)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Entre na sua conta.")));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    public static Long usuarioId(HttpSession session) {
        Object id = session.getAttribute("usuarioId");
        if (!(id instanceof Long)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Entre na sua conta.");
        return (Long) id;
    }

    private static void validarSenha(String senha) {
        if (senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha muito longa. Use até 72 bytes em UTF-8; caracteres acentuados podem ocupar mais de um byte.");
    }

    private static UsuarioDTO dto(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getNome(), u.getEmail());
    }

    public record UsuarioDTO(Long id, String nome, String email) {}
    public record LoginDTO(@NotBlank @Email String email, @NotBlank @Size(max=72) String senha) {}
    public record RegisterDTO(@NotBlank @Size(max=100) String nome, @NotBlank @Email @Size(max=254) String email,
                              @NotBlank @Size(min=8, max=72) String senha) {}
}