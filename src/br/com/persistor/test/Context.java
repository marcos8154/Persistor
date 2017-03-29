/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.generalClasses.EntitySet;
import br.com.persistor.interfaces.IPersistenceContext;

/**
 *
 * @author Marcos Vinícius
 */
@Deprecated
public class Context implements IPersistenceContext
{

    private EntitySet<Produtos> produtos;

    public EntitySet<Produtos> getProdutos()
    {
        return produtos;
    }

    public void setProdutos(EntitySet<Produtos> produtos)
    {
        this.produtos = produtos;
    }

}
