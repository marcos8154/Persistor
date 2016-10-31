/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.connectionManager;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.sessionManager.SessionFactory;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Marcos Vinícius
 */
public class Configuration
{

    private static SessionFactory mainSessionFactory = null;

    public static SessionFactory createSessionFactory(String dbConfigFile)
    {
        try
        {
            if (mainSessionFactory == null)
            {
                DBConfig config = buidDBConfig(dbConfigFile);

                if (config == null)
                    return null;

                SessionFactory sessionFactory = new SessionFactory();
                mainSessionFactory = sessionFactory.buildSession(config);
            }
            return mainSessionFactory;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    private static DBConfig buidDBConfig(String dbConfigFile)
    {
        try
        {
            DBConfig config = new DBConfig();

            FileReader fr = new FileReader(dbConfigFile);
            BufferedReader br = new BufferedReader(fr);

            String line = "";
            
            while ((line = br.readLine()) != null)
            {
                line = line.trim();
                if (line.toLowerCase().startsWith("db_type:"))
                {
                    line = line.substring(8, line.length());
                    switch (line)
                    {
                        case "mysql":
                            config.setDb_type(DB_TYPE.MySQL);
                            break;
                        case "postgresql":
                            config.setDb_type(DB_TYPE.PostgreSQL);
                            break;
                        case "firebirdsql":
                            config.setDb_type(DB_TYPE.FirebirdSQL);
                            break;
                        case "sqlserver":
                            config.setDb_type(DB_TYPE.SQLServer);
                            break;
                    }
                }

                if (line.startsWith("host:"))
                {
                    line = line.substring(5, line.length());
                    config.setHost(line);
                }

                if (line.startsWith("user:"))
                {
                    line = line.substring(5, line.length());
                    config.setUser(line);
                }

                if (line.startsWith("port:"))
                {
                    line = line.substring(5, line.length());
                    config.setPort(Integer.parseInt(line));
                }

                if (line.startsWith("password:"))
                {
                    line = line.substring(9, line.length());
                    config.setPassword(line);
                }

                if (line.startsWith("database:"))
                {
                    line = line.substring(9, line.length());
                    config.setDatabase(line);
                }

                if (line.startsWith("persistence_context:"))
                {
                    line = line.substring(20, line.length());
                    config.setPersistenceContext(line);
                }

                if (line.startsWith("persistence_logger:"))
                {
                    line = line.substring(19, line.length());
                    config.setPersistenceLogger(line);
                }
                
                if(line.startsWith("acquire_increment:"))
                {
                    line = line.substring(18, line.length());
                    config.setAcquireIncrement(Integer.parseInt(line));
                }
                
                if(line.startsWith("max_statements:"))
                {
                    line = line.substring(15, line.length());
                    config.setMaxStatements(Integer.parseInt(line));
                }
                
                if(line.startsWith("max_pool_size:"))
                {
                    line = line.substring(14, line.length());
                    config.setMaxPoolSize(Integer.parseInt(line));
                }
                
                if(line.startsWith("min_pool_size:"))
                {
                    line = line.substring(14, line.length());
                    config.setMinPoolSize(Integer.parseInt(line));
                }
            }

            br.close();
            fr.close();

            return config;
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}
