package calorize.controller;

import calorize.dao.UsuarioDAO;
import calorize.model.Usuario;
import calorize.utils.Contexto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Optional; // Import java.util.Optional

/**
 * <p>Controlador da tela de edição de informações do usuário, permitindo ao usuário logado
 * atualizar seus dados pessoais como nome, e-mail, senha, peso, altura, gênero e meta de peso.</p>
 *
 * <p>Esta classe gerencia a interface de usuário para a edição de perfil, valida as entradas
 * do usuário e interage com {@link UsuarioDAO} para persistir as alterações no banco de dados.</p>
 *
 * <p>As alterações de peso também são registradas no histórico do usuário.</p>
 */
public class telaEditarInformacoesController {

    @FXML private TextField nomeField;
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private TextField pesoField;
    @FXML private TextField alturaField;

    @FXML private RadioButton genMasculino;
    @FXML private RadioButton genFeminino;
    @FXML private RadioButton genOutro;
    @FXML private ToggleGroup genero; // Grupo para os RadioButtons de gênero

    @FXML private RadioButton metaPerderPeso;
    @FXML private RadioButton metaManterPeso;
    @FXML private RadioButton metaGanharPeso;
    @FXML private ToggleGroup meta; // Grupo para os RadioButtons de meta

    private final UsuarioDAO usuarioDAO = UsuarioDAO.getInstance(); // Ensure you're using getInstance()
    private final SceneController sceneController = new SceneController();

    /**
     * <p>Manipula o evento de atualização dos dados do usuário logado.</p>
     *
     * <p>Este método coleta as informações dos campos da interface. Ele verifica se os campos
     * de texto foram preenchidos para determinar quais informações devem ser atualizadas.
     * Campos de peso e altura são validados para garantir que sejam números inteiros.
     * Gênero e meta de peso são atualizados com base nos {@link RadioButton RadioButtons} selecionados.</p>
     *
     * <p>Após a coleta e validação, as informações são persistidas utilizando {@link UsuarioDAO#atualizar(Usuario)}.
     * Se o peso for alterado, um registro de peso é adicionado através de {@link UsuarioDAO#adicionarPeso(int, double)}.</p>
     *
     * <p>Em caso de sucesso, o usuário é redirecionado para a tela principal; caso contrário,
     * uma mensagem de erro é exibida.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     */
    @FXML
    private void atualizaDadosUsuario(ActionEvent event) {
        // Get the Optional from Contexto
        Optional<Usuario> optionalUsuario = Contexto.getUsuarioLogado();

        // Check if a user is present in the Optional
        if (optionalUsuario.isEmpty()) {
            showErro("Nenhum usuário logado. Por favor, faça login novamente.");
            // Optionally, redirect to login screen or disable controls
            return;
        }

        Usuario usuario = optionalUsuario.get(); // Get the User object from the Optional

        // Atualiza o nome se o campo não estiver vazio
        String novoNome = nomeField.getText().trim();
        if (!novoNome.isEmpty()) {
            try {
                usuario.setNome(novoNome);
            } catch (IllegalArgumentException e) {
                showErro(e.getMessage());
                return;
            }
        }

        // Atualiza o e-mail se o campo não estiver vazio
        String novoEmail = emailField.getText().trim();
        if (!novoEmail.isEmpty()) {
            try {
                usuario.setEmail(novoEmail);
            } catch (IllegalArgumentException e) {
                showErro(e.getMessage());
                return;
            }
        }

        // Atualiza a senha se o campo não estiver vazio
        String novaSenha = senhaField.getText().trim();
        if (!novaSenha.isEmpty()) {
            try {
                usuario.setSenha(novaSenha);
            } catch (IllegalArgumentException e) {
                showErro(e.getMessage());
                return;
            }
        }

        // Tenta atualizar o peso e adicionar ao histórico de peso
        String pesoTexto = pesoField.getText().trim();
        if (!pesoTexto.isEmpty()) {
            try {
                double novoPeso = Double.parseDouble(pesoTexto);
                usuario.adicionarPeso(novoPeso); // Update weight in memory and add to history
                usuarioDAO.adicionarPeso(usuario.getId(), novoPeso); // Add to database history
            } catch (NumberFormatException e) {
                showErro("Peso inválido. Digite apenas números.");
                return;
            } catch (IllegalArgumentException e) { // Catch potential validation errors from adicionarPeso
                showErro(e.getMessage());
                return;
            }
        }

        // Tenta atualizar a altura
        String alturaTexto = alturaField.getText().trim();
        if (!alturaTexto.isEmpty()) {
            try {
                int novaAltura = Integer.parseInt(alturaTexto);
                usuario.setAlturaCm(novaAltura);
            } catch (NumberFormatException e) {
                showErro("Altura inválida. Digite apenas números inteiros.");
                return;
            } catch (IllegalArgumentException e) { // Catch potential validation errors from setAlturaCm
                showErro(e.getMessage());
                return;
            }
        }

        // Atualiza o gênero com base na seleção do RadioButton
        // Make sure to set the gender even if not explicitly selected, based on current user's gender
        // This ensures the value is passed correctly even if the user doesn't interact with these buttons
        char generoChar = 'O'; // Default to 'Outro'
        if (genMasculino.isSelected()) generoChar = 'M';
        else if (genFeminino.isSelected()) generoChar = 'F';
        usuario.setGenero(generoChar);


        // Atualiza a meta de peso com base na seleção do RadioButton
        // Similar to gender, ensure a default or current value is used if no radio button is selected
        char metaChar = 'M'; // Default to 'Manter'
        if (metaPerderPeso.isSelected()) metaChar = 'P';
        else if (metaGanharPeso.isSelected()) metaChar = 'G';
        usuario.setMeta(metaChar);


        // Tenta atualizar o usuário no banco de dados
        try {
            boolean sucesso = usuarioDAO.atualizar(usuario);
            if (sucesso) {
                showInfo("Informações atualizadas com sucesso!");
                // Redireciona para a tela principal após a atualização
                sceneController.switchScene(event, "/fxml/telaPrincipal.fxml");
            } else {
                showErro("Erro ao atualizar usuário. Verifique se o e-mail já está em uso ou outros dados são inválidos.");
            }
        } catch (UsuarioDAO.DataAccessException e) {
            showErro("Erro no banco de dados ao atualizar informações: " + e.getMessage());
            // Log the exception for debugging
            e.printStackTrace();
        }
    }

