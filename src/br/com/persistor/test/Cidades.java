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
@Deprecated
public class Cidades extends Entity
{

    private int ordem;
    private String cidade;
    private String uf;
    private int cod_uf;
    private int cod_cidade;

    @PrimaryKey(increment = INCREMENT.MANUAL)
    public int getOrdem()
    {
        return ordem;
    }

    public void setOrdem(int ordem)
    {
        this.ordem = ordem;
    }

    public String getCidade()
    {
        return cidade;
    }

    public void setCidade(String cidade)
    {
        this.cidade = cidade;
    }

    public String getUf()
    {
        return uf;
    }

    public void setUf(String uf)
    {
        this.uf = uf;
    }

    public int getCod_uf()
    {
        return cod_uf;
    }

    public void setCod_uf(int cod_uf)
    {
        this.cod_uf = cod_uf;
    }

    public int getCod_cidade()
    {
        return cod_cidade;
    }

    public void setCod_cidade(int cod_cidade)
    {
        this.cod_cidade = cod_cidade;
    }
    
}
