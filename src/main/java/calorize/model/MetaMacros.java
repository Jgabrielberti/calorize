package calorize.model;

/**
 * Representa as metas nutricionais diárias de um usuário (macronutrientes e calorias).
 * <p>
 * Esta classe é imutável - uma vez criada, seus valores não podem ser alterados.
 * </p>
 */
public final class MetaMacros {
    private final double calorias;
    private final double proteinas;
    private final double carboidratos;
    private final double gorduras;

    /**
     * Constrói uma nova instância de MetaMacros com valores validados.
     *
     * @param calorias Meta diária de calorias (em kcal, deve ser ≥ 0).
     * @param proteinas Meta diária de proteínas (em gramas, deve ser ≥ 0).
     * @param carboidratos Meta diária de carboidratos (em gramas, deve ser ≥ 0).
     * @param gorduras Meta diária de gorduras (em gramas, deve ser ≥ 0).
     * @throws IllegalArgumentException Se qualquer nutriente for negativo.
     */
    public MetaMacros(double calorias, double proteinas, double carboidratos, double gorduras) {
        this.calorias = validarNutriente(calorias, "Calorias");
        this.proteinas = validarNutriente(proteinas, "Proteínas");
        this.carboidratos = validarNutriente(carboidratos, "Carboidratos");
        this.gorduras = validarNutriente(gorduras, "Gorduras");
    }

    /**
     * Valida se um valor nutricional é não-negativo.
     *
     * @param valor Valor a ser validado.
     * @param nomeNutriente Nome do nutriente para mensagens de erro.
     * @return O próprio valor se válido.
     * @throws IllegalArgumentException Se o valor for negativo.
     */
    private double validarNutriente(double valor, String nomeNutriente) {
        if (valor < 0) {
            throw new IllegalArgumentException(nomeNutriente + " não pode ser negativo");
        }
        return valor;
    }

    // --- Getters --- //

    /** @return Meta diária de calorias em quilocalorias (kcal). */
    public double getCalorias() {
        return calorias;
    }

    /** @return Meta diária de proteínas em gramas (g). */
    public double getProteinas() {
        return proteinas;
    }

    /** @return Meta diária de carboidratos em gramas (g). */
    public double getCarboidratos() {
        return carboidratos;
    }

    /** @return Meta diária de gorduras em gramas (g). */
    public double getGorduras() {
        return gorduras;
    }

    // --- Builder --- //

    /**
     * Builder para criação flexível de instâncias de MetaMacros.
     * <p>
     * Permite construção passo-a-passo com métodos encadeados.
     * </p>
     * Exemplo de uso:
     * <pre>{@code
     * MetaMacros metas = new MetaMacros.Builder()
     *     .calorias(2000)
     *     .proteinas(150)
     *     .carboidratos(200)
     *     .gorduras(50)
     *     .build();
     * }</pre>
     */
    public static class Builder {
        private double calorias;
        private double proteinas;
        private double carboidratos;
        private double gorduras;

        /**
         * Define a meta de calorias.
         *
         * @param calorias Valor em kcal.
         * @return O próprio builder para encadeamento.
         */
        public Builder calorias(double calorias) {
            this.calorias = calorias;
            return this;
        }

        /**
         * Define a meta de proteínas.
         *
         * @param proteinas Valor em gramas.
         * @return O próprio builder para encadeamento.
         */
        public Builder proteinas(double proteinas) {
            this.proteinas = proteinas;
            return this;
        }

        /**
         * Define a meta de carboidratos.
         *
         * @param carboidratos Valor em gramas.
         * @return O próprio builder para encadeamento.
         */
        public Builder carboidratos(double carboidratos) {
            this.carboidratos = carboidratos;
            return this;
        }

        /**
         * Define a meta de gorduras.
         *
         * @param gorduras Valor em gramas.
         * @return O próprio builder para encadeamento.
         */
        public Builder gorduras(double gorduras) {
            this.gorduras = gorduras;
            return this;
        }

        /**
         * Constrói a instância de MetaMacros com os valores definidos.
         *
         * @return Nova instância imutável de MetaMacros.
         * @throws IllegalArgumentException Se qualquer nutriente for negativo.
         */
        public MetaMacros build() {
            return new MetaMacros(calorias, proteinas, carboidratos, gorduras);
        }
    }

    // --- Representação textual --- //

    /**
     * Retorna uma representação formatada das metas nutricionais.
     *
     * @return String no formato:
     * "MetaMacros [Calorias=X.XX kcal, Proteínas=X.XXg, Carboidratos=X.XXg, Gorduras=X.XXg]"
     */
    @Override
    public String toString() {
        return String.format(
                "MetaMacros [Calorias=%.2f kcal, Proteínas=%.2fg, Carboidratos=%.2fg, Gorduras=%.2fg]",
                calorias, proteinas, carboidratos, gorduras
        );
    }
}