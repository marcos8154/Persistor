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
public class Usuarios extends Entity
{
    private String id;
    private String nome;
    private String senha;
    private boolean ativo;
    private boolean admin;
    private int grupo_usuarios_id;

    @PrimaryKey(increment = INCREMENT.MANUAL)
    public String getId()
    {
        return id;
    }

    public void setId(String id)
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

    public String getSenha()
    {
        return senha;
    }

    public void setSenha(String senha)
    {
        this.senha = senha;
    }

    public boolean isAtivo()
    {
        return ativo;
    }

    public void setAtivo(boolean ativo)
    {
        this.ativo = ativo;
    }

    public boolean isAdmin()
    {
        return admin;
    }

    public void setAdmin(boolean admin)
    {
        this.admin = admin;
    }

    public int getGrupo_usuarios_id()
    {
        return grupo_usuarios_id;
    }

    public void setGrupo_usuarios_id(int grupo_usuarios_id)
    {
        this.grupo_usuarios_id = grupo_usuarios_id;
    }
      
}
