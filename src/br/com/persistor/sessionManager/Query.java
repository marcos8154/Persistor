/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

import br.com.persistor.enums.PARAMETER_TYPE;

/**
 *
 * @author marcosvinicius
 */
public class Query
{
    public void createQuery(SessionFactory session, Class cls, String query)
    {
        //if "query" starts with "@", is an NamedQuery.
        //Find in Class "cls" the NamedQuery
    }
    
    public void setParameter(PARAMETER_TYPE parameter_type, int parameter_index, Object value)
    {
        
    }
    
    public void execute()
    {
        
    }
}
