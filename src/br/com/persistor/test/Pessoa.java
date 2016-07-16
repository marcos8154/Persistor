/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.NamedQuery;
import br.com.persistor.annotations.OneToMany;
import br.com.persistor.annotations.OneToOne;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.annotations.Version;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;
import java.util.List;

/**
 *
 * @author Marcos Vinícius
 */

@NamedQuery(name = "selecionaPessoa", value = "select * from pessoa")
public class Pessoa extends Entity {

    
    private int id;
    private String nome;
    private String telefone;
    private String email;
    private String endereco;
    private int version;
    private int veiculo_id;
    private boolean habilitado;

    public boolean isHabilitado()
    {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado)
    {
        this.habilitado = habilitado;
    }
    
    private Veiculo veiculo;

    @OneToMany(source = "id", target = "pessoa_id", join_type = JOIN_TYPE.LEFT, load = LOAD.MANUAL)
    public Profissao getProfissao() {
        return profissao;
    }

    public void setProfissao(Profissao profissao) {
        this.profissao = profissao;
    }
    private Profissao profissao;
    
    public List<Pessoa> lista()
    {
        List<Pessoa> lp = this.ResultList;
        return  lp;
    }        
    
    public int getVeiculo_id()
    {
        return veiculo_id;
    }

    public void setVeiculo_id(int veiculo_id)
    {
        this.veiculo_id = veiculo_id;
    }

    @OneToOne(source = "veiculo_id", target = "id", join_type = JOIN_TYPE.INNER, load = LOAD.AUTO)
    public Veiculo getVeiculo()
    {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo)
    {
        this.veiculo = veiculo;
    }

    @PrimaryKey(increment = INCREMENT.MANUAL)
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getNome()
    {
        return nome;
    }

    public void setNome(String nome)
    {
        this.nome = nome;
    }

    public String getTelefone()
    {
        return telefone;
    }

    public void setTelefone(String telefone)
    {
        this.telefone = telefone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getEndereco()
    {
        return endereco;
    }

    public void setEndereco(String endereco)
    {
        this.endereco = endereco;
    }

    @Version
    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }
}
