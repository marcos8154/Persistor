/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import br.com.persistor.connectionManager.DataSource;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.interfaces.DbMigration;
import br.com.persistor.interfaces.IPersistenceLogger;
import br.com.persistor.interfaces.Session;
import br.com.persistor.sessionManager.SessionFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Marcos Vinícius
 */
public class MigrationsController
{

    private List<DbMigration> migrations = null;
    private Session session = null;
    private SessionFactory sessionFactory = null;
    private DBConfig config = null;
    private IPersistenceLogger logger = null;

    public MigrationsController(DBConfig config)
    {
        migrations = new ArrayList<>();
        this.config = config;
        try
        {
            logger = (IPersistenceLogger) Class.forName(config.getPersistenceLogger()).newInstance();
        }
        catch (Exception ex)
        {

        }
    }

    public void addMigration(Class migrationClass) throws Exception
    {
        DbMigration migration = (DbMigration) migrationClass.newInstance();

        for (DbMigration m : migrations)
            if (m.getVesion() == migration.getVesion())
                System.err.println("Persistor: Duplicate DbMigration for version " + migration.getVesion() + ". \nVerify added migrations and try again.");

        migrations.add(migration);
    }

    private boolean historyTableExists()
    {
        PreparedStatement ps = null;
        try
        {
            ps = session.getActiveConnection().prepareStatement("select * from MigrationHistory");
            ps.executeQuery();
            ps.close();
            return true;
        }
        catch (Exception ex)
        {
            try
            {
                if (ps != null)
                    ps.close();
            }
            catch (Exception e)
            {
            }
            return false;
        }
    }

    private void createHistoryTable()
    {
        PreparedStatement ps = null;
        try
        {
            CodeFirstTableDomain table = new CodeFirstTableDomain(MigrationHistory.class);
            table.setColumnProperty("migrationDate", false, 0, 0, null);
            table.setColumnProperty("currentVersion", false, 0, 0, null);

            CodeFirstDatabase db = new CodeFirstDatabase(false);
            db.setSession(session);
            db.addTableDomain(table);
            db.setSqlToRun("insert into MigrationHistory (migrationDate, currentVersion) values ('2017-06-30', 0);");
            db.createTables(config);
        }
        catch (Exception ex)
        {
            logger.newNofication(new PersistenceLog("MigrationsController", "historyTableExists", null, ex, ""));
        }
    }

    private int getCurrentVersion() throws Exception
    {
        MigrationHistory mh = new MigrationHistory();
        session.createCriteria(mh, RESULT_TYPE.UNIQUE)
                .execute();
        return mh.getCurrentVersion();
    }

    private void upgrade(int version, int dbVersion) throws Exception
    {
        for (int i = 0; i < migrations.size(); i++)
        {
            DbMigration migration = migrations.get(i);
            migration.setDbConfig(config);

            int migVersion = migration.getVesion();

            if (migVersion <= dbVersion)
                continue;

            if (migVersion > version)
                break;

            migration.up();
            String[] queryes = migration.migrationScript.split("\n");
            for (int q = 0; q < queryes.length; q++)
                if (!queryes[q].isEmpty())
                    executeMigrationSql(queryes[q]);
        }
    }

    private void downGrade(int version, int dbVersion) throws Exception
    {
        for (int i = (migrations.size() - 1); i >= 0; i--)
        {
            DbMigration migration = migrations.get(i);
            migration.setDbConfig(config);

            int migVersion = migration.getVesion();

            if (migVersion > version && migVersion > dbVersion)
                continue;
            if ((migVersion < version) || (migVersion == version))
                break;

            migration.down();
            String[] queryes = migration.migrationScript.split("\n");
            for (int q = 0; q < queryes.length; q++)
                if (!queryes[q].isEmpty())
                    executeMigrationSql(queryes[q]);
        }
    }

    public void migrateForVersion(int version) throws Exception
    {
        sessionFactory = new SessionFactory();
        session = sessionFactory.getSession(config);
        try
        {
            if (!historyTableExists())
                createHistoryTable();
            int currentVersion = getCurrentVersion();

            executeMigrationSql("set autocommit=0");
            if (version > currentVersion)
                upgrade(version, currentVersion);
            else if (version < currentVersion)
                downGrade(version, currentVersion);

            MigrationHistory m = new MigrationHistory();
            session.createQuery(m, "update MigrationHistory set currentVersion = ?, migrationDate = ?")
                    .setParameter(1, version)
                    .setParameter(2, new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()))
                    .execute();

            session.commit();
            session.close();
        }
        catch (Exception ex)
        {
            if (session != null)
                session.rollback();
        }
    }

    private void executeMigrationSql(String sql) throws Exception
    {
        Statement st = null;
        try
        {
            st = session.getActiveConnection().createStatement();
            st.execute(sql);
            st.close();
        }
        catch (Exception ex)
        {
            if (st != null)
                st.close();

            logger.newNofication(new PersistenceLog("MigrationsController", "historyTableExists", null, ex, ""));
            throw new Exception(ex.getMessage());
        }
    }
}
