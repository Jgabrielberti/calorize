package calorize.interfaces;

/**
 * Interface que define os métodos obrigatórios para qualquer item consumível no sistema.
 * <p>
 * Esta interface estabelece o contrato básico para obtenção de informações nutricionais,
 * permitindo que diferentes tipos de itens alimentares (alimentos, refeições, receitas)
 * possam ser tratados de forma uniforme pelo sistema.
 * </p>
 * <p>
 * <b>Unidades de medida:</b>
 * <ul>
 *   <li>Energia: quilocalorias (kcal)</li>
 *   <li>Proteínas, lipídios, carboidratos e fibras: gramas (g)</li>
 *   <li>Cálcio: miligramas (mg)</li>
 * </ul>
 * </p>
 */
public interface Consumivel {

    /**
     * Obtém a quantidade de energia fornecida pelo item consumível.
     *
     * @return valor energético em quilocalorias (kcal)
     */
    double getEnergy_kcal();

    /**
     * Obtém a quantidade de proteínas contida no item consumível.
     *
     * @return quantidade de proteínas em gramas (g)
     */
    double getProtein_g();

    /**
     * Obtém a quantidade de lipídios (gorduras) contida no item consumível.
     *
     * @return quantidade de lipídios em gramas (g)
     */
    double getLipid_g();

    /**
     * Obtém a quantidade de carboidratos contida no item consumível.
     *
     * @return quantidade de carboidratos em gramas (g)
     */
    double getCarbohydrate_g();

    /**
     * Obtém a quantidade de fibras alimentares contida no item consumível.
     *
     * @return quantidade de fibras em gramas (g)
     */
    double getFibras();

    /**
     * Obtém a quantidade de cálcio contida no item consumível.
     *
     * @return quantidade de cálcio em miligramas (mg)
     */
    double getCalcio();
}