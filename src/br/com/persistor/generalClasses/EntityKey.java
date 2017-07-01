/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

/**
 *
 * @author Marcos Vinícius
 */
public class EntityKey
{

    private String keyField;
    private int keyValue;

    private EntityKey(String key, int value)
    {
        this.keyField = key;
        this.keyValue = value;
    }

    public static EntityKey set(String key, int value)
    {
        return new EntityKey(key, value);
    }

    public String getKeyField()
    {
        return keyField;
    }

    public void setKeyField(String keyField)
    {
        this.keyField = keyField;
    }

    public int getKeyValue()
    {
        return keyValue;
    }

    public void setKeyValue(int keyValue)
    {
        this.keyValue = keyValue;
    }
}
