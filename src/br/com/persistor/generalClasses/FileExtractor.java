/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 *
 * @author Marcos Vinícius
 */
public class FileExtractor
{

    private InputStream inputStream;
    private int bufferSize;
    private String fileToExtract;

    public void setInputStream(InputStream inputStream)
    {
        this.inputStream = inputStream;
    }

    public void setBufferSize(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    public void setFileToExtract(String fileToExtract)
    {
        this.fileToExtract = fileToExtract;
    }

    public void extract()
    {
        FileOutputStream outputStream = null;
        try
        {
           File file = new File(fileToExtract);
           outputStream = new FileOutputStream(file);
           
           byte[] buffer = new byte[bufferSize];
           
           while(inputStream.read(buffer) > 0)
           {
               outputStream.write(buffer);
           }
           
        } catch (Exception ex)
        {
            
        } finally
        {
             closeOutputStream(outputStream);
        }
    }
    
    private void closeOutputStream(FileOutputStream outputStream)
    {
        try
        {
            outputStream.close();
        }catch(Exception ex)
        {
          //  ex.printStackTrace();
        }
    }
}
