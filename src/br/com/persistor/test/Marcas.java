/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.OneToMany;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;
import br.com.persistor.sessionManager.FieldHandled;

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

    private Produtos produto;

    @OneToMany(source = "id", target = "marca_id", join_type = JOIN_TYPE.LEFT, load = LOAD.AUTO)
    public Produtos getProduto()
    {
        if(produto == null)
            produto = (Produtos)FieldHandled.readObject(this, "produto");
        return produto;
    }

    public void setProduto(Produtos produto)
    {
        this.produto = produto;
    }
    
    
    
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
