/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.connection;

import br.com.persistor.generalClasses.DBConfig;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author marcosvinicius
 */
public class DataSources
{
    private static List<DataSource> dataSources = new ArrayList<>();
    
    public static void add(DataSource dataSource)
    {
        dataSources.add(dataSource);
    }
    
    public static DataSource getDataSource(DBConfig config)
    {
        for(DataSource ds : dataSources)
        {
            if(ds.config.getDb_type() == config.getDb_type())
            {
                return ds;
            }
        }
        
        return null;
    }
}
