/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import br.com.persistor.abstractClasses.Entity;
import java.util.Date;

/**
 *
 * @author Marcos Vinícius
 */
public class MigrationHistory extends Entity
{
    private Date migrationDate;
    private int currentVersion;

    public Date getMigrationDate()
    {
        return migrationDate;
    }

    public void setMigrationDate(Date migrationDate)
    {
        this.migrationDate = migrationDate;
    }

    public int getCurrentVersion()
    {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion)
    {
        this.currentVersion = currentVersion;
    }
    
    
}
