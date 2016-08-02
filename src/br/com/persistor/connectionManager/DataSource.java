package br.com.persistor.connectionManager;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import br.com.persistor.generalClasses.DBConfig;

public class DataSource
{

    private static DataSource datasource;
    private ComboPooledDataSource cpds;

    public DBConfig config;
    
    private DataSource(DBConfig config) throws IOException, SQLException, PropertyVetoException
    {

        this.config = config;
        
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass(decodeDriver(config)); //loads the jdbc driver
        cpds.setJdbcUrl(dataBaseURL(config));
        cpds.setUser(config.getUser());
        cpds.setPassword(config.getPassword());

        // the settings below are optional -- c3p0 can work with defaults
        cpds.setMinPoolSize(config.getMinPoolSize());
        cpds.setAcquireIncrement(config.getAcquireIncrement());
        cpds.setMaxPoolSize(config.getMaxPoolSize());
        cpds.setMaxStatements(config.getMaxStatements());

    }

    public static DataSource getInstance(DBConfig config) throws IOException, SQLException, PropertyVetoException
    {
        if (DataSources.getDataSource(config) == null)
        {
            datasource = new DataSource(config);
            
            DataSources.add(datasource);
            
            return datasource;

        } else
        {
            return DataSources.getDataSource(config);
        }
    }

    public Connection getConnection() throws SQLException
    {
        Connection connection = this.cpds.getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    private String dataBaseURL(DBConfig config)
    {
        switch (config.getDb_type())
        {
            case MySQL:
                return "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
            case PostgreSQL:
                return "jdbc:postgresql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
            case SQLServer:
                return "jdbc:sqlserver://" + config.getHost() + ":" + config.getPort() + ";" + "databaseName=" + config.getDatabase() + ";user=" + config.getUser() + ";password=" + config.getPassword() + ";";
            case FirebirdSQL:
                return "jdbc:firebirdsql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
        }

        return null;
    }

    private String decodeDriver(DBConfig config)
    {
        switch (config.getDb_type())
        {
            case MySQL:
                return "com.mysql.jdbc.Driver";
            case PostgreSQL:
                return "org.postgresql.Driver";
            case SQLServer:
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case FirebirdSQL:
                return "org.firebirdsql.jdbc.FBDriver";
        }

        return null;
    }
}
