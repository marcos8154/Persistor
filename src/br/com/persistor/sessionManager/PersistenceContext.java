/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

import br.com.persistor.generalClasses.EntitySet;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcos Vinícius
 */
public class PersistenceContext
{
    private Object context = null;
    private List<EntitySet> entitySets = new ArrayList<>();
    
    public void Initialize(String className)
    {
        try
        {
            if(className == null || className == "") return;
            
            Class contextClass = Class.forName(className);
            Constructor ctor = contextClass.getConstructor();
            boolean initialized = false;
            for (Class clazz : contextClass.getInterfaces())
            {
                if (clazz == br.com.persistor.interfaces.IPersistenceContext.class)
                {
                    context = ctor.newInstance();
                    System.err.println("Persistor: Persistence Context initialized successfully! The Context Class is: " + className);
                    return;
                }
            }

            if (!initialized)
                throw new Exception("Persistor: PersistenceContext initialization error: the class " + className + " not implements IPersistenceContext.");
        }
        catch (Exception ex)
        {
            System.err.println("Persistor: PersistenceContext initialization error: " + ex.getMessage());
        }
    }

    public Object getFromContext(Object entity)
    {
        try
        {
            for (EntitySet es : entitySets)
            {
                Object obj = es.getEntity();
                Field field = obj.getClass().getField("mountedQuery");
                String mountedQueryEs = field.get(obj).toString();
                String mounStringEntity = entity.getClass().getField("mountedQuery").get(entity).toString();
                
                if(mountedQueryEs.equals(mounStringEntity)) return es.getEntity();
            }
        }
        catch (Exception ex)
        {

        }
        return null;
    }

    public void addToContext(Object entity)
    {
        try
        { 
            if(getFromContext(entity) != null) return;
            for (Field f : context.getClass().getDeclaredFields())
            {
                String base = f.getGenericType().getTypeName();
                base = base.substring(base.indexOf("<"), base.indexOf(">"));
                base = base.replace("<", "");

                Class cls = Class.forName(base);
                if (cls == entity.getClass())
                {
                    for (Method m : context.getClass().getMethods())
                    {
                        if (m.getName().startsWith("set"))
                        {
                            if (m.getName().contains(cls.getSimpleName()))
                            {
                                EntitySet set = new EntitySet<>(entity);
                                m.invoke(context, set);
                                entitySets.add(set);
                                break;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
             System.out.println(ex.getMessage());
        }
    }
}
