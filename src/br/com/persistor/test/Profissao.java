package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.PrimaryKey;

public class Profissao extends Entity
{
    private int id;
    private String descricao;
    private int pessoa_id;

    public int getPessoa_id()
    {
        return pessoa_id;
    }

    public void setPessoa_id(int pessoa_id)
    {
        this.pessoa_id = pessoa_id;
    }

    @PrimaryKey
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
