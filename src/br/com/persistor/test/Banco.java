/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.generalClasses.CodeFirstDatabase;
import br.com.persistor.generalClasses.CodeFirstTableDomain;
import br.com.persistor.generalClasses.DBConfig;

/**
 *
 * @author Marcos Vinícius
 */
@Deprecated
public class Banco
{
    
    public void Criar(DBConfig config)
    {
        try
        {
            CodeFirstDatabase db = new CodeFirstDatabase(true);
            db.addTableDomain(Marcas());
            db.createTables(config);
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    private CodeFirstTableDomain Marcas()
    {
        return new CodeFirstTableDomain(Marcas.class)
                .setColumnProperty("nome", false, 50, 0, null)
                .setColumnProperty("foto_id", false, 0, 0, 0)
                .setColumnProperty("valordouble", false, 10, 2, 10)
                .setColumnProperty("id", false, 0, 0, 0);
    }
}
