package calorize.dao;

import calorize.model.Alimento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para operações relacionadas a refeições no banco de dados.
 * <p>
 * Gerencia a persistência de dados de refeições e alimentos associados.
 * </p>
 */
public class RefeicaoDAO {

    private final String url = "jdbc:sqlite:calorize.data/usuarios.db";

    /**
     * Adiciona um alimento a uma refeição do usuário.
     *
     * @param usuarioId ID do usuário
     * @param tipoRefeicao Tipo da refeição (CAFE_DA_MANHA, ALMOCO, etc.)
     * @param alimento Alimento a ser adicionado
     * @return true se a operação foi bem-sucedida, false caso contrário
     * @throws SQLException Se ocorrer um erro de banco de dados
     */
    public boolean adicionarAlimento(int usuarioId, String tipoRefeicao, Alimento alimento) throws SQLException {
        String insertRefeicao = "INSERT INTO refeicoes(usuario_id, tipo, alimento_nome, calorias, proteinas, carboidratos, gorduras, fibras, calcio, data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, date('now'))";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(insertRefeicao)) {

            pstmt.setInt(1, usuarioId);
            pstmt.setString(2, tipoRefeicao);
            pstmt.setString(3, alimento.getNome());
            pstmt.setDouble(4, alimento.getCalorias());
            pstmt.setDouble(5, alimento.getProteinas());
            pstmt.setDouble(6, alimento.getCarboidratos());
            pstmt.setDouble(7, alimento.getGorduras());
            pstmt.setDouble(8, alimento.getFibras());
            pstmt.setDouble(9, alimento.getCalcio());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao adicionar alimento: " + e.getMessage());
            throw e;
        }
    }

    /**
     * <p>Recupera os alimentos de uma refeição específica de um usuário em uma determinada calorize.data.</p>
     * <p>Este método foi renomeado de `getAlimentosPorTipoEData` para melhor clareza e
     * para corresponder à chamada no `telaPrincipalController`.</p>
     *
     * @param usuarioId ID do usuário
     * @param tipo Tipo da refeição (e.g., "cafe", "almoco", "jantar", "lanches")
     * @param data Data no formato 'YYYY-MM-DD'
     * @return Lista de alimentos encontrados (lista vazia se nenhum encontrado)
     * @throws SQLException Se ocorrer um erro de banco de dados
     */
    public List<Alimento> buscarAlimentosPorRefeicao(int usuarioId, String tipo, String data) throws SQLException {
        List<Alimento> alimentos = new ArrayList<>();

        String query = "SELECT alimento_nome, calorias, proteinas, carboidratos, gorduras, fibras, calcio " +
                "FROM refeicoes WHERE usuario_id = ? AND tipo = ? AND data = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, usuarioId);
            pstmt.setString(2, tipo);
            pstmt.setString(3, data);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Alimento alimento = new Alimento(
                            0, // ID não disponível na consulta ou pode ser um campo nulo no construtor
                            rs.getString("alimento_nome"),
                            null, // categoria não armazenada na tabela refeicoes
                            rs.getDouble("calorias"),
                            rs.getDouble("proteinas"),
                            rs.getDouble("gorduras"),
                            rs.getDouble("carboidratos"),
                            rs.getDouble("fibras"),
                            rs.getDouble("calcio")
                    );
                    alimentos.add(alimento);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar alimentos: " + e.getMessage());
            throw e;
        }

        return alimentos;
    }

    /**
     * Busca os alimentos mais recentes registrados por um usuário.
     *
     * @param usuarioId ID do usuário
     * @param limite Número máximo de alimentos a retornar
     * @return Lista de alimentos recentes (lista vazia se nenhum encontrado)
     * @throws SQLException Se ocorrer um erro de banco de dados
     */
    public List<Alimento> buscarAlimentosRecentes(int usuarioId, int limite) throws SQLException {
        List<Alimento> alimentos = new ArrayList<>();

        String sql = "SELECT alimento_nome, calorias, proteinas, carboidratos, gorduras, fibras, calcio " +
                "FROM refeicoes WHERE usuario_id = ? " +
                "ORDER BY id DESC LIMIT ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            pstmt.setInt(2, limite);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Alimento alimento = new Alimento(
                            0, // ID não disponível na consulta
                            rs.getString("alimento_nome"),
                            null, // categoria não armazenada
                            rs.getDouble("calorias"),
                            rs.getDouble("proteinas"),
                            rs.getDouble("gorduras"),
                            rs.getDouble("carboidratos"),
                            rs.getDouble("fibras"),
                            rs.getDouble("calcio")
                    );
                    alimentos.add(alimento);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar alimentos recentes: " + e.getMessage());
            throw e;
        }

        return alimentos;
    }
}