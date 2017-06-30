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
public class Versao3 extends DbMigration
{

    @Override
    public int getVesion()
    {
        return 3;
    }

    @Override
    public void up()
    {
        addColumn("produtos", ColumnProperties.get("inativo", "boolean", false, 0, 0, false));
    }

    @Override
    public void down()
    {
        dropColumn("produtos", "inativo");
    }

}
