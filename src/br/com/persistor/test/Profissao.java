package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.annotations.*;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;

public class Profissao extends Entity
{
    private int id;
    private String descricao;
    private int pessoa_id;
    private Pessoa pessoa;

    @OneToOne(source = "pessoa_id", target = "id", join_type = JOIN_TYPE.INNER, load = LOAD.AUTO)
    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }
    
    public int getPessoa_id()
    {
        return pessoa_id;
    }

    public void setPessoa_id(int pessoa_id)
    {
        this.pessoa_id = pessoa_id;
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

    public String getDescricao()
    {
        return descricao;
    }

    public void setDescricao(String descricao)
    {
        this.descricao = descricao;
    }
}
