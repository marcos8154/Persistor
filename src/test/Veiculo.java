/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import abstractClasses.Entity;
import annotations.PrimaryKey;

/**
 *
 * @author Marcos Vinícius
 */
public class Veiculo extends Entity
{
    private int id;
    private String nome;

    @PrimaryKey
    public int getId() {
        return id;
    }

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
