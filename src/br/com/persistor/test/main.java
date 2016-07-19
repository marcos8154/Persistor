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
        
    }
}
