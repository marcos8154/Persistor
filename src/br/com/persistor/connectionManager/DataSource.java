package br.com.persistor.connectionManager;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import br.com.persistor.generalClasses.DBConfig;

public class DataSource
{

    public static DataSource datasource;
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
        cpds.setAcquireRetryAttempts(1);

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
    
    public void reset()
    {
        DataSources.clear();
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
                return "jdbc:sqlserver://" + config.getHost() + ":" + config.getPort() + ";Databasename=" + config.getDatabase();
            case FirebirdSQL:
                return "jdbc:firebirdsql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase() + "?encoding=ISO8859_1";
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
