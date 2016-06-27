package test;

import abstractClasses.Entity;
import annotations.OneToMany;
import annotations.OneToOne;
import annotations.PrimaryKey;
import enums.JOIN_TYPE;
import enums.LOAD;

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
