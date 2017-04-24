/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.OneToOne;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;
import java.io.InputStream;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Marcos Vinícius
 */
@Deprecated
public class Marcas extends Entity
{

    private int id;
    private String nome;
    private boolean inativa;

    @PrimaryKey(increment = INCREMENT.AUTO)
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

    public boolean isInativa()
    {
        return inativa;
    }

    public void setInativa(boolean inativa)
    {
        this.inativa = inativa;
    }

}
