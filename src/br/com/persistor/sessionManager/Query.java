/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

import br.com.persistor.enums.PARAMETER_TYPE;
import br.com.persistor.enums.ResultType;

/**
 *
 * @author marcosvinicius
 */
public class Query
{
    public void createQuery(SessionFactory session, ResultType result_type, Class cls, String query)
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
