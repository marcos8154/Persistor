package br.com.persistor.test;

import br.com.persistor.enums.COMMIT_MODE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.sessionManager.Criteria;
import br.com.persistor.sessionManager.Query;
import br.com.persistor.sessionManager.SessionFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class main
{

    public static void main(String[] args)
    {
        try
        {
            SessionFactory session = new ConfigureSession().getMySQLSession();

            Veiculo v = new Veiculo();

            InputStream is = new FileInputStream("C:\\Users\\Marcos Vinícius\\Pictures\\bolsonaro.jpg");

            v.setFoto(is);

            session.save(v);
            session.commit();
            
            session.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
