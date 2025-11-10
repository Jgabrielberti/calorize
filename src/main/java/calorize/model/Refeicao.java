package calorize.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa uma refeição contendo alimentos consumidos em um horário específico,
 * com cálculo automático de macrosnutrientes totais.
 */
public class Refeicao {
    /**
     * Tipos de refeições disponíveis para categorização.
     */
    public enum TipoRefeicao {
        /** Café da manhã */
        CAFE_DA_MANHA,
        /** Almoço */
        ALMOCO,
        /** Jantar */
        JANTAR,
        /** Lanche entre refeições */
        LANCHE
    }

    private final TipoRefeicao tipo;
    private final String horario;
    private final List<Alimento> alimentos;

    /**
     * Cria uma nova refeição vazia.
     *
     * @param tipo Tipo da refeição ({@link TipoRefeicao}, não pode ser nulo).
     * @param horario Horário no formato HH:MM (24h, não pode ser nulo ou inválido).
     * @throws IllegalArgumentException Se o horário estiver em formato inválido.
     * @throws NullPointerException Se tipo ou horário forem nulos.
     */
    public Refeicao(TipoRefeicao tipo, String horario) {
        this.tipo = Objects.requireNonNull(tipo, "Tipo de refeição não pode ser nulo");
        this.horario = validarHorario(horario);
        this.alimentos = new ArrayList<>();
    }

    /**
     * Valida o formato do horário.
     *
     * @param horario String no formato HH:MM.
     * @return O próprio horário se válido.
     * @throws IllegalArgumentException Se o formato for inválido.
     * @throws NullPointerException Se o horário for nulo.
     */
    private String validarHorario(String horario) {
        Objects.requireNonNull(horario, "Horário não pode ser nulo");
        if (!horario.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            throw new IllegalArgumentException("Horário inválido. Use o formato HH:MM");
        }
        return horario;
    }

    // --- Getters --- //

    /**
     * @return Tipo da refeição ({@link TipoRefeicao}).
     */
    public TipoRefeicao getTipo() {
        return tipo;
    }

    /**
     * @return Horário no formato HH:MM.
     */
    public String getHorario() {
        return horario;
    }

    /**
     * @return Lista imutável de alimentos desta refeição.
     */
    public List<Alimento> getAlimentos() {
        return Collections.unmodifiableList(alimentos);
    }

    // --- Cálculos nutricionais --- //

    /**
     * Calcula o total de calorias da refeição.
     *
     * @return Soma das calorias de todos os alimentos (em kcal).
     */
    public double getCalorias() {
        return alimentos.stream().mapToDouble(Alimento::getCalorias).sum();
    }

    /**
     * Calcula o total de proteínas da refeição.
     *
     * @return Soma das proteínas de todos os alimentos (em gramas).
     */
    public double getProteinas() {
        return alimentos.stream().mapToDouble(Alimento::getProteinas).sum();
    }

    /**
     * Calcula o total de carboidratos da refeição.
     *
     * @return Soma dos carboidratos de todos os alimentos (em gramas).
     */
    public double getCarboidratos() {
        return alimentos.stream().mapToDouble(Alimento::getCarboidratos).sum();
    }

    /**
     * Calcula o total de gorduras da refeição.
     *
     * @return Soma das gorduras de todos os alimentos (em gramas).
     */
    public double getGorduras() {
        return alimentos.stream().mapToDouble(Alimento::getGorduras).sum();
    }

    // --- Modificação --- //

    /**
     * Adiciona um alimento à refeição.
     *
     * @param alimento Alimento a ser adicionado (não pode ser nulo).
     * @throws NullPointerException Se o alimento for nulo.
     */
    public void adicionarAlimento(Alimento alimento) {
        Objects.requireNonNull(alimento, "Alimento não pode ser nulo");
        alimentos.add(alimento);
    }

    // --- Representação textual --- //

    /**
     * Retorna uma representação resumida da refeição.
     *
     * @return String no formato "Refeição [TIPO - HORÁRIO] Calorias: X kcal".
     */
    @Override
    public String toString() {
        return String.format(
                "Refeição [%s - %s] Calorias: %.2f kcal",
                tipo.name(), horario, getCalorias()
        );
    }
}