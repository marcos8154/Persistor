/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.INCREMENT;

/**
 *
 * @author Marcos Vinícius
 */
public class Estoque extends Entity
{

    private int id;
    private int produto_id;
    private double quant;

    @PrimaryKey(increment = INCREMENT.AUTO)
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getProduto_id()
    {
        return produto_id;
    }

    public void setProduto_id(int produto_id)
    {
        this.produto_id = produto_id;
    }

    public double getQuant()
    {
        return quant;
    }

    public void setQuant(double quant)
    {
        this.quant = quant;
    }

}
