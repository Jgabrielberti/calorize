package calorize.controller;

import calorize.dao.UsuarioDAO;
import calorize.model.Usuario;
import calorize.utils.Contexto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import java.util.Optional;

/**
 * <p>Controlador da tela de login da aplicação, responsável por autenticar o usuário.</p>
 *
 * <p>Gerencia a interação com os campos de e-mail e senha, valida as credenciais
 * informadas pelo usuário e, em caso de sucesso, define o usuário logado no {@link Contexto}
 * e navega para a tela principal. Também oferece a opção de navegar para a tela de cadastro.</p>
 */
public class telaLoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private Label mensagemLabel; // Rótulo para exibir mensagens ao usuário

    private static final String TELA_PRINCIPAL = "/fxml/telaPrincipal.fxml";
    private static final String TELA_CADASTRO = "/fxml/telaCadastro.fxml";

    private final SceneController sceneController = new SceneController();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * <p>Manipula o evento de clique no botão "Entrar".</p>
     *
     * <p>Este método coleta o e-mail e a senha dos campos de entrada, valida se estão preenchidos
     * e tenta buscar um {@link Usuario} correspondente no banco de dados através do {@link UsuarioDAO}.</p>
     *
     * <p>Em caso de credenciais válidas, o usuário é armazenado no {@link Contexto} e a aplicação
     * navega para a tela principal. Caso contrário, uma mensagem de erro é exibida na interface.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     */
    @FXML
    private void handleEntrar(ActionEvent event) {
        String email = emailField.getText().trim();
        String senha = senhaField.getText();

        // Valida se os campos estão vazios
        if (email.isEmpty() || senha.isEmpty()) {
            mensagemLabel.setText("Por favor, preencha todos os campos.");
            return;
        }

        try {
            // Tenta buscar o usuário no banco de dados
            Optional<Usuario> usuario = usuarioDAO.buscarPorEmailESenha(email, senha);

            if (usuario != null) {
                // Se o usuário for encontrado, define-o no contexto global
                Contexto.setUsuarioLogado(usuario);
                // Navega para a tela principal
                sceneController.switchScene(event, TELA_PRINCIPAL);
            } else {
                // Credenciais incorretas
                mensagemLabel.setText("Email ou senha incorretos.");
            }
        } catch (Exception e) {
            // Em caso de erro no acesso ao banco de dados ou outra exceção
            mensagemLabel.setText("Erro ao tentar logar. Tente novamente.");
            System.err.println("Erro ao acessar o banco de dados: " + e.getMessage());
        }
    }

    /**
     * <p>Manipula o evento de clique no botão "Cadastrar".</p>
     *
     * <p>Este método é responsável por navegar o usuário para a tela de cadastro,
     * permitindo que novos usuários se registrem na aplicação.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     */
    @FXML
    private void handleCadastrar(ActionEvent event) {
        // Navega para a tela de cadastro
        sceneController.switchScene(event, TELA_CADASTRO);
    }
}