
/* AUTO-GENERATED CLASS */
 /* DOES NOT ADD ACCESSOR METHODS */
 /* DOES NOT CHANGE NAME OF ACCESSOR METHODS */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.annotations.OneToOne;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;

public class Pessoa extends Entity
{

    private int id;
    private String nome;
    private int version;
    private int veiculo_id;
    private Veiculo veiculo;

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

    public void setVersion(int version)
    {
        this.version = version;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVeiculo(Veiculo veiculo)
    {
        this.veiculo = veiculo;
    }

    public void setVeiculo_Id(int veiculo_id)
    {
        this.veiculo_id = veiculo_id;
    }

    public int getVeiculo_Id()
    {
        return veiculo_id;
    }

    @OneToOne(source = "veiculo_id", target = "id", load = LOAD.AUTO, join_type = JOIN_TYPE.INNER)
    public Veiculo getVeiculo()
    {
        return veiculo;
    }
}
