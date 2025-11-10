package calorize.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap; // IMPORTANTE: Certifique-se de que TreeMap está importado

/**
 * Representa um usuário do sistema Calorize, com informações pessoais,
 * histórico de peso, metas nutricionais e relacionamentos.
 */
public class Usuario {
    public void setId(int id) {this.id = id;}

    /** Enumeração que define os gêneros disponíveis para o usuário. */
    public enum Genero { MASCULINO, FEMININO, OUTRO } // Adicione OUTRO se você o utiliza

    /** Enumeração que define as metas de peso do usuário. */
    public enum Meta { MANTER, PERDER, GANHAR }

    private int id;
    private String nome;
    private String email;
    private String senha;
    // Mudar a declaração aqui para TreeMap
    private TreeMap<LocalDate, Double> historicoPeso; // <-- AQUI! Mude de Map para TreeMap
    private int alturaCm;
    private Genero genero;
    private Meta meta;
    private MetaMacros metaMacros;
    private final List<Refeicao> refeicoes;
    private final List<Usuario> amigos;

    /**
     * Constrói um novo usuário com os dados básicos e calcula suas metas nutricionais.
     *
     * @param id Id do usuário (não pode ser vazio ou nulo).
     * @param nome Nome completo do usuário (não pode ser vazio ou nulo).
     * @param email do usuário (deve seguir o formato válido).
     * @param senha Senha do usuário (mínimo de 6 caracteres).
     * @param pesoKg Peso inicial do usuário em quilogramas (deve ser positivo).
     * @param alturaCm Altura do usuário em centímetros (deve ser positiva).
     * @param generoChar Caractere representando o gênero biológico do usuário ('M' ou 'F').
     * @param metaChar Caractere representando a meta de peso do usuário ('M', 'P', 'G').
     * @throws IllegalArgumentException Se algum parâmetro for inválido.
     * @throws NullPointerException Se nome, email ou senha forem nulos.
     */
    public Usuario(int id, String nome, String email, String senha, double pesoKg, int alturaCm, char generoChar, char metaChar) {
        // Mapeia char para Genero e Meta
        Genero generoEnum = switch (generoChar) {
            case 'M' -> Genero.MASCULINO;
            case 'F' -> Genero.FEMININO;
            default -> Genero.OUTRO; // Assumindo que você tem um Genero.OUTRO ou similar
        };

        Meta metaEnum = switch (metaChar) {
            case 'P' -> Meta.PERDER;
            case 'M' -> Meta.MANTER;
            case 'G' -> Meta.GANHAR;
            default -> Meta.MANTER; // Padrão caso não haja meta definida
        };

        validarDados(nome, email, senha, pesoKg, alturaCm);
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.alturaCm = alturaCm;
        this.genero = generoEnum;
        this.meta = metaEnum;
        // Inicializa historicoPeso como um TreeMap para manter a ordem por calorize.data
        this.historicoPeso = new TreeMap<>(); // <-- E AQUI!
        this.historicoPeso.put(LocalDate.now(), pesoKg); // Adiciona o peso inicial com a calorize.data atual
        this.refeicoes = new ArrayList<>();
        this.amigos = new ArrayList<>();
        int metaCal;
        double prot;
        double gord;
        this.metaMacros = new MetaMacros(
                metaCal = (calcularMetaCalorias(25)), // Idade 25 é um valor padrão aqui
                prot = (calcularMetaProteinas(pesoKg)),
                gord = (calcularMetaGorduras(metaCal, pesoKg)),
                calcularMetaCarboidratos(metaCal, prot, gord)
        );
    }

    /**
     * Segundo construtor, sem ID, para casos de criação de novo usuário (útil para cadastro)
     * O ID seria atribuído pelo DAO após a inserção no banco de dados.
     */
    public Usuario(String nome, String email, String senha, double pesoKg, int alturaCm, char generoChar, char metaChar) {
        this(0, nome, email, senha, pesoKg, alturaCm, generoChar, metaChar); // Chama o construtor principal com ID 0 temporário
    }

