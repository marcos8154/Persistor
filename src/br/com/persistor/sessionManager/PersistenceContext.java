/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.sessionManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;

/**
 *
 * @author Marcos Vinícius
 */
public class PersistenceContext
{

    public boolean initialized;

    private Object context = null;
    private List<CachedQuery> cachedQuerys = new ArrayList<>();
    private CacheManager cacheManager = null;

    public CachedQuery findCachedQuery(String query)
    {
        if (cachedQuerys == null)
            cachedQuerys = new ArrayList<>();

        for (CachedQuery cq : cachedQuerys)
            if (cq.getQuery().equals(query))
                return cq;

        return null;
    }

    public void addCachedQuery(String query, int[] resultKeys)
    {
        if (cachedQuerys == null)
            cachedQuerys = new ArrayList<>();

        CachedQuery cq = findCachedQuery(query);
        if (cq != null)
        {
            cq.setResultKeys(resultKeys);
            return;
        }

        CachedQuery cachedQuery = new CachedQuery(query, resultKeys);
        cachedQuerys.add(cachedQuery);
    }

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
                    cacheManager = CacheManager.newInstance();
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
        if (!initialized)
            return;

        if (cacheManager != null)
            cacheManager.getCache("cache1").removeAll();
    }

    public Object findByID(Object entity, Object id)
    {
        try
        {
            if (!initialized)
                return null;
            
            Cache cache = cacheManager.getCache("cache1");
            Element element = cache.get(entity.getClass().getName() + "-" + id);
            if (element != null)
                System.err.println("Persistor: entity '" + entity.getClass().getName() + "' successfully retrieved from context!");
            return element.getObjectValue();

        }
        catch (Exception ex)
        {

        }
        return null;
    }

    public List<Object> listByClassType(Class entityClass)
    {
        if (!initialized)
            return new ArrayList<>();

        List<Object> result = new ArrayList<>();
        try
        {
            Cache cache = cacheManager.getCache("cache1");

            Attribute<String> keys = cache.getSearchAttribute("entityClass");
            Results results = cache.createQuery()
                    .includeKeys()
                    .includeValues()
                    .addCriteria(keys.ilike(entityClass.getName())).execute();

            for (Result r : results.all())
                result.add(r.getValue());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return result;
    }

    public void removeAllFromClass(Class entityClass)
    {
        try
        {
            int removed = 0;
            Cache cache = cacheManager.getCache("cache1");
            for (Object key : cache.getKeys())
            {
                Element element = cache.get(key);
                Object entity = element.getObjectValue();
                if (entity.getClass() == entityClass)
                {
                    cache.remove(key);
                    removed++;
                }
            }

            System.err.println("Persistor: " + removed + " entity's removed from context!");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void shutDownCacheManager()
    {
        if (!initialized)
            return;

        if (cacheManager != null)
            cacheManager.shutdown();
    }

    public void removeFromContext(Object entity)
    {
        try
        {
            if (!initialized)
                return;

            SQLHelper helper = new SQLHelper();
            helper.prepareDelete(entity);
            String key = entity.getClass().getName() + "-" + helper.getPrimaryKeyValue();

            Cache cache = cacheManager.getCache("cache1");
            cache.remove(key);

            cachedQuerys.clear();
            System.err.println("Persistor: Entity '" + entity.getClass().getName() + "' successfully removed to context!");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void addToContext(Object entity) throws Exception
    {
        if (!initialized)
            return;
        if (!isEntitySet(entity))
            return;

        SQLHelper helper = new SQLHelper();
        helper.prepareDelete(entity);
        int id = Integer.parseInt(helper.getPrimaryKeyValue());
        String key = entity.getClass().getName() + "-" + id;

        Cache cache = cacheManager.getCache("cache1");
        if (cache.isKeyInCache(key))
            return;

        cache.put(new Element(key, entity));
        System.err.println("Persistor: Entity '" + entity.getClass().getName() + "' successfully added to context!");
    }

    public boolean isEntitySet(Object entity)
    {
        try
        {
            for (Field field : context.getClass().getDeclaredFields())
            {
                String base = field.getGenericType().getTypeName();
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
                                return true;
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            return false;
        }
        return false;
    }

}
