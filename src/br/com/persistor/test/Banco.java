/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

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
            db.addTableDomain(getMarcas());
            db.addTableDomain(getProdutos());
            db.createTables(config);
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    private CodeFirstTableDomain getMarcas()
    {
        return new CodeFirstTableDomain(Marcas.class)
                .setColumnProperty("id", false, 0, 0, 0)
                .setColumnProperty("nome", false, 80, 0, null)
                .setColumnProperty("inativo", false, 0, 0, false);
    }

    private CodeFirstTableDomain getProdutos()
    {
        return new CodeFirstTableDomain(Produtos.class)
                .setColumnProperty("id", false, 0, 0, null)
                .setColumnProperty("descricao", false, 100, 0, null)
                .setColumnProperty("ean", false, 13, 0, null)
                .setColumnProperty("local_estoque", true, 50, 0, null)
                .setColumnProperty("preco_custo", false, 10, 2, 0)
                .setColumnProperty("margem_lucro", false, 10, 2, 0)
                .setColumnProperty("preco_venda", false, 10, 2, 0)
                .setColumnProperty("marca_id", false, 0, 0, null);
    }
}
