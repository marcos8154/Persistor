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

    private EntitySet<Produtos> pessoa;
    private EntitySet<Marcas> marca;

    public EntitySet<Produtos> getPessoas()
    {
        return pessoa;
    }

    public void setPessoas(EntitySet<Produtos> pessoas)
    {
        this.pessoa = pessoas;
    }

    public EntitySet<Marcas> getMarcas()
    {
        return marca;
    }

    public void setMarcas(EntitySet<Marcas> marcas)
    {
        this.marca = marcas;
    }

}
