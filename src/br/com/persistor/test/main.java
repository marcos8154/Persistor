package br.com.persistor.test;

import br.com.persistor.enums.DB_TYPE;
import br.com.persistor.enums.FILTER_TYPE;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.Restrictions;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class main
{

    public static void main(String[] args)
    {
        
        Session session = getSession();

        Produtos produtos = session.onID(Produtos.class, 1);
        session.createCriteria(produtos , RESULT_TYPE.MULTIPLE)
                .add(Restrictions.eq(FILTER_TYPE.WHERE, "id", "1; select * from produtos where id =2"))
                .execute();
        /*
        CacheManager cm = CacheManager.newInstance();

        //2. Get a cache called "cache1", declared in ehcache.xml
        Cache cache = cm.getCache("cache1");

        //3. Put few elements in cache
        cache.put(new Element("1", "Jan"));
        cache.put(new Element("2", "Feb"));
        cache.put(new Element("3", "Mar"));

        //4. Get element from cache
        Element ele = cache.get("2");

        //5. Print out the element
        String output = (ele == null ? null : ele.getObjectValue().toString());
        System.out.println(output);

        //6. Is key in cache?
        System.out.println(cache.isKeyInCache("3"));
        System.out.println(cache.isKeyInCache("10"));

        //7. shut down the cache manager
        cm.shutdown(); */
    }

    private static SessionFactory sf = null;

    private static Session getSession()
    {
        try
        {
            if (sf == null)
            {
                sf = new SessionFactory();
                return sf.getSession(getConfig());
            }
            return sf.getSession();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    static String toCamelCase(String string)
    {
        StringBuffer sb = new StringBuffer(string);
        sb.replace(0, 1, string.substring(0, 1).toUpperCase());
        return sb.toString();

    }

    private static DBConfig getConfig()
    {
        DBConfig config = new DBConfig();
        config.setPersistenceLogger(LogTest.class);
        //  config.setPersistenceContext(Context.class);
       // config.setSlPersistenceContext(Context.class);
        config.setDb_type(DB_TYPE.MySQL);
        config.setHost("localhost");
        config.setPort(3306);
        config.setUser("root");
        config.setPassword("81547686");
        config.setDatabase("teste_cache");

        return config;
    }
}
