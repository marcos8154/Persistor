/* 04/10/2016 22:36:40 */
/* AUTO-GENERATED CLASS */
/* DOES NOT ADD ACCESSOR METHODS */
/* DOES NOT CHANGE NAME OF ACCESSOR METHODS */

package test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.PrimaryKey;
import br.com.persistor.enums.INCREMENT;
import br.com.persistor.annotations.OneToOne;
import br.com.persistor.annotations.OneToMany;
import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;
import java.util.Date;
import java.io.InputStream;

/**
 *
 * @author Persistor4J
 */
public class Usuario extends Entity
{
   private int id_usu; 
   private String nome_usu; 
   private String login_usu; 
   private String senha_usu; 
   private Date data_cadastro_usu; 
   private String status_usu; 

   public void setId_usu(int id_usu)
   {
       this.id_usu = id_usu;
   }

   @PrimaryKey(increment = INCREMENT.AUTO)
   public int getId_usu()
   {
       return id_usu;
   }

   public void setNome_usu(String nome_usu)
   {
       this.nome_usu = nome_usu;
   }

   public String getNome_usu()
   {
       return nome_usu;
   }

   public void setLogin_usu(String login_usu)
   {
       this.login_usu = login_usu;
   }

   public String getLogin_usu()
   {
       return login_usu;
   }

   public void setSenha_usu(String senha_usu)
   {
       this.senha_usu = senha_usu;
   }

   public String getSenha_usu()
   {
       return senha_usu;
   }

   public void setData_cadastro_usu(Date data_cadastro_usu)
   {
       this.data_cadastro_usu = data_cadastro_usu;
   }

   public Date getData_cadastro_usu()
   {
       return data_cadastro_usu;
   }

   public void setStatus_usu(String status_usu)
   {
       this.status_usu = status_usu;
   }

   public String getStatus_usu()
   {
       return status_usu;
   }
}
