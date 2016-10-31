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

    public SessionFactory buildSession(DBConfig config)
    {
        try
        {
            this.mainConfig = config;
            mainDataSource = DataSource.getInstance(config);
            return this;
        }
        catch (Exception ex)
        {
            System.err.println("Persistor: build SessionFactory error at: \n");
            ex.printStackTrace();
        }
        return null;
    }

    public DBConfig getConfig()
    {
        return this.mainConfig;
    }

    public Session getSession()
    {
        SessionImpl returnSessonImpl = null;

        try
        {
            returnSessonImpl = new SessionImpl(mainDataSource.getConnection());
            returnSessonImpl.setConfig(mainConfig);

        }
        catch (Exception ex)
        {
            System.err.println("Persistor: create session error at: \n");
            ex.printStackTrace();
        }

        return returnSessonImpl;
    }

    public Session getSession(DBConfig config) throws Exception
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

        }
        catch (Exception ex)
        {
            System.err.println("Persistor: create session error at: \n");
            throw new Exception(ex.getMessage());
        }

        return returnSessonImpl;
    }
}
