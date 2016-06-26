/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import abstractClasses.Entity;
import annotations.OneToOne;
import annotations.PrimaryKey;
import annotations.Version;
import enums.JOIN_TYPE;
import enums.LOAD;

/**
 *
 * @author Marcos Vinícius
 */
public class Pessoa extends Entity {

    private int id;
    private String nome;
    private String telefone;
    private String email;
    private String endereco;
    private int version;
    private int veiculo_id;
    private int profissao_id;
    private Profissao profissao = new Profissao();
    private Veiculo veiculo = new Veiculo();

    public int getVeiculo_id() {
        return veiculo_id;
    }

    public void setVeiculo_id(int veiculo_id) {
        this.veiculo_id = veiculo_id;
    }

    public int getProfissao_id() {
        return profissao_id;
    }

    public void setProfissao_id(int profissao_id) {
        this.profissao_id = profissao_id;
    }

    @OneToOne(source = "profissao_id", target = "id", join_type = JOIN_TYPE.INNER, load = LOAD.AUTO)
    public Profissao getProfissao() {
        return profissao;
    }

    public void setProfissao(Profissao profissao) {
        this.profissao = profissao;
    }

    @OneToOne(source = "veiculo_id", target = "id", join_type = JOIN_TYPE.INNER, load = LOAD.AUTO)
    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    @PrimaryKey
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    @Version
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

}