    /**
     * <p>Manipula o evento de voltar para a tela principal da aplicação.</p>
     *
     * <p>Redireciona o usuário para a tela principal sem salvar as alterações não confirmadas.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     * @throws IOException Se houver um erro ao carregar o arquivo FXML da tela principal.
     */
    @FXML
    private void voltarParaTelaPrincipal(ActionEvent event) throws IOException {
        sceneController.switchScene(event, "/fxml/telaPrincipal.fxml");
    }

    /**
     * <p>Exibe uma caixa de diálogo de alerta com uma mensagem de erro.</p>
     *
     * @param mensagem A mensagem de erro a ser exibida.
     */
    private void showErro(String mensagem) {
        showMensagem("Erro", mensagem, Alert.AlertType.ERROR);
    }

    /**
     * <p>Exibe uma caixa de diálogo de alerta com uma mensagem informativa de sucesso.</p>
     *
     * @param mensagem A mensagem informativa a ser exibida.
     */
    private void showInfo(String mensagem) {
        showMensagem("Sucesso", mensagem, Alert.AlertType.INFORMATION);
    }

    /**
     * <p>Exibe uma caixa de diálogo de alerta genérica com título, mensagem e tipo de alerta especificados.</p>
     *
     * @param titulo O título da caixa de diálogo.
     * @param mensagem A mensagem a ser exibida na caixa de diálogo.
     * @param tipo O tipo de alerta (e.g., {@link Alert.AlertType#INFORMATION}, {@link Alert.AlertType#ERROR}).
     */
    private void showMensagem(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}