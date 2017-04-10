/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.NamedQuery;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.annotations.Version;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.PRIMARYKEY_TYPE;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Marcos VinÃ­cius
 */
@Deprecated
@NamedQuery(queryName = "busca", queryValue = "select * from produtos where descricao like '%%'")
public class Produtos extends Entity
{

    private int id;
    private String descricao;
    private double valor;
    private String unidade;
    private boolean inativo;

  //  @PrimaryKey(increment = INCREMENT.MANUAL)
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

    public double getValor()
    {
        return valor;
    }

    public void setValor(double valor)
    {
        this.valor = valor;
    }

    public String getUnidade()
    {
        return unidade;
    }

    public void setUnidade(String unidade)
    {
        this.unidade = unidade;
    }

    public boolean isInativo()
    {
        return inativo;
    }

    public void setInativo(boolean inativo)
    {
        this.inativo = inativo;
    }

}
