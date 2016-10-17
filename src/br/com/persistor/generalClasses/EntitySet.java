/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

/**
 *
 * @author Marcos Vinícius
 * @param <T>
 */
public class EntitySet<T>
{
    private T entity;

    public T getEntity()
    {
        return this.entity;
    }

    public EntitySet(T entity)
    {
        this.entity = entity;
    }
}
