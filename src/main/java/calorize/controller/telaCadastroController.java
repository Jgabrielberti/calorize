package calorize.controller;

import calorize.dao.UsuarioDAO;
import calorize.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * <p>Controlador da tela de cadastro de novos usuários, responsável por gerenciar
 * a entrada de dados do usuário e persistir as informações no sistema.</p>
 *
 * <p>Permite ao usuário inserir seus dados pessoais como nome, e-mail, senha, peso,
 * altura, gênero e meta de peso. Realiza validações básicas nos campos e interage
 * com {@link UsuarioDAO} para realizar o cadastro.</p>
 */
public class telaCadastroController {

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

    @FXML private Label mensagemLabel; // Rótulo para exibir mensagens ao usuário

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final SceneController sceneController = new SceneController();

    /**
     * <p>Manipula o evento de clique no botão de cadastro.</p>
     *
     * <p>Este método coleta todos os dados inseridos nos campos da interface,
     * realiza validações necessárias (campos vazios, formato de números, seleção de RadioButtons)
     * e, se os dados forem válidos, tenta cadastrar um novo {@link Usuario} utilizando o {@link UsuarioDAO}.</p>
     *
     * <p>Em caso de sucesso, exibe uma mensagem informativa e navega para a tela de login.
     * Em caso de erro (ex: dados inválidos ou e-mail já cadastrado), exibe uma mensagem de erro.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     */
    @FXML
    private void handleCadastrar(ActionEvent event) {
        try {
            String nome = nomeField.getText();
            String email = emailField.getText();
            String senha = senhaField.getText();

            // Validação de campos obrigatórios
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                showErro("Preencha todos os campos obrigatórios.");
                return;
            }

            int peso;
            int altura;
            try {
                // Tenta converter peso e altura para inteiros
                peso = Integer.parseInt(pesoField.getText());
                altura = Integer.parseInt(alturaField.getText());
            } catch (NumberFormatException e) {
                // Erro se peso ou altura não forem números válidos
                showErro("Peso e altura devem ser números inteiros.");
                return;
            }

            char generoSelecionado = '\0';
            if (genMasculino.isSelected()) generoSelecionado = 'M';
            else if (genFeminino.isSelected()) generoSelecionado = 'F';
            else if (genOutro.isSelected()) generoSelecionado = 'O';
            else {
                // Erro se nenhum gênero for selecionado
                showErro("Selecione um gênero.");
                return;
            }

            char metaSelecionada;
            if (metaPerderPeso.isSelected()) metaSelecionada = 'P';
            else if (metaManterPeso.isSelected()) metaSelecionada = 'M';
            else if (metaGanharPeso.isSelected()) metaSelecionada = 'G';
            else {
                // Erro se nenhuma meta for selecionada
                showErro("Selecione uma meta.");
                return;
            }

            // Cria uma nova instância de Usuário com os dados coletados
            Usuario usuario = new Usuario(nome, email, senha, peso, altura, generoSelecionado, metaSelecionada);

            // Tenta inserir o usuário no banco de dados
            boolean sucesso = usuarioDAO.inserir(usuario);
            if (sucesso) {
                showInfo("Usuário cadastrado com sucesso!");
                // Navega para a tela de login após o cadastro
                sceneController.switchScene(event, "/fxml/telaLogin.fxml");
            } else {
                // Erro se o e-mail já estiver cadastrado
                showErro("Erro: email já cadastrado.");
            }

        } catch (Exception e) {
            // Captura qualquer outra exceção inesperada durante o processo de cadastro
            showErro("Erro ao cadastrar: " + e.getMessage());
        }
    }

    /**
     * <p>Exibe uma caixa de diálogo de alerta com uma mensagem de erro e atualiza o rótulo de mensagem na interface.</p>
     *
     * @param mensagem A mensagem de erro a ser exibida.
     */
    private void showErro(String mensagem) {
        mensagemLabel.setText(mensagem); // Atualiza o rótulo na interface
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * <p>Exibe uma caixa de diálogo de alerta com uma mensagem informativa e atualiza o rótulo de mensagem na interface.</p>
     *
     * @param mensagem A mensagem informativa a ser exibida.
     */
    private void showInfo(String mensagem) {
        mensagemLabel.setText(mensagem); // Atualiza o rótulo na interface
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}