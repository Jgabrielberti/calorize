package calorize.model;

import calorize.utils.BancoInit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Classe principal que inicia a aplicação Calorize.
 * <p>
 * Esta classe estende {@link javafx.application.Application} e serve como ponto de entrada
 * para a aplicação JavaFX. Responsável por:
 * </p>
 * <ul>
 *   <li>Inicializar o banco de dados</li>
 *   <li>Carregar a interface gráfica inicial</li>
 *   <li>Configurar os estilos CSS</li>
 *   <li>Lançar a janela principal</li>
 * </ul>
 *
 * <p><b>Fluxo de inicialização:</b></p>
 * <ol>
 *   <li>O método {@link #main(String[])} é chamado pela JVM</li>
 *   <li>O framework JavaFX chama automaticamente o método {@link #start(Stage)}</li>
 *   <li>A tabela do banco de dados é criada através de {@link calorize.utils.BancoInit#criarTabelas()}</li>
 *   <li>A interface de login é carregada a partir do arquivo FXML</li>
 *   <li>Os estilos CSS são aplicados</li>
 *   <li>A janela principal é exibida</li>
 * </ol>
 *
 * <p><b>Arquivos relacionados:</b></p>
 * <ul>
 *   <li>View: {@code /fxml/telaLogin.fxml}</li>
 *   <li>Estilos: {@code /fxml/ringprogress.css}, {@code /fxml/circleprogress.css}</li>
 * </ul>
 */
public class Main extends Application {

    /**
     * Método principal que inicia a aplicação JavaFX.
     *
     * @param args Argumentos de linha de comando (não utilizados nesta aplicação)
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Ponto de entrada principal para a aplicação JavaFX.
     * <p>
     * Este método é chamado automaticamente após a inicialização do JavaFX e realiza:
     * </p>
     * <ul>
     *   <li>Inicialização do banco de dados</li>
     *   <li>Carregamento da cena inicial (tela de login)</li>
     *   <li>Aplicação dos estilos visuais</li>
     *   <li>Configuração e exibição da janela principal</li>
     * </ul>
     *
     * @param stage O palco (janela) primário fornecido pelo JavaFX
     * @throws Exception Se ocorrer algum erro durante o carregamento dos recursos FXML ou CSS
     */
    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Inicializa o banco de dados
            BancoInit.criarTabelas();

            // Carrega a interface de login
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/telaLogin.fxml"));
            Scene scene = new Scene(root);

            // Aplica os estilos CSS
            scene.getStylesheets().add(getClass().getResource("/fxml/ringprogress.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/fxml/circleprogress.css").toExternalForm());

            // Configura e exibe a janela
            stage.setTitle("Calorize - Login");
            stage.setScene(scene);
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}