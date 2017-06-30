/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.interfaces;

import br.com.persistor.generalClasses.ColumnProperties;
import br.com.persistor.generalClasses.DBConfig;

/**
 *
 * @author Marcos Vinícius
 */
public abstract class DbMigration
{

    public String migrationScript = "";
    private DBConfig config;

    public void addColumn(String tableName,
            ColumnProperties properties)
    {
        migrationScript += "alter table "
                + tableName + " add " + properties.getColumnDescription() + ";\n";
    }

    public void alterColumn(String tableName, ColumnProperties properties)
    {
        migrationScript += "alter table "
                + tableName + " ";

        switch (config.getDb_type())
        {
            case MySQL:
                migrationScript += "modify " + properties.getColumnDescription() + "; \n";
                break;

            case SQLServer:
                migrationScript += "alter column " + properties.getColumnDescription() + "; \n";
                break;

            case PostgreSQL:
                migrationScript += "alter column " + properties.getColumnDescription() + "; \n";
                break;

            case ORACLE:
                migrationScript += "modify " + properties.getColumnDescription() + "; \n";
                break;

            case FirebirdSQL:
                migrationScript += "alter column " + properties.getColumnDescription() + "; \n";
                break;
        }
    }
    
    public void addSqlStatement(String sql)
    { 
        migrationScript += sql + "\n";
    }

    public void dropColumn(String tableName, String columnName)
    {
        migrationScript += "alter table " + tableName + " drop column " + columnName + ";\n";
    }

    public void setDbConfig(DBConfig config)
    {
        this.config = config;
    }

    public abstract int getVesion();

    public abstract void up();

    public abstract void down();
}
