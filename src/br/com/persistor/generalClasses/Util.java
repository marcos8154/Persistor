package br.com.persistor.generalClasses;

import java.lang.reflect.Field;

public class Util
{

    public static boolean extendsEntity(Class cls)
    {
        try
        {
            for (Field field : cls.getFields())
            {
                if (field.getName() == "saved")
                {
                    return true;
                }
            }

        }
        catch (Exception ex)
        {

        }
        return false;
    }
}
