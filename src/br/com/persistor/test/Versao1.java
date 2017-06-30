/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.generalClasses.ColumnProperties;
import br.com.persistor.interfaces.DbMigration;

/**
 *
 * @author Marcos Vinícius
 */
public class Versao1 extends DbMigration
{

    @Override
    public int getVesion()
    {
        return 1;
    }

    @Override
    public void up()
    {
        addColumn("produtos", ColumnProperties.get("ean", "varchar", false, 13, 0, null));
    }

    @Override
    public void down()
    {
    }

}
