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
    private int foto_id;
    private double valorDouble;
    private InputStream valorIM;
    private Produtos produtos;

    public InputStream getValorIM()
    {
        return valorIM;
    }

    public void setValorIM(InputStream valorIM)
    {
        this.valorIM = valorIM;
    }

    @OneToOne(source = "produto_id", target = "id", join_type = JOIN_TYPE.LEFT, load = LOAD.AUTO)
    public Produtos getProdutos()
    {
        return produtos;
    }

    public void setProdutos(Produtos produtos)
    {
        this.produtos = produtos;
    }

    public double getValorDouble()
    {
        return valorDouble;
    }

    public void setValorDouble(double valorDouble)
    {
        this.valorDouble = valorDouble;
    }

    @NotNull(message = "nao pode")
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

    public int getFoto_id()
    {
        return foto_id;
    }

    public void setFoto_id(int foto_id)
    {
        this.foto_id = foto_id;
    }

}
