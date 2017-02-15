/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
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
public class Produtos extends Entity
{

    private int id;
    private int loja_id;
    private String nome;
    private int version;
    private Calendar data_nasc;

    @PrimaryKey(increment = INCREMENT.MANUAL, primarykey_type = PRIMARYKEY_TYPE.AUXILIAR)
    public int getLoja_id()
    {
        return loja_id;
    }

    public void setLoja_id(int loja_id)
    {
        this.loja_id = loja_id;
    }

    @PrimaryKey(increment = INCREMENT.MANUAL)
    public int getId()
    {
        return id;
    }

    public Calendar getData_nasc()
    {
        return data_nasc;
    }

    public void setData_nasc(Calendar data_nasc)
    {
        this.data_nasc = data_nasc;
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
