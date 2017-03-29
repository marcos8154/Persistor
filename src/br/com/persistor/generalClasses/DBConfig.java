package br.com.persistor.generalClasses;

import br.com.persistor.enums.DB_TYPE;

public class DBConfig
{

    private String persistenceContext;
    private String persistenceLogger;
    private String slPersistenceContext = null;

    private DB_TYPE db_type;
    private String host;
    private String database;
    private String user;
    private String password;
    private int port;

    private int minPoolSize = 3;
    private int acquireIncrement = 1;
    private int maxPoolSize = 20;
    private int maxStatements = 180;

    public String getSlPersistenceContext()
    {
        return slPersistenceContext;
    }

    public void setSlPersistenceContext(Class slPersistenceContextClass)
    {
        this.slPersistenceContext = slPersistenceContextClass.getName();
    }

    public String getPersistenceLogger()
    {
        return persistenceLogger;
    }

    public void setPersistenceLogger(Class persistenceLoggerClass)
    {
        this.persistenceLogger = persistenceLoggerClass.getName();
    }

    public String getPersistenceContext()
    {
        return persistenceContext;
    }

    public void setPersistenceContext(Class persistenceContextClass)
    {
        this.persistenceContext = persistenceContextClass.getName();
    }

    public int getMinPoolSize()
    {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize)
    {
        this.minPoolSize = minPoolSize;
    }

    public int getAcquireIncrement()
    {
        return acquireIncrement;
    }

    public void setAcquireIncrement(int acquireIncrement)
    {
        this.acquireIncrement = acquireIncrement;
    }

    public int getMaxPoolSize()
    {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize)
    {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMaxStatements()
    {
        return maxStatements;
    }

    public void setMaxStatements(int maxStatements)
    {
        this.maxStatements = maxStatements;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public DB_TYPE getDb_type()
    {
        return db_type;
    }

    public void setDb_type(DB_TYPE db_type)
    {
        this.db_type = db_type;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getDatabase()
    {
        return database;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
