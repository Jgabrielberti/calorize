package calorize.controller;

import calorize.dao.RefeicaoDAO;
import calorize.model.Alimento;
import calorize.model.Usuario; // Import Usuario class
import calorize.utils.Contexto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Import Optional

import static calorize.model.Alimento.Categoria.OUTROS;

/**
 * <p>Controlador da tela de tabela de nutrientes, exibindo os alimentos consumidos
 * em um dia específico e seus respectivos valores nutricionais totais.</p>
 *
 * <p>Esta classe permite ao usuário visualizar um resumo detalhado dos macronutrientes
 * e micronutrientes consumidos em todas as refeições de uma calorize.data, e navegar para
 * o dia anterior para revisar dados passados.</p>
 *
 * <p>Integra-se com {@link RefeicaoDAO} para buscar os alimentos e {@link Contexto}
 * para obter o usuário logado.</p>
 */
public class telaTabelaController {

    @FXML private TableView<Alimento> tabelaNutrientes; // Tabela para exibir os alimentos e seus nutrientes
    @FXML private Button tabelaDiaAnteriorButton; // Botão para navegar para o dia anterior

    private LocalDate dataAtual = LocalDate.now(); // Armazena a calorize.data atualmente exibida na tabela
    private final RefeicaoDAO refeicaoDAO = new RefeicaoDAO(); // DAO para operações relacionadas a refeições

    /**
     * <p>Método de inicialização do controlador, invocado automaticamente após o carregamento do FXML.</p>
     *
     * <p>Responsável por configurar as colunas da {@link TableView} e carregar os dados
     * nutricionais do dia atual do usuário logado.</p>
     */
    @FXML
    public void initialize() {
        configurarColunas(); // Configura as colunas da tabela
        carregarTabelaParaData(dataAtual); // Carrega os dados para a calorize.data atual
    }

    /**
     * <p>Configura as colunas da {@link TableView} de nutrientes.</p>
     *
     * <p>Este método limpa quaisquer colunas existentes e adiciona novas colunas
     * para "Alimento", "Calorias", "Proteínas", "Carboidratos", "Gorduras", "Fibras" e "Cálcio",
     * mapeando-as às propriedades correspondentes da classe {@link Alimento}.</p>
     */
    private void configurarColunas() {
        tabelaNutrientes.getColumns().clear(); // Limpa colunas existentes para evitar duplicação

        // Adiciona todas as colunas necessárias à tabela
        tabelaNutrientes.getColumns().addAll(
                criarColuna("Alimento", "nome"),
                criarColuna("Calorias (Kcal)", "calorias"),
                criarColuna("Proteínas (g)", "proteinas"),
                criarColuna("Carboidratos (g)", "carboidratos"),
                criarColuna("Gorduras (g)", "gorduras"),
                criarColuna("Fibras (g)", "fibras"),
                criarColuna("Cálcio (mg)", "calcio")
                // Se houver uma 9ª propriedade no construtor de Alimento, ela também deve ser mapeada aqui
                // Exemplo: criarColuna("Nova Propriedade", "novaPropriedade")
        );
    }

    /**
     * <p>Cria uma {@link TableColumn} genérica para a {@link TableView}.</p>
     *
     * <p>Define o título da coluna e a propriedade do objeto {@link Alimento} à qual
     * esta coluna estará vinculada, permitindo que a tabela exiba o valor da propriedade.</p>
     *
     * @param <T> O tipo de dado da propriedade que esta coluna irá exibir.
     * @param titulo O texto que será exibido como cabeçalho da coluna.
     * @param propriedade O nome da propriedade (atributo) da classe {@link Alimento}
     * que conterá o valor para esta coluna (e.g., "nome", "calorias").
     * @return Uma nova instância de {@link TableColumn} configurada.
     */
    private <T> TableColumn<Alimento, T> criarColuna(String titulo, String propriedade) {
        TableColumn<Alimento, T> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(new PropertyValueFactory<>(propriedade)); // Vincula a coluna à propriedade
        return coluna;
    }

    /**
     * <p>Carrega os alimentos consumidos e seus totais nutricionais para uma calorize.data específica na tabela.</p>
     *
     * <p>Busca todos os alimentos consumidos em todas as refeições do usuário logado para a calorize.data fornecida.
     * Calcula os totais de calorias, proteínas, carboidratos, gorduras, fibras e cálcio.
     * Adiciona uma linha "Total" à tabela com esses somatórios e atualiza a {@link TableView}.</p>
     *
     * @param data A {@link LocalDate} para a qual os dados da tabela devem ser carregados.
     */
    private void carregarTabelaParaData(LocalDate data) {
        try {
            Optional<Usuario> optionalUsuario = Contexto.getUsuarioLogado(); // Get the Optional

            if (optionalUsuario.isEmpty()) { // Check if user is present
                System.err.println("Erro: Nenhum usuário logado. Não é possível carregar a tabela.");
                tabelaNutrientes.getItems().clear(); // Clear table if no user
                // Optionally show an alert to the user
                // showError("Nenhum usuário logado. Por favor, faça login para ver a tabela.");
                return;
            }

            Usuario usuario = optionalUsuario.get(); // Get the Usuario object
            int usuarioId = usuario.getId(); // Now you can call getId()

            String dataFormatada = data.toString(); // Converte a calorize.data para String (formato ISO)

            List<Alimento> todosAlimentos = new ArrayList<>();
            String[] tiposRefeicao = {"cafe", "almoco", "jantar", "lanches"}; // Tipos de refeições a serem consideradas

            // Itera sobre cada tipo de refeição para buscar os alimentos e adicioná-los à lista total
            for (String tipo : tiposRefeicao) {
                todosAlimentos.addAll(refeicaoDAO.buscarAlimentosPorRefeicao(usuarioId, tipo, dataFormatada));
            }

            // Inicializa variáveis para somar os totais nutricionais
            double totalCal = 0, totalProt = 0, totalCarb = 0, totalGord = 0, totalFibra = 0, totalCalcio = 0;
            // Percorre a lista de alimentos para somar seus valores nutricionais
            for (Alimento a : todosAlimentos) {
                totalCal += a.getCalorias();
                totalProt += a.getProteinas();
                totalCarb += a.getCarboidratos();
                totalGord += a.getGorduras();
                totalFibra += a.getFibras();
                totalCalcio += a.getCalcio();
            }

            Alimento total = new Alimento(0, "",  OUTROS, totalCal, totalProt, totalGord, totalCarb, totalFibra, totalCalcio);
            todosAlimentos.add(total); // Adiciona a linha de total à lista de alimentos

            // Converte a lista de alimentos para uma ObservableList e define-a na tabela
            ObservableList<Alimento> observableList = FXCollections.observableArrayList(todosAlimentos);
            tabelaNutrientes.setItems(observableList);
        } catch (Exception e) {
            System.err.println("Erro ao carregar tabela de nutrientes: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for better debugging
            // Em caso de erro, limpa a tabela para evitar dados inconsistentes
            tabelaNutrientes.getItems().clear();
        }
    }

    /**
     * <p>Manipula o evento de clique no botão "Dia Anterior".</p>
     *
     * <p>Decrementa a calorize.data atual em um dia e recarrega a tabela de nutrientes para
     * a nova calorize.data, permitindo a visualização de dados históricos.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     */
    @FXML
    private void handleVisualizarDiaAnterior(ActionEvent event) {
        dataAtual = dataAtual.minusDays(1); // Retorna a calorize.data para o dia anterior
        carregarTabelaParaData(dataAtual); // Recarrega a tabela com os dados do dia anterior
    }
}