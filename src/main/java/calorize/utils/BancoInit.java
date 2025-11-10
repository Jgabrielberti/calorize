package calorize.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Classe utilitária para inicialização do banco de dados SQLite do sistema Calorize.
 * <p>
 * Responsável pela criação das tabelas necessárias para operação do sistema.
 * </p>
 */
public class BancoInit {

    /**
     * Cria todas as tabelas necessárias no banco de dados SQLite.
     * <p>
     * Método seguro para execução múltipla (usa CREATE TABLE IF NOT EXISTS).
     * Cria as seguintes tabelas:
     * </p>
     * <ul>
     * <li>usuarios - Armazena dados dos usuários</li>
     * <li>refeicoes - Registro de refeições dos usuários</li>
     * <li>alimentos - Catálogo de alimentos com informações nutricionais</li>
     * <li>refeicao_alimento - Relacionamento entre refeições e alimentos</li>
     * <li>amigos - Relacionamentos de amizade entre usuários</li>
     * <li>pesos - Histórico de pesos dos usuários</li>
     * </ul>
     *
     * @throws RuntimeException Se ocorrer um erro durante a criação das tabelas
     */
    public static void criarTabelas() {
        try {
            // Tenta carregar o driver SQLite
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            // Se o driver não for encontrado, lança uma exceção de tempo de execução
            throw new RuntimeException("Driver SQLite não encontrado. Verifique as dependências no pom.xml e a configuração do seu ambiente.", e);
        }

        // Caminho relativo para o banco de dados (certifique-se que a pasta 'calorize.data' exista)
        final String url = "jdbc:sqlite:calorize.data/usuarios.db";

        // SQL para criação das tabelas
        final String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "altura INTEGER NOT NULL," +
                "genero CHAR(1)," +
                "meta CHAR(1) NOT NULL," +
                "senha TEXT NOT NULL" + // Adicionando campo senha, importante para autenticação
                ");";

        final String sqlRefeicoes = "CREATE TABLE IF NOT EXISTS refeicoes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario_id INTEGER NOT NULL," +
                "tipo TEXT NOT NULL," +
                "data TEXT NOT NULL," +
                "FOREIGN KEY(usuario_id) REFERENCES usuarios(id)" +
                ");";

        final String sqlAlimentos = "CREATE TABLE IF NOT EXISTS alimentos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "descricao TEXT NOT NULL," +
                "categoria TEXT," +
                "calorias REAL," +
                "proteinas REAL," +
                "gorduras REAL," +
                "carboidratos REAL," +
                "fibras REAL," +
                "calcio REAL" +
                ");";

        final String sqlRefeicaoAlimento = "CREATE TABLE IF NOT EXISTS refeicao_alimento (" +
                "refeicao_id INTEGER NOT NULL," +
                "alimento_id INTEGER NOT NULL," +
                "quantidade REAL NOT NULL," +
                "PRIMARY KEY(refeicao_id, alimento_id)," + // Adicionando PRIMARY KEY composta
                "FOREIGN KEY(refeicao_id) REFERENCES refeicoes(id) ON DELETE CASCADE," + // Adicionando ON DELETE CASCADE
                "FOREIGN KEY(alimento_id) REFERENCES alimentos(id) ON DELETE CASCADE" +   // Adicionando ON DELETE CASCADE
                ");";

        final String sqlAmigos = "CREATE TABLE IF NOT EXISTS amigos (" +
                "usuario_id INTEGER NOT NULL," +
                "amigo_id INTEGER NOT NULL," +
                "PRIMARY KEY (usuario_id, amigo_id)," +
                "FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE," +
                "FOREIGN KEY(amigo_id) REFERENCES usuarios(id) ON DELETE CASCADE" +
                ");";

        final String sqlPesos = "CREATE TABLE IF NOT EXISTS pesos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario_id INTEGER NOT NULL," +
                "peso REAL NOT NULL," +
                "data TEXT DEFAULT (date('now'))," + // Mudando para função SQLite padrão de calorize.data
                "FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE" +
                ");";

        try (Connection conn = DriverManager.getConnection(url); // Conecta uma única vez
             Statement stmt = conn.createStatement()) {

            // Executa todos os comandos SQL
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlRefeicoes);
            stmt.execute(sqlAlimentos);
            stmt.execute(sqlRefeicaoAlimento);
            stmt.execute(sqlAmigos);
            stmt.execute(sqlPesos);

            System.out.println("Tabelas criadas ou já existentes.");
        } catch (Exception e) {
            System.err.println("Erro ao criar tabelas: " + e.getMessage());
            // Lança uma RuntimeException se houver um problema na conexão ou execução do SQL
            throw new RuntimeException("Falha na inicialização do banco de dados", e);
        }
    }
}