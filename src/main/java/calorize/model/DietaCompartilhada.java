package calorize.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa uma dieta compartilhada entre usuários do sistema Calorize.
 * <p>
 * Contém uma lista de alimentos compartilhados, calorize.data da operação e informações
 * sobre remetente e destinatário. A classe é imutável após construção.
 * </p>
 */
public final class DietaCompartilhada {
    private final Usuario remetente;
    private final Usuario destinatario;
    private final List<Alimento> alimentos;
    private final LocalDate data;

    /**
     * Construtor privado usado pelo Builder.
     *
     * @param remetente Usuário que compartilha a dieta (não pode ser nulo)
     * @param destinatario Usuário que recebe a dieta (não pode ser nulo ou igual ao remetente)
     * @param alimentos Lista de alimentos compartilhados (não pode ser nula ou vazia)
     * @param data Data do compartilhamento (não pode ser nula)
     */
    private DietaCompartilhada(Usuario remetente, Usuario destinatario, List<Alimento> alimentos, LocalDate data) {
        this.remetente = validarRemetente(remetente);
        this.destinatario = validarDestinatario(destinatario, remetente);
        this.alimentos = validarAlimentos(alimentos);
        this.data = Objects.requireNonNull(data, "Data não pode ser nula");
    }

    /**
     * Valida o usuário remetente.
     *
     * @param remetente Usuário a validar
     * @return O mesmo remetente se válido
     * @throws NullPointerException Se remetente for nulo
     */
    private Usuario validarRemetente(Usuario remetente) {
        return Objects.requireNonNull(remetente, "Remetente não pode ser nulo");
    }

    /**
     * Valida o usuário destinatário.
     *
     * @param destinatario Usuário a validar
     * @param remetente Usuário remetente para comparação
     * @return O mesmo destinatário se válido
     * @throws NullPointerException Se destinatário for nulo
     * @throws IllegalArgumentException Se destinatário for igual ao remetente
     */
    private Usuario validarDestinatario(Usuario destinatario, Usuario remetente) {
        Objects.requireNonNull(destinatario, "Destinatário não pode ser nulo");
        if (remetente.equals(destinatario)) {
            throw new IllegalArgumentException("Remetente e destinatário não podem ser iguais");
        }
        return destinatario;
    }

    /**
     * Valida a lista de alimentos.
     *
     * @param alimentos Lista a validar
     * @return Lista imutável dos alimentos
     * @throws NullPointerException Se a lista for nula
     * @throws IllegalArgumentException Se a lista estiver vazia
     */
    private List<Alimento> validarAlimentos(List<Alimento> alimentos) {
        Objects.requireNonNull(alimentos, "Lista de alimentos não pode ser nula");
        if (alimentos.isEmpty()) {
            throw new IllegalArgumentException("A dieta deve conter pelo menos um alimento");
        }
        return Collections.unmodifiableList(alimentos);
    }

    // --- Getters --- //

    /** @return Usuário remetente da dieta */
    public Usuario getRemetente() { return remetente; }

    /** @return Usuário destinatário da dieta */
    public Usuario getDestinatario() { return destinatario; }

    /** @return Lista imutável de alimentos compartilhados */
    public List<Alimento> getAlimentos() { return alimentos; }

    /** @return Data do compartilhamento */
    public LocalDate getData() { return data; }

    // --- Builder --- //

    /**
     * Builder para criação flexível de instâncias de DietaCompartilhada.
     * <p>
     * Exemplo de uso:
     * <pre>{@code
     * DietaCompartilhada dieta = new DietaCompartilhada.Builder()
     *     .remetente(usuario1)
     *     .destinatario(usuario2)
     *     .alimentos(listaAlimentos)
     *     .calorize.data("2023-12-25")
     *     .build();
     * }</pre>
     */
    public static class Builder {
        private Usuario remetente;
        private Usuario destinatario;
        private List<Alimento> alimentos;
        private LocalDate data;

        /**
         * Define o remetente da dieta.
         *
         * @param remetente Usuário remetente
         * @return O próprio builder para encadeamento
         */
        public Builder remetente(Usuario remetente) {
            this.remetente = remetente;
            return this;
        }

        /**
         * Define o destinatário da dieta.
         *
         * @param destinatario Usuário destinatário
         * @return O próprio builder para encadeamento
         */
        public Builder destinatario(Usuario destinatario) {
            this.destinatario = destinatario;
            return this;
        }

        /**
         * Define os alimentos da dieta.
         *
         * @param alimentos Lista de alimentos a compartilhar
         * @return O próprio builder para encadeamento
         */
        public Builder alimentos(List<Alimento> alimentos) {
            this.alimentos = alimentos;
            return this;
        }

        /**
         * Define a calorize.data do compartilhamento.
         *
         * @param data String no formato "yyyy-MM-dd"
         * @return O próprio builder para encadeamento
         * @throws IllegalArgumentException Se a calorize.data estiver em formato inválido
         */
        public Builder data(String data) {
            this.data = parseData(data);
            return this;
        }

        /**
         * Constrói a instância de DietaCompartilhada.
         *
         * @return Nova instância imutável
         * @throws NullPointerException Se remetente, destinatário ou alimentos forem nulos
         * @throws IllegalArgumentException Se a lista de alimentos estiver vazia ou remetente/destinatário forem iguais
         */
        public DietaCompartilhada build() {
            return new DietaCompartilhada(remetente, destinatario, alimentos, data);
        }

        /**
         * Converte string para LocalDate.
         *
         * @param data String no formato ISO (yyyy-MM-dd)
         * @return Objeto LocalDate
         * @throws IllegalArgumentException Se o formato for inválido
         */
        private LocalDate parseData(String data) {
            try {
                return LocalDate.parse(data, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Data inválida. Use o formato yyyy-MM-dd");
            }
        }
    }

    // --- Métodos adicionais --- //

    /**
     * Calcula o total de calorias da dieta compartilhada.
     *
     * @return Soma das calorias de todos os alimentos
     */
    public double getCaloriasTotais() {
        return alimentos.stream().mapToDouble(Alimento::getCalorias).sum();
    }

    /**
     * Retorna uma representação resumida da dieta compartilhada.
     *
     * @return String no formato "DietaCompartilhada [De: X, Para: Y, Data: Z, Alimentos: N]"
     */
    @Override
    public String toString() {
        return String.format(
                "DietaCompartilhada [De: %s, Para: %s, Data: %s, Alimentos: %d]",
                remetente.getNome(), destinatario.getNome(), data, alimentos.size()
        );
    }
}