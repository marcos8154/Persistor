/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.interfaces.IPersistenceLogger;
import java.util.Date;

/**
 *
 * @author Marcos Vinícius
 */
@Deprecated
public class LogTest implements IPersistenceLogger
{

    @Override
    public void newNofication(String className, String methodName, String date, String description, String query)
    {
        System.out.println(" Classe: " + className);
        System.out.println(" Metodo: " + methodName);
        System.out.println(" Data: " + date);
        System.out.println(" Descricao: " + description);
        System.out.println(" Query: " + query);
    }

}
