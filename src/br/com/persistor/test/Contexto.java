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
public class Contexto implements IPersistenceContext
{
    private EntitySet<Pessoa> pessoas;
    private EntitySet<Veiculo> veiculos;

    public EntitySet<Veiculo> getVeiculos()
    {
        return veiculos;
    }

    public void setVeiculos(EntitySet<Veiculo> veiculos)
    {
        this.veiculos = veiculos;
    }

 /*   public EntitySet<Pessoa> getPessoas()
    {
        return pessoas;
    }

    public void setPessoas(EntitySet<Pessoa> pessoas)
    {
        this.pessoas = pessoas;
    }
*/

}
