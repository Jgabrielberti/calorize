package calorize.controller;

import calorize.dao.UsuarioDAO;
import calorize.model.Usuario;
import calorize.utils.Contexto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional; // Import java.util.Optional
import java.util.stream.Collectors;

/**
 * <p>Controlador da tela de gerenciamento de amigos, permitindo ao usuário logado
 * buscar, adicionar e remover outros usuários como amigos dentro da aplicação.</p>
 *
 * <p>Esta classe gerencia a exibição da lista de amigos atuais e a lista de usuários
 * que ainda não são amigos, facilitando a interação de rede entre os usuários.</p>
 *
 * <p>Utiliza {@link UsuarioDAO} para operações de banco de dados relacionadas a usuários e amigos,
 * e {@link Contexto} para obter o usuário logado.</p>
 */
public class telaAdicionarAmigosController {

    @FXML private ListView<String> listAmigosAtuais;
    @FXML private ListView<String> listUsuariosNaoAmigos;
    @FXML private TextField buscarUsuarioField;

    private final UsuarioDAO usuarioDAO = UsuarioDAO.getInstance(); // Ensure you're using getInstance()
    private ObservableList<Usuario> amigos;
    private ObservableList<Usuario> naoAmigos;

    /**
     * <p>Método de inicialização do controlador, invocado automaticamente após o carregamento do FXML.</p>
     *
     * <p>Responsável por carregar a lista de amigos do usuário logado e configurar o
     * comportamento da barra de busca para encontrar outros usuários.</p>
     *
     * <p>Exibe uma mensagem de erro se nenhum usuário estiver logado no sistema.</p>
     */
    @FXML
    public void initialize() {
        Optional<Usuario> optionalUsuarioLogado = Contexto.getUsuarioLogado(); // Get the Optional

        if (optionalUsuarioLogado.isEmpty()) { // Check if a user is present
            showError("Nenhum usuário logado. Por favor, faça login novamente.");
            // Optionally, disable controls or redirect to login screen
            listAmigosAtuais.setDisable(true);
            listUsuariosNaoAmigos.setDisable(true);
            buscarUsuarioField.setDisable(true);
            return;
        }

        Usuario usuarioLogado = optionalUsuarioLogado.get(); // Get the User object from the Optional

        carregarAmigos(usuarioLogado.getId());
        configurarBuscaUsuarios(usuarioLogado.getId());
    }

    /**
     * <p>Carrega e exibe a lista de amigos do usuário especificado na {@link ListView} de amigos atuais.</p>
     *
     * @param usuarioId O ID do usuário cujos amigos devem ser carregados.
     */
    private void carregarAmigos(int usuarioId) {
        List<Usuario> listaAmigos = usuarioDAO.buscarAmigos(usuarioId);
        amigos = FXCollections.observableArrayList(listaAmigos);
        listAmigosAtuais.setItems(FXCollections.observableArrayList(
                amigos.stream().map(Usuario::getNome).collect(Collectors.toList())
        ));
    }

    /**
     * <p>Configura o listener para o campo de busca de usuários, atualizando a lista
     * de usuários que não são amigos conforme o texto é digitado.</p>
     *
     * <p>A busca é realizada no banco de dados, excluindo o próprio usuário logado e seus amigos atuais.</p>
     *
     * @param usuarioId O ID do usuário logado, usado para filtrar a busca.
     */
    private void configurarBuscaUsuarios(int usuarioId) {
        buscarUsuarioField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                listUsuariosNaoAmigos.getItems().clear();
                return;
            }

            // You might want to add a debounce here for performance on large databases
            List<Usuario> encontrados = usuarioDAO.buscarUsuariosNaoAmigosPorNome(usuarioId, newText);
            naoAmigos = FXCollections.observableArrayList(encontrados);

