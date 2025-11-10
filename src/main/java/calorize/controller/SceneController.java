package calorize.controller;

import java.io.IOException;
import javafx.event.Event; // Change from ActionEvent to the more general Event
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * <p>Classe controladora utilitária para gerenciar a troca de cenas (telas) em aplicações JavaFX.</p>
 *
 * <p>Facilita a navegação entre diferentes arquivos FXML, carregando o novo conteúdo
 * e definindo-o como a cena atual no Stage principal da aplicação.</p>
 *
 * <p><b>Uso:</b> Para utilizar esta classe, instancie-a e chame o método {@link #switchScene(Event, String)}
 * passando o evento da ação que disparou a troca e o caminho para o arquivo FXML da nova cena.</p>
 */
public class SceneController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    /**
     * <p>Muda a cena (tela) atual da aplicação para uma nova cena definida por um arquivo FXML.</p>
     *
     * <p>Este método carrega o arquivo FXML especificado, cria uma nova {@link Scene} a partir dele
     * e a define no {@link Stage} principal da aplicação, exibindo a nova tela ao usuário.</p>
     *
     * <p>Em caso de caminho FXML inválido ou erro durante o carregamento, mensagens de erro são
     * impressas no console do sistema.</p>
     *
     * @param event O evento {@link Event} que disparou a requisição de mudança de cena.
     * É usado para obter o {@link Stage} atual da aplicação.
     * @param fxmlPath O caminho (String) para o arquivo FXML da nova cena a ser carregada.
     * O caminho deve ser relativo à raiz do classpath da aplicação
     * (ex: "/calorize/view/MainView.fxml").
     */
    public void switchScene(Event event, String fxmlPath) { // Changed ActionEvent to Event
        if (fxmlPath == null || getClass().getResource(fxmlPath) == null) {
            System.err.println("Erro: Caminho FXML inválido ou inexistente: " + fxmlPath);
            return;
        }

        try {
            root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Cast to Node is safe as event.getSource() typically returns a Node for UI events
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela: " + fxmlPath);
            e.printStackTrace();
        }
    }
}