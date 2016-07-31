/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

import br.com.persistor.connectionManager.DataSource;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.interfaces.Session;

/**
 *
 * @author Marcos Vinícius
 */
public class SessionFactory
{

    private DataSource mainDataSource = null;
    private DBConfig mainConfig = null;

    private void buildSession(DBConfig config)
    {
        try
        {
            mainDataSource = DataSource.getInstance(config);

        } catch (Exception ex)
        {
            System.err.println("Persistor: error at: \n");
            ex.printStackTrace();
        }
    }

    public Session getSession(DBConfig config)
    {
        SessionImpl returnSessonImpl = null;

        try
        {
            if (mainDataSource == null)
            {
                buildSession(config);
            }

            returnSessonImpl = new SessionImpl(mainDataSource.getConnection());
            returnSessonImpl.setConfig(config);

        } catch (Exception ex)
        {
            System.err.println("Persistor: create session error at: \n");
            ex.getMessage();
        }

        return returnSessonImpl;
    }
}
