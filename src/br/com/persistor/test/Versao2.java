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
public class Versao2 extends DbMigration
{

    @Override
    public int getVesion()
    {
        return 2;
    }

    @Override
    public void up()
    {
        addColumn("produtos", ColumnProperties.get("TesteNovaColuna", "varchar", false, 100, 0, null));
        dropColumn("produtos", "ean");
    }

    @Override
    public void down()
    {
        addColumn("produtos", ColumnProperties.get("ean", "varchar", false, 100, 0, null));
        dropColumn("produtos", "TesteNovaColuna");
    }

}