    /**
     * Calcula a meta calórica diária com base na fórmula de Harris-Benedict.
     *
     * @param idade Idade do usuário (deve ser positiva).
     * @return Calorias diárias recomendadas arredondadas para cima.
     * @throws IllegalArgumentException Se a idade for inválida.
    **/
    public int calcularMetaCalorias(int idade) {
        if (idade <= 0) throw new IllegalArgumentException("Idade inválida");
        double pesoAtual = getPesoAtual() != null ? getPesoAtual() : 0;
        double tmb = switch (genero) {
            case MASCULINO -> 88.362 + (13.397 * pesoAtual) + (4.799 * alturaCm) - (5.677 * idade);
            case FEMININO -> 447.593 + (9.247 * pesoAtual) + (3.098 * alturaCm) - (4.330 * idade);
            default -> 447.593 + (9.247 * pesoAtual) + (3.098 * alturaCm) - (4.330 * idade);
        };

        return (int) Math.ceil(switch (meta) {
            case MANTER -> tmb;
            case PERDER -> tmb * 0.9;
            case GANHAR -> tmb * 1.06;
        });
    }

    /**
     * Calcula a meta diária de proteínas em gramas (baseada no peso).
     *
     * @param pesoKg Peso atual do usuário em kg.
     * @return Quantidade de proteínas em gramas (2g por kg de peso).
     */
    public double calcularMetaProteinas(double pesoKg) {
        return 2 * pesoKg;
    }

    /**
     * Calcula a meta diária de gorduras em gramas.
     *
     * @param metaCal Meta calórica diária total.
     * @param pesoKg Peso atual do usuário em kg.
     * @return Quantidade de gorduras em gramas (25% das calorias totais).
     */
    public double calcularMetaGorduras(double metaCal, double pesoKg) {
        return Math.floor((metaCal * 0.25) / 9);
    }

    /**
     * Calcula a meta diária de carboidratos em gramas.
     *
     * @param metaCal Meta calórica diária total.
     * @param prot Meta de proteínas em gramas.
     * @param gord Meta de gorduras em gramas.
     * @return Quantidade de carboidratos em gramas (calorias restantes).
     */
    public double calcularMetaCarboidratos(double metaCal, double prot, double gord) {
        return Math.floor((metaCal - (prot * 4 + gord * 9)) / 4);
    }

