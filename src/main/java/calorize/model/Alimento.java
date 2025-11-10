package calorize.model;

import calorize.interfaces.Consumivel;
import java.util.Objects;

/**
 * Representa um alimento com suas propriedades nutricionais e categorização.
 * <p>
 * Esta classe é imutável e implementa a interface {@link Consumivel} para integração
 * com o sistema de cálculo nutricional do Calorize.
 * </p>
 */
public final class Alimento implements Consumivel {
    /**
     * Categorias de alimentos disponíveis para classificação.
     */
    public enum Categoria {
        /** Frutas */
        FRUTA,
        /** Verduras e legumes */
        VERDURA,
        /** Carnes e proteínas animais */
        CARNE,
        /** Leite e derivados */
        LATICINIO,
        /** Grãos e cereais */
        GRAOS,
        /** Outros alimentos não classificados */
        OUTROS
    }

    private final int id;
    private final String nome;
    private final Categoria categoria;
    private final double calorias;
    private final double proteinas;
    private final double gorduras;
    private final double carboidratos;
    private final double fibras;
    private final double calcio;

    /**
     * Constrói um novo alimento com todos os atributos nutricionais.
     *
     * @param id Identificador único do alimento
     * @param nome Nome do alimento (não pode ser nulo)
     * @param categoria Categoria do alimento (não pode ser nula)
     * @param calorias Valor energético em kcal (≥ 0)
     * @param proteinas Quantidade de proteínas em gramas (≥ 0)
     * @param gorduras Quantidade de gorduras em gramas (≥ 0)
     * @param carboidratos Quantidade de carboidratos em gramas (≥ 0)
     * @param fibras Quantidade de fibras em gramas (≥ 0)
     * @param calcio Quantidade de cálcio em mg (≥ 0)
     * @throws IllegalArgumentException Se qualquer valor nutricional for negativo
     * @throws NullPointerException Se nome ou categoria forem nulos
     */
    public Alimento(int id, String nome, Categoria categoria,
                    double calorias, double proteinas, double gorduras,
                    double carboidratos, double fibras, double calcio) {
        this.id = id;
        this.nome = Objects.requireNonNull(nome, "Nome não pode ser nulo");
        this.categoria = Objects.requireNonNull(categoria, "Categoria não pode ser nula");
        this.calorias = validarNutriente(calorias, "Calorias");
        this.proteinas = validarNutriente(proteinas, "Proteínas");
        this.gorduras = validarNutriente(gorduras, "Gorduras");
        this.carboidratos = validarNutriente(carboidratos, "Carboidratos");
        this.fibras = validarNutriente(fibras, "Fibras");
        this.calcio = validarNutriente(calcio, "Cálcio");
    }

    /**
     * Valida se um valor nutricional é não-negativo.
     *
     * @param valor Valor a ser validado
     * @param nomeNutriente Nome do nutriente para mensagens de erro
     * @return O valor validado
     * @throws IllegalArgumentException Se o valor for negativo
     */
    private double validarNutriente(double valor, String nomeNutriente) {
        if (valor < 0) {
            throw new IllegalArgumentException(nomeNutriente + " não pode ser negativo");
        }
        return valor;
    }

    // --- Getters Básicos --- //

    /** @return Identificador único do alimento */
    public int getId() { return id; }

    /** @return Nome do alimento */
    public String getNome() { return nome; }

    /** @return Categoria do alimento */
    public Categoria getCategoria() { return categoria; }

    // --- Getters Nutricionais (em gramas) --- //

    /** @return Valor energético em quilocalorias (kcal) */
    public double getCalorias() { return calorias; }

    /** @return Quantidade de proteínas em gramas (g) */
    public double getProteinas() { return proteinas; }

    /** @return Quantidade de gorduras em gramas (g) */
    public double getGorduras() { return gorduras; }

    /** @return Quantidade de carboidratos em gramas (g) */
    public double getCarboidratos() { return carboidratos; }

    // --- Implementação da Interface Consumivel --- //

    /** {@inheritDoc} */
    @Override public double getEnergy_kcal() { return calorias; }

    /** {@inheritDoc} */
    @Override public double getProtein_g() { return proteinas; }

    /** {@inheritDoc} */
    @Override public double getLipid_g() { return gorduras; }

    /** {@inheritDoc} */
    @Override public double getCarbohydrate_g() { return carboidratos; }

    /** {@inheritDoc} */
    @Override public double getFibras() { return fibras; }

    /** {@inheritDoc} */
    @Override public double getCalcio() { return calcio; }

    // --- Métodos de Objeto --- //

    /**
     * Retorna uma representação resumida do alimento.
     *
     * @return String no formato "Alimento [ID - NOME, Cals: X.X kcal]"
     */
    @Override
    public String toString() {
        return String.format("Alimento [%s - %s, Cals: %.1f kcal]", id, nome, calorias);
    }

    /**
     * Compara alimentos por ID.
     *
     * @param o Objeto a ser comparado
     * @return true se os IDs forem iguais
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alimento alimento = (Alimento) o;
        return id == alimento.id;
    }

    /** @return Hash code baseado no ID */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}