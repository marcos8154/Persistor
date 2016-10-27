/* 17/10/2016 11:07:10 */
 /* AUTO-GENERATED CLASS */
 /* DOES NOT ADD ACCESSOR METHODS */
 /* DOES NOT CHANGE NAME OF ACCESSOR METHODS */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.Column;
import br.com.persistor.annotations.NamedQuery;
import br.com.persistor.annotations.NamedQueryes;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.annotations.OneToOne;
import br.com.persistor.annotations.Version;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;

/**
 *
 * @author Persistor4J
 */
@Deprecated

@NamedQueryes(
{
    @NamedQuery(queryName = "listarPorNome", queryValue = "select * from pessoa"),
    @NamedQuery(queryName = "listarPorNomes", queryValue = "select * from pessoa")
})
public class Pessoa extends Entity
{

    private int id;
    private String nome;
    private int version;
    private int veiculo_id;
    private Veiculo veiculo;

    @Version
    @Column(name = "teste")
    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    @PrimaryKey(increment = INCREMENT.AUTO)
    public int getId()
    {
        return id;
    }

    public void setNome(String nome)
    {
        this.nome = nome;
    }

    public String getNome()
    {
        return nome;
    }

    public void setVeiculo(Veiculo veiculo)
    {
        this.veiculo = veiculo;
    }

    public void setVeiculo_id(int veiculo_id)
    {
        this.veiculo_id = veiculo_id;
    }

    public int getVeiculo_id()
    {
        return veiculo_id;
    }

    @OneToOne(source = "veiculo_id", target = "id", load = LOAD.MANUAL, join_type = JOIN_TYPE.INNER)
    public Veiculo getVeiculo()
    {
        return veiculo;
    }
}
