package calorize.utils;

import calorize.model.Alimento;
import calorize.model.Usuario;

import java.util.Optional;

/**
 * Classe utilitária para armazenamento de estado global da aplicação.
 * <p>
 * Mantém referências estáticas aos objetos de contexto atual como:
 * </p>
 * <ul>
 *   <li>Usuário logado</li>
 *   <li>Data selecionada</li>
 *   <li>Tipo de refeição sendo manipulada</li>
 *   <li>Alimento selecionado</li>
 * </ul>
 * <p>
 * <b>Observação:</b> Por ser um singleton implícito com estado global,
 * deve ser usado com cautela em ambientes multi-thread.
 * </p>
 */
public class Contexto {
    private static String tipoRefeicaoAtual;
    private static Optional<Usuario> usuarioLogado;
    private static String dataSelecionada;
    private static Alimento alimentoSelecionado;

    /**
     * Define a calorize.data atualmente selecionada na aplicação.
     *
     * @param data Data no formato de string (recomenda-se formato ISO 8601)
     */
    public static void setDataSelecionada(String data) {
        dataSelecionada = data;
    }

    /**
     * Obtém a calorize.data atualmente selecionada.
     *
     * @return Data no formato de string ou null se não definida
     */
    public static String getDataSelecionada() {
        return dataSelecionada;
    }

    /**
     * Define o tipo de refeição atualmente em foco.
     *
     * @param tipo Tipo de refeição (deve corresponder aos valores de {@link calorize.model.Refeicao.TipoRefeicao})
     */
    public static void setTipoRefeicao(String tipo) {
        tipoRefeicaoAtual = tipo;
    }

    /**
     * Obtém o tipo de refeição atualmente em foco.
     *
     * @return String com o tipo de refeição ou null se não definido
     */
    public static String getTipoRefeicao() {
        return tipoRefeicaoAtual;
    }

    /**
     * Define o usuário atualmente logado no sistema.
     *
     * @param usuario Instância de {@link Usuario} (pode ser null para logout)
     */
    public static void setUsuarioLogado(Optional<Usuario> usuario) {
        usuarioLogado = usuario;
    }

    /**
     * Obtém o usuário atualmente logado.
     *
     * @return Instância de {@link Usuario} ou null se nenhum usuário estiver logado
     */
    public static Optional<Usuario> getUsuarioLogado() {
        return usuarioLogado;
    }

    /**
     * Obtém o alimento atualmente selecionado na interface.
     *
     * @return Instância de {@link Alimento} ou null se nenhum estiver selecionado
     */
    public static Alimento getAlimentoSelecionado() {
        return alimentoSelecionado;
    }

    /**
     * Define o alimento atualmente selecionado na interface.
     *
     * @param alimento Instância de {@link Alimento} (pode ser null para deselecionar)
     */
    public static void setAlimentoSelecionado(Alimento alimento) {
        Contexto.alimentoSelecionado = alimento;
    }
}