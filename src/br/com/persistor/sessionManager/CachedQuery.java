/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

/**
 *
 * @author Marcos Vinícius
 */
public class CachedQuery
{

    private String query;
    private int[] resultKeys;

    public CachedQuery(String query, int[] resultKeys)
    {
        this.query = query;
        this.resultKeys = resultKeys;
    }
    
    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public int[] getResultKeys()
    {
        return resultKeys;
    }

    public void setResultKeys(int[] resultKeys)
    {
        this.resultKeys = resultKeys;
    }
}
