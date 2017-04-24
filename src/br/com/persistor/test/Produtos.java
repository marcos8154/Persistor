/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.NamedQuery;
import br.com.persistor.annotations.OneToOne;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;

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
    private String ean;
    private String local_estoque;
    private double preco_custo;
    private double margem_lucro;
    private double preco_venda;
    private int marca_id;

    private Marcas marcas;

    @PrimaryKey(increment = INCREMENT.AUTO)
    public int getId()
    {
        return id;
    }

    public int getMarca_id()
    {
        return marca_id;
    }

    public void setMarca_id(int marca_id)
    {
        this.marca_id = marca_id;
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

    public String getEan()
    {
        return ean;
    }

    public void setEan(String ean)
    {
        this.ean = ean;
    }

    public String getLocal_estoque()
    {
        return local_estoque;
    }

    public void setLocal_estoque(String local_estoque)
    {
        this.local_estoque = local_estoque;
    }

    public double getPreco_custo()
    {
        return preco_custo;
    }

    public void setPreco_custo(double preco_custo)
    {
        this.preco_custo = preco_custo;
    }

    public double getMargem_lucro()
    {
        return margem_lucro;
    }

    public void setMargem_lucro(double margem_lucro)
    {
        this.margem_lucro = margem_lucro;
    }

    public double getPreco_venda()
    {
        return preco_venda;
    }

    public void setPreco_venda(double preco_venda)
    {
        this.preco_venda = preco_venda;
    }

    @OneToOne(source = "marca_id", target = "id", join_type = JOIN_TYPE.INNER, load = LOAD.AUTO)
    public Marcas getMarcas()
    {
        return marcas;
    }

    public void setMarcas(Marcas marcas)
    {
        this.marcas = marcas;
    }

}
