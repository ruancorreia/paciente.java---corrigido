package model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "paciente")
public class Paciente {
    // Constantes
    public static final int TAMANHO_TIPO_SANGUINEO = 3;
    public static final int TAMANHO_NUMERO_SUS = 14;

    // Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = TAMANHO_TIPO_SANGUINEO)
    @Enumerated(EnumType.STRING)
    private TipoSanguineo tipoSanguineo;

    @Column(nullable = false, unique = true, length = TAMANHO_NUMERO_SUS)
    private String numeroSus;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "plano_id", referencedColumnName = "id", nullable = false)
    private Plano plano;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "registro_pessoa_id", referencedColumnName = "id", nullable = false)
    private RegistroPessoa registroPessoa;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    @OneToMany(mappedBy = "paciente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Consulta> listaConsultas = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference("paciente-aplicacoes")
    private List<AplicacaoDose> listaAplicacoes = new ArrayList<>();

    // Construtores
    public Paciente() {
    }

    public Paciente(TipoSanguineo tipoSanguineo,
                    String numeroSus,
                    Plano plano,
                    RegistroPessoa registroPessoa,
                    Usuario usuarioCadastrante) throws ModelException {
        this.setTipoSanguineo(tipoSanguineo);
        this.setNumeroSus(numeroSus);
        this.setPlano(plano);
        this.setRegistroPessoa(registroPessoa);
        this.setUsuario(usuarioCadastrante);
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public TipoSanguineo getTipoSanguineo() {
        return tipoSanguineo;
    }

    public void setTipoSanguineo(TipoSanguineo tipoSanguineo) throws ModelException {
        validarTipoSanguineo(tipoSanguineo);
        this.tipoSanguineo = tipoSanguineo;
    }

    public String getNumeroSus() {
        return numeroSus;
    }

    public void setNumeroSus(String numeroSus) throws ModelException {
        validarNumeroSus(numeroSus);
        this.numeroSus = numeroSus;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) throws ModelException {
        validarPlano(plano);
        this.plano = plano;
    }

    public RegistroPessoa getRegistroPessoa() {
        return registroPessoa;
    }

    public void setRegistroPessoa(RegistroPessoa registroPessoa) throws ModelException {
        validarRegistroPessoa(registroPessoa);
        this.registroPessoa = registroPessoa;
        registroPessoa.setPaciente(this);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuarioCadastrante) throws ModelException {
        validarUsuarioCadastrante(usuarioCadastrante);
        this.usuario = usuarioCadastrante;
        usuarioCadastrante.adicionarPaciente(this);
    }

    public List<Consulta> getListaConsultas() {
        return new ArrayList<>(this.listaConsultas);
    }

    public void setListaConsultas(List<Consulta> listaConsultas) {
        this.listaConsultas = listaConsultas != null ? listaConsultas : new ArrayList<>();
    }

    public boolean adicionarConsulta(Consulta consulta) throws ModelException {
        if (consulta == null)
            throw new ModelException("A consulta não pode ser nula.");

        boolean adicionada = !this.listaConsultas.contains(consulta);
        if (adicionada)
            this.listaConsultas.add(consulta);

        if (consulta.getPaciente() != this)
            consulta.setPaciente(this);

        return adicionada;
    }

    public boolean removerConsulta(Consulta consulta) {
        return this.listaConsultas.remove(consulta);
    }

    public List<AplicacaoDose> getListaAplicacoes() {
        return new ArrayList<>(this.listaAplicacoes);
    }

    public void setListaAplicacoes(List<AplicacaoDose> listaAplicacoes) {
        this.listaAplicacoes = listaAplicacoes != null ? listaAplicacoes : new ArrayList<>();
    }

    public boolean adicionarAplicacao(AplicacaoDose aplicacao) throws ModelException {
        if (aplicacao == null)
            throw new ModelException("A aplicação de dose não pode ser nula.");

        boolean adicionada = !this.listaAplicacoes.contains(aplicacao);
        if (adicionada)
            this.listaAplicacoes.add(aplicacao);

        if (aplicacao.getPaciente() != this)
            aplicacao.setPaciente(this);

        return adicionada;
    }

    public boolean removerAplicacao(AplicacaoDose aplicacao) {
        return this.listaAplicacoes.remove(aplicacao);
    }

    /**
     * Mantido para compatibilidade com chamadas existentes.
     */
    public List<AplicacaoDose> getListaAplicacaoDose() {
        return getListaAplicacoes();
    }

    /**
     * Mantido para compatibilidade com chamadas existentes.
     */
    public void setListaAplicacaoDose(List<AplicacaoDose> listaAplicacaoDose) {
        setListaAplicacoes(listaAplicacaoDose);
    }

    // Métodos estáticos de validação
    public static void validarTipoSanguineo(TipoSanguineo tipoSanguineo) throws ModelException {
        if (tipoSanguineo == null)
            throw new ModelException("O tipo sanguíneo não pode ser nulo.");
    }

    public static void validarNumeroSus(String numeroSus) throws ModelException {
        if (numeroSus == null || numeroSus.trim().isEmpty() ||
                numeroSus.length() != TAMANHO_NUMERO_SUS)
            throw new ModelException("O número do SUS é inválido: " + numeroSus);
    }

    public static void validarPlano(Plano plano) throws ModelException {
        if (plano == null)
            throw new ModelException("O plano associado ao paciente não pode ser nulo.");
    }

    public static void validarRegistroPessoa(RegistroPessoa registroPessoa) throws ModelException {
        if (registroPessoa == null)
            throw new ModelException("O registro de pessoa não pode ser nulo.");
    }

    public static void validarUsuarioCadastrante(Usuario usuarioCadastrante) throws ModelException {
        if (usuarioCadastrante == null)
            throw new ModelException("O usuário cadastrante associado ao paciente não pode ser nulo.");
    }
}
