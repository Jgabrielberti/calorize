package calorize.dao;

import calorize.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) for user-related database operations.
 * <p>
 * This class follows the singleton pattern to ensure a single instance manages
 * all database connections. It provides CRUD operations for users, friend management,
 * and weight tracking functionality.
 * </p>
 */
public final class UsuarioDAO {
    private static final Logger LOGGER = Logger.getLogger(UsuarioDAO.class.getName());
    private static final String URL = "jdbc:sqlite:calorize.data/usuarios.db";

    private static UsuarioDAO instance;

    /**
     * Private constructor to enforce singleton pattern.
     */
    public UsuarioDAO() {
        criarTabelasSeNaoExistirem();
    }

    /**
     * Gets the singleton instance of UsuarioDAO.
     *
     * @return the singleton instance
     */
    public static synchronized UsuarioDAO getInstance() {
        if (instance == null) {
            instance = new UsuarioDAO();
        }
        return instance;
    }

    /**
     * Creates the required database tables if they don't exist.
     * <p>
     * Creates three tables:
     * <ul>
     *   <li>usuarios - Stores user information</li>
     *   <li>amigos - Manages friend relationships</li>
     *   <li>pesos - Tracks user weight history</li>
     * </ul>
     * </p>
     * @throws DataAccessException if there's an error creating the tables
     */
    private void criarTabelasSeNaoExistirem() {
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "senha TEXT NOT NULL, " +
                "peso REAL NOT NULL, " +
                "altura INTEGER NOT NULL, " +
                "genero TEXT, " +
                "meta TEXT NOT NULL)";

        String sqlAmigos = "CREATE TABLE IF NOT EXISTS amigos (" +
                "usuario_id INTEGER NOT NULL, " +
                "amigo_id INTEGER NOT NULL, " +
                "PRIMARY KEY (usuario_id, amigo_id), " +
                "FOREIGN KEY (usuario_id) REFERENCES usuarios(id), " +
                "FOREIGN KEY (amigo_id) REFERENCES usuarios(id))";

        String sqlPesos = "CREATE TABLE IF NOT EXISTS pesos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER NOT NULL, " +
                "peso REAL NOT NULL, " +
                "data TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (usuario_id) REFERENCES usuarios(id))";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlAmigos);
            stmt.execute(sqlPesos);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao criar tabelas", e);
            throw new DataAccessException("Falha ao inicializar banco de dados", e);
        }
    }

    /**
     * Inserts a new user into the database.
     *
     * @param usuario the user to insert
     * @return true if insertion was successful, false otherwise
     * @throws DataAccessException if there's a database error
     */
    public boolean inserir(Usuario usuario) {
        String sql = "INSERT INTO usuarios(nome, email, senha, peso, altura, genero, meta) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setUsuarioParameters(stmt, usuario);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getInt(1));
                }
            }

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao inserir usuário", e);
            throw new DataAccessException("Falha ao inserir usuário", e);
        }
    }

    /**
     * Finds a user by email and password.
     *
     * @param email the user's email
     * @param senha the user's password
     * @return an Optional containing the user if found, empty otherwise
     * @throws DataAccessException if there's a database error
     */
    public Optional<Usuario> buscarPorEmailESenha(String email, String senha) {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND senha = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar usuário por email e senha", e);
            throw new DataAccessException("Falha ao buscar usuário", e);
        }

        return Optional.empty();
    }

    /**
     * Retrieves a user's friends.
     *
     * @param usuarioId the user's ID
     * @return list of friends (empty if none)
     * @throws DataAccessException if there's a database error
     */
    public List<Usuario> buscarAmigos(int usuarioId) {
        List<Usuario> amigos = new ArrayList<>();
        String sql = "SELECT u.* FROM usuarios u JOIN amigos a ON u.id = a.amigo_id WHERE a.usuario_id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    amigos.add(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar amigos", e);
            throw new DataAccessException("Falha ao buscar amigos", e);
        }

        return amigos;
    }

    /**
     * Searches for non-friend users by name.
     *
     * @param usuarioId the current user's ID
     * @param nome name to search for
     * @return list of matching non-friend users
     * @throws DataAccessException if there's a database error
     */
    public List<Usuario> buscarUsuariosNaoAmigosPorNome(int usuarioId, String nome) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE nome LIKE ? AND id != ? AND id NOT IN (" +
                "SELECT amigo_id FROM amigos WHERE usuario_id = ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");
            stmt.setInt(2, usuarioId);
            stmt.setInt(3, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar usuários não amigos", e);
            throw new DataAccessException("Falha ao buscar usuários não amigos", e);
        }

        return usuarios;
    }

    /**
     * Adds a friend relationship between two users.
     *
     * @param usuarioId the user's ID
     * @param amigoId the friend's ID
     * @return true if the operation was successful
     * @throws IllegalArgumentException if user tries to add themselves as friend
     * @throws DataAccessException if there's a database error
     */
    public boolean adicionarAmigo(int usuarioId, int amigoId) {
        if (usuarioId == amigoId) {
            throw new IllegalArgumentException("Um usuário não pode ser amigo de si mesmo");
        }

        String sql = "INSERT INTO amigos(usuario_id, amigo_id) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setInt(2, amigoId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar amigo", e);
            throw new DataAccessException("Falha ao adicionar amigo", e);
        }
    }

    /**
     * Removes a friend relationship between two users.
     *
     * @param usuarioId the user's ID
     * @param amigoId the friend's ID
     * @return true if the operation was successful
     * @throws DataAccessException if there's a database error
     */
    public boolean removerAmigo(int usuarioId, int amigoId) {
        String sql = "DELETE FROM amigos WHERE usuario_id = ? AND amigo_id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setInt(2, amigoId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao remover amigo", e);
            throw new DataAccessException("Falha ao remover amigo", e);
        }
    }

    /**
     * Updates a user's information in the database.
     *
     * @param usuario the user with updated information
     * @return true if the update was successful
     * @throws DataAccessException if there's a database error
     */
    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, email = ?, senha = ?, peso = ?, altura = ?, " +
                "genero = ?, meta = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setUsuarioParameters(stmt, usuario);
            stmt.setInt(8, usuario.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar usuário", e);
            throw new DataAccessException("Falha ao atualizar usuário", e);
        }
    }

    /**
     * Adds a weight entry for a user.
     *
     * @param usuarioId the user's ID
     * @param peso the weight value to add
     * @throws DataAccessException if there's a database error
     */
    public void adicionarPeso(int usuarioId, double peso) {
        String sql = "INSERT INTO pesos (usuario_id, peso) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setDouble(2, peso);

            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar peso", e);
            throw new DataAccessException("Falha ao adicionar peso", e);
        }
    }

    /**
     * Retrieves a user's weight history.
     *
     * @param usuarioId the user's ID
     * @return list of weights in chronological order
     * @throws DataAccessException if there's a database error
     */
    public List<Double> getPesosPorUsuario(int usuarioId) {
        List<Double> pesos = new ArrayList<>();
        String sql = "SELECT peso FROM pesos WHERE usuario_id = ? ORDER BY data ASC";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pesos.add(rs.getDouble("peso"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar pesos", e);
            throw new DataAccessException("Falha ao buscar pesos", e);
        }

        return pesos;
    }

    /**
     * Helper method to set user parameters on a PreparedStatement.
     */
    private void setUsuarioParameters(PreparedStatement stmt, Usuario usuario) throws SQLException {
        stmt.setString(1, usuario.getNome());
        stmt.setString(2, usuario.getEmail());
        stmt.setString(3, usuario.getSenha());
        stmt.setDouble(4, usuario.getPesoAtual());
        stmt.setInt(5, usuario.getAlturaCm());
        stmt.setString(6, usuario.getGenero() != null ? String.valueOf(usuario.getGenero()) : null);
        stmt.setString(7, String.valueOf(usuario.getMeta()));
    }

    /**
     * Maps a ResultSet row to a Usuario object.
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("senha"),
                rs.getDouble("peso"),
                rs.getInt("altura"),
                rs.getString("genero") != null ? rs.getString("genero").charAt(0) : null,
                rs.getString("meta").charAt(0)
        );
        usuario.setId(rs.getInt("id"));
        return usuario;
    }

    /**
     * Custom exception for calorize.data access errors.
     */
    public static class DataAccessException extends RuntimeException {
        public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}