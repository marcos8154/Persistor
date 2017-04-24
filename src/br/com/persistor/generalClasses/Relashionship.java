/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import br.com.persistor.annotations.OneToOne;

/**
 *
 * @author Marcos Vinícius
 */
public class Relashionship
{
    private String targetColumn;
    private OneToOne oneToOne;

    public Relashionship(String targetColumn, OneToOne oneToOne)
    {
        this.targetColumn = targetColumn;
        this.oneToOne = oneToOne;
    }
    
    public String getTargetTable()
    {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn)
    {
        this.targetColumn = targetColumn;
    }

    public OneToOne getOneToOne()
    {
        return oneToOne;
    }

    public void setOneToOne(OneToOne oneToOne)
    {
        this.oneToOne = oneToOne;
    }
    
    
}
