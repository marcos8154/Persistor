/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.interfaces;

import br.com.persistor.generalClasses.PersistenceLog;
import java.util.Date;

/**
 *
 * @author Marcos Vinícius
 */
public interface IPersistenceLogger
{
    public void newNofication(PersistenceLog persistenceLog);
}
