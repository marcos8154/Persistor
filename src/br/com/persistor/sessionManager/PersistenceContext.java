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
    public boolean initialized;

    public void Initialize(String className)
    {
        try
        {
            if (className == null || className == "")
                return;

            Class contextClass = Class.forName(className);
            Constructor ctor = contextClass.getConstructor();
            for (Class clazz : contextClass.getInterfaces())
            {
                if (clazz == br.com.persistor.interfaces.IPersistenceContext.class)
                {
                    context = ctor.newInstance();
                    initialized = true;
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
    
    public void clear()
    {
        System.out.println("Persistor: cleaning up Persistence Context...");
        entitySets.clear();
        entitySets = null;
        context = null;
    }

    public Object getFromContext(Object entity)
    {
        try
        {
            for (EntitySet es : entitySets)
            {
                Object esEntity = es.getEntity();

                String qEntity = entity.getClass().getField("mountedQuery").get(entity).toString();
                String qEsEntity = esEntity.getClass().getField("mountedQuery").get(esEntity).toString();

                if (esEntity.equals(entity) || qEntity.equals(qEsEntity))
                    return esEntity;
            }
        }
        catch (Exception ex)
        {

        }
        return null;
    }

    public void removeFromContext(Object entity)
    {
        try
        {
            for (EntitySet es : entitySets)
            {
                Object obj = es.getEntity();
                if (obj == entity)
                {
                    entitySets.remove(es);
                    break;
                }
            }
        }
        catch (Exception ex)
        {

        }
    }

    public void mergeEntity(Object entity)
    {
        try
        {
            for (EntitySet es : entitySets)
            {
                Object obj = es.getEntity();
                if (obj == entity)
                {
                    EntitySet newEs = new EntitySet(entity);
                    es = newEs;
                    break;
                }
            }
        }
        catch (Exception ex)
        {

        }
    }

    public void addToContext(Object entity)
    {
        try
        {
            if (getFromContext(entity) != null)
                return;
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
        }
    }
}
