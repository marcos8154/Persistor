package br.com.persistor.test;

import br.com.persistor.enums.COMMIT_MODE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.sessionManager.Query;
import br.com.persistor.sessionManager.SessionFactory;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class main
{
    private FileOutputStream outImage;
    private FileInputStream intImage;
    
    Object obj;
    
    public void setNameImage(FileInputStream image)
    {
        this.intImage = image;
    }
    
    public FileOutputStream getImage()
    {
        return outImage;
    }
    
    public static void main(String[] args)
    {
        SessionFactory sessionFactory = new ConfigureSession().getMySQLSession();
        
        Pessoa pessoa = new Pessoa();
        
        Query query = sessionFactory.createQuery(pessoa, "select*from pessoa");
        query.setResult_type(RESULT_TYPE.MULTIPLE);
        
        query.execute();
        
        sessionFactory.close();
    }
}
