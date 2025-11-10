package calorize.controller;

import calorize.dao.RefeicaoDAO;
import calorize.model.Alimento;
import calorize.model.Usuario; 
import calorize.utils.Contexto;
import calorize.utils.LeitorDeAlimentos;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.SelectionMode;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional; 
import java.util.stream.Collectors;

/**
 * <p>Controlador da tela de adição de alimentos, responsável por gerenciar a interação do usuário
 * com a interface para buscar e selecionar alimentos.</p>
 *
 * <p>Permite ao usuário buscar alimentos por nome, visualizar alimentos recentes adicionados
 * e adicionar alimentos selecionados à refeição atual do usuário logado.</p>
 *
 * <p>Utiliza {@link Contexto} para obter informações do usuário logado e do tipo de refeição
 * sendo manipulada, e {@link RefeicaoDAO} para persistir os dados dos alimentos.</p>
 */
public class telaAdicionarAlimentoController {

    @FXML private TextField buscarAlimento;
    @FXML private ListView<String> listAlimentosNaoRecentes;
    @FXML private ListView<String> listAlimentosRecentes;

    private List<Alimento> todosAlimentos;

    /**
     * <p>Método de inicialização do controlador, chamado automaticamente após o carregamento do FXML.</p>
     *
     * <p>Responsável por carregar a lista de todos os alimentos a partir de um arquivo JSON,
     * configurar o listener para a barra de busca, configurar o modo de seleção da lista de
     * alimentos recentes e carregar os alimentos adicionados recentemente pelo usuário.</p>
     */
    @FXML
    private void initialize() {
        // Carrega todos os alimentos de um arquivo JSON
        todosAlimentos = LeitorDeAlimentos.carregarAlimentos("data/taco.json");

        if (todosAlimentos == null || todosAlimentos.isEmpty()) {
            showError("Erro ao carregar alimentos ou arquivo taco.json está vazio.");
            
            buscarAlimento.setDisable(true);
            listAlimentosNaoRecentes.setDisable(true);
            listAlimentosRecentes.setDisable(true);
            return;
        }

        // Inicializa a lista de alimentos não recentes (resultado da busca)
        listAlimentosNaoRecentes.setItems(FXCollections.observableArrayList());
        // Configura a lista de alimentos recentes para permitir múltiplas seleções
        listAlimentosRecentes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Adiciona um listener para a propriedade de texto do campo de busca
        buscarAlimento.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                // Limpa a lista se o campo de busca estiver vazio
                listAlimentosNaoRecentes.getItems().clear();
            } else {
                // Filtra os alimentos com base no texto digitado e atualiza a lista
                List<String> filtrados = todosAlimentos.stream()
                        .map(Alimento::getNome)
                        .filter(nome -> nome.toLowerCase().contains(newText.toLowerCase()))
                        .collect(Collectors.toList());

                listAlimentosNaoRecentes.setItems(FXCollections.observableArrayList(filtrados));
            }
        });

        // Configura o manipulador de clique para a lista de alimentos não recentes
        listAlimentosNaoRecentes.setOnMouseClicked(this::handleSelecionarAlimento);
        // Carrega os alimentos recentes do usuário
        carregarAlimentosRecentes();
    }

    /**
     * <p>Carrega e exibe na {@link ListView} os alimentos recentemente adicionados pelo usuário logado.</p>
     *
     * <p>Busca até 10 alimentos mais recentes associados ao usuário logado no banco de dados
     * e os exibe na lista de alimentos recentes. Em caso de erro (usuário não logado ou falha
     * na busca), uma mensagem de erro é exibida.</p>
     */
    private void carregarAlimentosRecentes() {
        Optional<Usuario> optionalUsuario = Contexto.getUsuarioLogado(); // Get the Optional

        if (optionalUsuario.isEmpty()) { // Check if user is present
            showError("Nenhum usuário logado para carregar alimentos recentes.");
            listAlimentosRecentes.setItems(FXCollections.observableArrayList()); // Clear list
            return;
        }

        Usuario usuario = optionalUsuario.get(); // Get the Usuario object

        RefeicaoDAO refeicaoDAO = new RefeicaoDAO();
        List<Alimento> recentes = null;
        try {
            recentes = refeicaoDAO.buscarAlimentosRecentes(usuario.getId(), 10);
        } catch (SQLException e) {
            showError("Erro ao carregar alimentos recentes do banco de dados: " + e.getMessage());
            System.err.println("SQL Exception: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return; // Exit method on DB error
        } catch (Exception e) { // Catch any other unexpected exceptions
            showError("Erro inesperado ao carregar alimentos recentes: " + e.getMessage());
            System.err.println("General Exception: " + e.getMessage());
            e.printStackTrace();
            return; // Exit method on unexpected error
        }

        if (recentes == null || recentes.isEmpty()) {
            listAlimentosRecentes.setItems(FXCollections.observableArrayList()); // Clear list if no recent foods
            // showInfo("Nenhum alimento recente encontrado."); // Optional: inform user
            return;
        }

        // Mapeia a lista de Alimento para uma lista de nomes distintos de alimentos
        List<String> nomes = recentes.stream()
                .map(Alimento::getNome)
                .distinct() // Garante que não haja nomes duplicados
                .collect(Collectors.toList());

        listAlimentosRecentes.setItems(FXCollections.observableArrayList(nomes));
    }

    /**
     * <p>Adiciona os alimentos selecionados da lista de "Alimentos Recentes" à refeição atual do usuário.</p>
     *
     * <p>Este método é chamado quando o botão de adição de alimentos recentes é acionado.
     * Ele recupera os alimentos selecionados, o usuário logado e o tipo de refeição do {@link Contexto},
     * e os adiciona à base de dados através de {@link RefeicaoDAO}.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     */
    @FXML
    private void adicionarAlimentosRecentes(ActionEvent event) {
        List<String> selecionados = listAlimentosRecentes.getSelectionModel().getSelectedItems();
        if (selecionados == null || selecionados.isEmpty()) {
            showError("Selecione pelo menos um alimento para adicionar.");
            return;
        }

        Optional<Usuario> optionalUsuario = Contexto.getUsuarioLogado();
        String tipo = Contexto.getTipoRefeicao();

        if (optionalUsuario.isEmpty() || tipo == null) {
            showError("Erro ao recuperar informações do usuário ou tipo de refeição. Por favor, faça login ou selecione a refeição novamente.");
            return;
        }
        Usuario usuario = optionalUsuario.get(); // Get the Usuario object

        RefeicaoDAO refeicaoDAO = new RefeicaoDAO();
        boolean todosAdicionados = true; // Flag to track if all selected items were added successfully

        // Adiciona cada alimento selecionado à refeição do usuário
        for (String nome : selecionados) {
            Alimento alimento = todosAlimentos.stream()
                    .filter(a -> a.getNome().equalsIgnoreCase(nome))
                    .findFirst()
                    .orElse(null);

            if (alimento != null) {
                try {
                    boolean sucesso = refeicaoDAO.adicionarAlimento(usuario.getId(), tipo, alimento);
                    if (!sucesso) {
                        todosAdicionados = false; // Set flag if any addition fails
                        showError("Falha ao adicionar '" + nome + "'.");
                    }
                } catch (SQLException e) {
                    todosAdicionados = false;
                    showError("Erro de banco de dados ao adicionar '" + nome + "': " + e.getMessage());
                    System.err.println("SQL Exception adding food: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    todosAdicionados = false;
                    showError("Erro inesperado ao adicionar '" + nome + "': " + e.getMessage());
                    System.err.println("General Exception adding food: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                todosAdicionados = false;
                showError("Alimento '" + nome + "' não encontrado na base de dados de alimentos.");
            }
        }

        if (todosAdicionados) {
            showInfo("Alimentos adicionados com sucesso.");
        } else {
            showInfo("Alguns alimentos não puderam ser adicionados. Verifique os erros.");
        }
        carregarAlimentosRecentes(); // Refresh the recent foods list
    }

    /**
     * <p>Manipula o evento de clique em um item da lista de alimentos não recentes (resultados da busca).</p>
     *
     * <p>Quando um alimento é selecionado, ele é definido no {@link Contexto} como o alimento selecionado
     * e a aplicação é redirecionada para a tela de informações do alimento.</p>
     *
     * @param event O {@link MouseEvent} que disparou esta ação (geralmente um clique do mouse).
     */
    private void handleSelecionarAlimento(MouseEvent event) {
        String nomeSelecionado = listAlimentosNaoRecentes.getSelectionModel().getSelectedItem();
        if (nomeSelecionado != null && event.getClickCount() == 2) { // Only on double click
            Alimento alimento = todosAlimentos.stream()
                    .filter(a -> a.getNome().equalsIgnoreCase(nomeSelecionado))
                    .findFirst()
                    .orElse(null);

            if (alimento != null) {
                Contexto.setAlimentoSelecionado(alimento);

                // Redireciona para a tela de informações do alimento
                new SceneController().switchScene(event, "/fxml/telaAlimentoInformacoes.fxml");
            } else {
                showError("Alimento selecionado não encontrado em nossa base de dados.");
            }
        }
    }

    /**
     * <p>Manipula o evento de voltar para a tela de refeições.</p>
     *
     * <p>Redireciona o usuário de volta para a tela anterior utilizando o {@link SceneController}.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     */
    @FXML
    private void voltarParaTelaRefeicoes(ActionEvent event) throws IOException {
        new SceneController().switchScene(event, "/fxml/telaRefeicoes.fxml");
    }

    /**
     * <p>Exibe uma caixa de diálogo de alerta com uma mensagem de erro.</p>
     *
     * @param mensagem A mensagem de erro a ser exibida.
     */
    private void showError(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * <p>Exibe uma caixa de diálogo de alerta com uma mensagem informativa.</p>
     *
     * @param mensagem A mensagem informativa a ser exibida.
     */
    private void showInfo(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}