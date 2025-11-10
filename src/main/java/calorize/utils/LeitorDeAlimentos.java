package calorize.utils;

import calorize.model.Alimento;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Classe utilitária para leitura de alimentos a partir de arquivos JSON.
 * <p>
 * Utiliza a biblioteca Gson para desserialização de objetos {@link Alimento}.
 * </p>
 *
 * <p><b>Exemplo de formato JSON esperado:</b></p>
 * <pre>{@code
 * [
 *   {
 *     "id": 1,
 *     "nome": "Maçã",
 *     "categoria": "FRUTA",
 *     "calorias": 52.0,
 *     "proteinas": 0.3,
 *     "gorduras": 0.2,
 *     "carboidratos": 14.0,
 *     "fibras": 2.4,
 *     "calcio": 6.0
 *   },
 *   ...
 * ]
 * }</pre>
 */
public class LeitorDeAlimentos {

    /**
     * Carrega uma lista de alimentos a partir de um arquivo JSON.
     *
     * @param caminho Caminho do arquivo JSON contendo os alimentos
     * @return Lista de objetos {@link Alimento} ou {@code null} em caso de erro
     * @throws RuntimeException Se ocorrer algum erro durante a leitura do arquivo
     *
     * @see Gson
     */
    public static List<Alimento> carregarAlimentos(String caminho) {
        try (FileReader reader = new FileReader(caminho)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Alimento>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            System.err.println("Erro ao ler arquivo de alimentos: " + e.getMessage());
            throw new RuntimeException("Falha ao carregar alimentos do arquivo: " + caminho, e);
        }
    }
}