    /**
     * Valida os dados básicos do usuário antes da criação.
     *
     * @throws IllegalArgumentException Se algum dado for inválido.
     * @throws NullPointerException Se nome, email ou senha forem nulos.
     */
    private void validarDados(String nome, String email, String senha, double pesoKg, int alturaCm) {
        Objects.requireNonNull(nome, "Nome não pode ser nulo");
        Objects.requireNonNull(email, "Email não pode ser nulo");
        Objects.requireNonNull(senha, "Senha não pode ser nula");

        if (nome.trim().isEmpty()) throw new IllegalArgumentException("Nome não pode ser vazio");
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) throw new IllegalArgumentException("Email inválido");
        if (senha.length() < 6) throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        if (pesoKg <= 0) throw new IllegalArgumentException("Peso deve ser positivo");
        if (alturaCm <= 0) throw new IllegalArgumentException("Altura deve ser positiva");
    }

    // --- Getters --- //

    /** @return Id do usuário. */
    public int getId() { return id; }

    /** @return Nome do usuário. */
    public String getNome() { return nome; }

    /** @return Email do usuário. */
    public String getEmail() { return email; }

    /**
     * @return Senha do usuário.
     */
    public String getSenha() { return senha; }

    /** @return Mapa imutável do histórico de pesos (calorize.data -> peso). */
    // Mudar aqui para TreeMap para que o retorno reflita o tipo real
    public Map<LocalDate, Double> getHistoricoPeso() {
        return Collections.unmodifiableMap(historicoPeso);
    }

    /** @return Peso atual ou null se não houver histórico. */
    public Double getPesoAtual() {
        // Agora historicoPeso é um TreeMap, então lastKey() está disponível
        if (historicoPeso.isEmpty()) return null;
        return historicoPeso.get(historicoPeso.lastKey());
    }

    /** @return Altura em centímetros. */
    public int getAlturaCm() { return alturaCm; }

    /** @return Gênero do usuário. */
    public Genero getGenero() { return genero; }

    /** @return Meta de peso do usuário. */
    public Meta getMeta() { return meta; }

    /** @return Metas nutricionais calculadas. */
    public MetaMacros getMetaMacros() { return metaMacros; }

    /** @return Lista imutável de refeições registradas. */
    public List<Refeicao> getRefeicoes() {
        return Collections.unmodifiableList(refeicoes);
    }

    /** @return Lista imutável de amigos do usuário. */
    public List<Usuario> getAmigos() {
        return Collections.unmodifiableList(amigos);
    }

    // --- Setters (Adicionados onde faz sentido para atualização do usuário) --- //

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio ou nulo.");
        }
        this.nome = nome;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("Email inválido.");
        }
        this.email = email;
    }

    public void setSenha(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres.");
        }
        this.senha = senha;
    }

    public void setAlturaCm(int alturaCm) {
        if (alturaCm <= 0) {
            throw new IllegalArgumentException("Altura deve ser positiva.");
        }
        this.alturaCm = alturaCm;
    }

    public void setGenero(char generoChar) {
        this.genero = switch (generoChar) {
            case 'M' -> Genero.MASCULINO;
            case 'F' -> Genero.FEMININO;
            default -> Genero.OUTRO; // Assumindo Genero.OUTRO
        };
    }

    public void setMeta(char metaChar) {
        this.meta = switch (metaChar) {
            case 'P' -> Meta.PERDER;
            case 'M' -> Meta.MANTER;
            case 'G' -> Meta.GANHAR;
            default -> Meta.MANTER;
        };
    }

    public void setMetaMacros(MetaMacros metaMacros) {
        this.metaMacros = metaMacros;
    }


    // --- Métodos de modificação --- //

    /**
     * Adiciona um novo registro de peso ao histórico com a calorize.data atual.
     * Se já houver um registro para a calorize.data atual, ele é atualizado.
     *
     * @param peso Novo peso em kg (deve ser positivo).
     * @throws IllegalArgumentException Se o peso for inválido.
     */
    public void adicionarPeso(double peso) {
        if (peso <= 0) throw new IllegalArgumentException("Peso inválido");
        historicoPeso.put(LocalDate.now(), peso);
    }

    /**
     * <p>Substitui o histórico de peso existente por um novo mapa de histórico.</p>
     * <p>Este método é usado para carregar o histórico de peso do banco de dados
     * para o objeto {@link Usuario} em memória.</p>
     *
     * @param novoHistorico O novo mapa contendo as datas e pesos. Não pode ser nulo.
     */
    // Modificar o parâmetro para Map se o DAO retornar um Map genérico
    public void setHistoricoPeso(Map<LocalDate, Double> novoHistorico) {
        Objects.requireNonNull(novoHistorico, "O histórico de peso não pode ser nulo.");
        // Cria uma nova instância de TreeMap a partir do mapa recebido
        this.historicoPeso = new TreeMap<>(novoHistorico);
    }


    /**
     * Adiciona um amigo à lista de amigos.
     *
     * @param amigo Usuário a ser adicionado (não pode ser nulo ou o próprio usuário).
     * @throws IllegalArgumentException Se o amigo for o próprio usuário.
     * @throws NullPointerException Se o amigo for nulo.
     */
    public void adicionarAmigo(Usuario amigo) {
        Objects.requireNonNull(amigo, "Amigo não pode ser nulo");
        if (this.equals(amigo)) throw new IllegalArgumentException("Usuário não pode se adicionar como amigo");
        amigos.add(amigo);
    }

    /**
     * Adiciona uma refeição ao registro diário.
     *
     * @param refeicao Refeição a ser registrada (não pode ser nula).
     * @throws NullPointerException Se a refeição for nula.
     */
    public void adicionarRefeicao(Refeicao refeicao) {
        Objects.requireNonNull(refeicao, "Refeição não pode ser nula");
        refeicoes.add(refeicao);
    }


    // --- Sobrescrita de métodos para equals e hashCode (importante para comparações de objetos) --- //

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id == usuario.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}