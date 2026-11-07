import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

// Executar com a aplicação parada, após o Hibernate criar as tabelas no destino.
class MigrarH2 {
    private static final List<String> TABELAS = List.of("categorias", "usuarios", "movimentacoes");
    private static final List<String> COLUNAS = List.of(
        "id,nome", "id,nome,email,senha", "id,descricao,valor,tipo,data,usuario_id,categoria_id");

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        Path local = Path.of("data/postgres-local.properties");
        if (Files.exists(local)) {
            try (var arquivo = Files.newBufferedReader(local)) { config.load(arquivo); }
        }
        for (String chave : List.of("DB_URL", "DB_USER", "DB_PASSWORD")) {
            if (System.getenv(chave) != null) config.setProperty(chave, System.getenv(chave));
        }
        try (var origem = DriverManager.getConnection(
                "jdbc:h2:file:./data/econosave;ACCESS_MODE_DATA=r;IFEXISTS=TRUE", "sa", "");
             var destino = DriverManager.getConnection(config.getProperty("DB_URL"),
                     config.getProperty("DB_USER"), config.getProperty("DB_PASSWORD"));
             var sql = destino.createStatement()) {
            destino.setAutoCommit(false);
            try {
                sql.execute("LOCK TABLE categorias, usuarios, movimentacoes IN ACCESS EXCLUSIVE MODE");
                try (var registros = sql.executeQuery("SELECT id FROM usuarios UNION ALL SELECT id FROM movimentacoes LIMIT 1")) {
                    if (registros.next()) throw new SQLException("O destino já contém registros de usuário.");
                }
                var categorias = new HashSet<String>();
                try (var registros = sql.executeQuery("SELECT nome FROM categorias")) {
                    while (registros.next()) {
                        if (!categorias.add(registros.getString(1))) throw new SQLException("Categoria duplicada no destino.");
                    }
                }
                if (!categorias.isEmpty() && !categorias.equals(Set.of(
                        "Alimentação", "Transporte", "Moradia", "Saúde", "Lazer", "Educação", "Salário", "Outros"))) {
                    throw new SQLException("O destino contém categorias personalizadas.");
                }
                sql.executeUpdate("DELETE FROM categorias");
                for (int i = 0; i < TABELAS.size(); i++) copiar(origem, destino, TABELAS.get(i), COLUNAS.get(i));
                destino.commit();
            } catch (Exception erro) {
                destino.rollback();
                throw erro;
            }
        }
        System.out.println("Migração confirmada.");
    }

    static void copiar(Connection origem, Connection destino, String tabela, String colunas) throws SQLException {
        int quantidade = colunas.split(",").length;
        String parametros = String.join(",", java.util.Collections.nCopies(quantidade, "?"));
        try (var consulta = origem.createStatement();
             var linhas = consulta.executeQuery("SELECT " + colunas + " FROM " + tabela + " ORDER BY id");
             var insert = destino.prepareStatement("INSERT INTO " + tabela + " (" + colunas + ") VALUES (" + parametros + ")");
             var sql = destino.createStatement()) {
            long proximoId = 1;
            long total = 0;
            while (linhas.next()) {
                for (int i = 1; i <= quantidade; i++) insert.setObject(i, linhas.getObject(i));
                insert.executeUpdate();
                proximoId = Math.max(proximoId, Math.addExact(linhas.getLong("id"), 1));
                total++;
            }
            sql.execute("ALTER TABLE " + tabela + " ALTER COLUMN id RESTART WITH " + proximoId);
            System.out.println(tabela + ": " + total);
        }
    }
}