            listUsuariosNaoAmigos.setItems(FXCollections.observableArrayList(
                    naoAmigos.stream().map(Usuario::getNome).collect(Collectors.toList())
            ));
        });
    }

    /**
     * <p>Manipula o evento de adição de um novo amigo.</p>
     *
     * <p>Quando um usuário é selecionado na lista de "Não Amigos" e o botão de adicionar é clicado,
     * este método tenta adicionar o usuário selecionado como amigo do usuário logado no banco de dados.</p>
     *
     * <p>Atualiza a lista de amigos e limpa a busca em caso de sucesso.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     */
    @FXML
    private void handleAdicionarAmigo(ActionEvent event) {
        String nomeSelecionado = listUsuariosNaoAmigos.getSelectionModel().getSelectedItem();
        if (nomeSelecionado == null) {
            showError("Selecione um usuário para adicionar.");
            return;
        }

        Optional<Usuario> optionalUsuarioLogado = Contexto.getUsuarioLogado();
        if (optionalUsuarioLogado.isEmpty()) {
            showError("Nenhum usuário logado. Por favor, faça login novamente.");
            return;
        }
        Usuario usuarioLogado = optionalUsuarioLogado.get();

        Usuario amigo = buscarPorNome(naoAmigos, nomeSelecionado);
        if (amigo != null) {
            boolean sucesso = usuarioDAO.adicionarAmigo(usuarioLogado.getId(), amigo.getId());
            if (sucesso) {
                showInfo("Amigo adicionado com sucesso!");
                carregarAmigos(usuarioLogado.getId()); // Recarrega a lista de amigos
                buscarUsuarioField.clear(); // Limpa o campo de busca
                listUsuariosNaoAmigos.getItems().clear(); // Limpa a lista de não amigos
            } else {
                showError("Erro ao adicionar amigo. O usuário já pode ser seu amigo ou houve um problema no banco de dados.");
            }
        } else {
            showError("O usuário selecionado não foi encontrado na lista de não amigos.");
        }
    }

    /**
     * <p>Manipula o evento de remoção de um amigo existente.</p>
     *
     * <p>Quando um amigo é selecionado na lista de "Amigos Atuais" e o botão de remover é clicado,
     * este método tenta remover a relação de amizade no banco de dados.</p>
     *
     * <p>Atualiza a lista de amigos em caso de sucesso.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     */
    @FXML
    private void handleRemoverAmigo(ActionEvent event) {
        String nomeSelecionado = listAmigosAtuais.getSelectionModel().getSelectedItem();
        if (nomeSelecionado == null) {
            showError("Selecione um amigo para remover.");
            return;
        }

        Optional<Usuario> optionalUsuarioLogado = Contexto.getUsuarioLogado();
        if (optionalUsuarioLogado.isEmpty()) {
            showError("Nenhum usuário logado. Por favor, faça login novamente.");
            return;
        }
        Usuario usuarioLogado = optionalUsuarioLogado.get();

        Usuario amigo = buscarPorNome(amigos, nomeSelecionado);
        if (amigo != null) {
            boolean sucesso = usuarioDAO.removerAmigo(usuarioLogado.getId(), amigo.getId());
            if (sucesso) {
                showInfo("Amigo removido com sucesso!");
                carregarAmigos(usuarioLogado.getId()); // Recarrega a lista de amigos
            } else {
                showError("Erro ao remover amigo.");
            }
        } else {
            showError("O amigo selecionado não foi encontrado na lista de amigos.");
        }
    }

    /**
     * <p>Auxiliar para buscar um {@link Usuario} dentro de uma {@link List} pelo nome.</p>
     *
     * @param lista A lista de usuários onde a busca será realizada.
     * @param nome O nome do usuário a ser buscado.
     * @return A instância de {@link Usuario} encontrada ou {@code null} se não for encontrado.
     */
    private Usuario buscarPorNome(List<Usuario> lista, String nome) {
        return lista.stream().filter(u -> u.getNome().equals(nome)).findFirst().orElse(null);
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