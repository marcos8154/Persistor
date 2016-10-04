/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.INCREMENT;
import java.io.InputStream;

/**
 *
 * @author Marcos Vinícius
 */
public class Veiculo extends Entity
{
    
    private int id;
    private String nome;
  //  private InputStream foto;
    
    @PrimaryKey(increment = INCREMENT.MANUAL)
    public int getId() {
        return id;
    }

 /*   public InputStream getFoto()
    {
        return foto;
    }

    public void setFoto(InputStream foto)
    {
        this.foto = foto;
    } */

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
