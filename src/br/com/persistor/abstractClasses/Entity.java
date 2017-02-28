package br.com.persistor.abstractClasses;

import br.com.persistor.generalClasses.PersistenceLog;
import br.com.persistor.generalClasses.Util;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class Entity
{

    public List<Object> ResultList = new ArrayList<>();
    public boolean saved = false;
    public boolean updated = false;
    public boolean deleted = false;
    public String mountedQuery;

    public <T> List<T> toList()
    {
        try
        {
            List<Object> list = ResultList;
            List<T> resultList = new ArrayList<>();
            for (Object obj : list)
            {
                resultList.add((T) obj);
            }
            return resultList;
        }
        catch (Exception ex)
        {
        }
        return new ArrayList<>();
    }
}
