/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.PRIMARYKEY_TYPE;

/**
 * 
 * @author Marcos Vinícius
 */
public class Cliente extends Entity
{
    private int id;
    private int loja;
    private String nome;

    @PrimaryKey(increment = INCREMENT.MANUAL)
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    @PrimaryKey(increment = INCREMENT.MANUAL, primarykey_type = PRIMARYKEY_TYPE.AUXILIAR)
    public int getLoja()
    {
        return loja;
    }

    public void setLoja(int loja)
    {
        this.loja = loja;
    }

    public String getNome()
    {
        return nome;
    }

    public void setNome(String nome)
    {
        this.nome = nome;
    }
}
