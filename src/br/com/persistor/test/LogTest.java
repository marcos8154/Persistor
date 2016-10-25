/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.generalClasses.PersistenceLog;
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
    public void newNofication(PersistenceLog persistenceLog)
    {
        System.err.println(" Classe: " + persistenceLog.getClassName());
        System.err.println(" Metodo: " + persistenceLog.getMethodName());
        System.err.println(" Data: " + persistenceLog.getDate());
        System.err.println(" Descricao: " + persistenceLog.getDescription());
        System.err.println(" Query: " + persistenceLog.getQuery());
    }

}
