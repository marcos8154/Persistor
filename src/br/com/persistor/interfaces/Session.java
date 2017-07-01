package br.com.persistor.interfaces;

import br.com.persistor.enums.ISOLATION_LEVEL;
import br.com.persistor.enums.RESULT_TYPE;
import br.com.persistor.generalClasses.DBConfig;
import br.com.persistor.generalClasses.EntityKey;
import br.com.persistor.sessionManager.Criteria;
import br.com.persistor.sessionManager.PersistenceContext;
import br.com.persistor.sessionManager.Query;
import java.sql.Connection;

public interface Session
{

    /**
     * Caso a entidade esteja utilizando o recurso @Version, após uma falha de
     * update, pode ser acionado para saber se houve o versionamento foi violado
     *
     * @return version violation
     */
    boolean isVersionViolation();

    /**
     * @return Instância atual do cache de primeiro nivel
     */
    PersistenceContext getPersistenceContext();

    /**
     * @return Instância atual do cache de segundo nivel
     */
    PersistenceContext getSLPersistenceContext();

    /**
     * @return Instância atual do PersistenceLogger
     */
    IPersistenceLogger getPersistenceLogger();

    boolean isEnabledSLContext();

    /**
     * Desabilita o cache de segundo nivel para Session em questão
     */
    void disableSLContext();

    /**
     * Salva uma entidade no banco de dados
     *
     * @param entity Instância da entidade a ser persistida
     */
    void save(Object entity);

    /**
     * Atualiza uma entidade no banco de dados
     *
     * @param entity Instância da entidade a ser atualizada
     */
    void update(Object entity);

    /**
     * Atualiza uma entidade no banco de dados passando uma condição SQL. NÃO É
     * NECESSÁRIO A CLAUSULA * WHERE
     *
     *
     * @param entity Entidade a ser atualizada
     * @param and_or_condition Condição SQL adicional para o UPDATE. Não
     * utilizar a clausula WHERE
     */
    void update(Object entity, String and_or_condition);

    /**
     * Altera o ISOLATION_LEVEL da transação atual na session. Não é
     * recomendável acionar este método após executar alguma operação com a
     * Session
     *
     * @param isolation_level Novo ISOLATION_LEVEL da transação
     */
    void setIsolationLevel(ISOLATION_LEVEL isolation_level);

    /**
     * Efetua a limpeza do cache de primeiro nivel (caso habilitado)
     *
     * @param includeSLCache Caso true, o cache de segundo nivel será limpo
     */
    void evict(boolean includeSLCache);

    /**
     * Remove a entidade do banco
     *
     * @param entity Instância da entidade a ser removida
     */
    void delete(Object entity);

    /**
     * Remove a entidade do banco
     *
     * @param entity Instância da entidade a ser removida
     * @param and_or_condition Condição SQL auxiliar para o comando. Apenas
     * utilize WHERE, se a entidade NÃO possuir PK
     */
    void delete(Object entity, String and_or_condition);

    /**
     * Efetua o commit
     */
    void commit();

    /**
     * Efetua o rollback
     */
    void rollback();

    /**
     * Busca uma entidade no banco por sua chave primaria. Caso habilitado o
     * cache, a busca será feita no cache.
     *
     * Exemplo: Cliente c = session.onID(Cliente.class, 7);
     *
     * @param <T> Tipo retornado pelo metodo (não é necessário cast)
     * @param entityCls Classe da entidade a ser buscada
     * @param id Valor da chave primaria a ser buscada
     * @return Instância da entidade em questão. (Caso não exista, será
     * retornado uma instância com atributos vazios)
     */
    <T> T onID(Class entityCls, int id);

    <T> T onID(Class entityCls, EntityKey... keys);
    
    /**
     * Retorna o último registro da entidade no banco de dados. Exemplo: Cliente
     * c = session.last(Cliente.class);
     *
     * @param <T> Tipo retornado
     * @param entityClass Classe de entidade em questão
     * @param whereCondition Condição WHERE adicional (Parametro opcional, pode
     * ser passado somente o Class, se não for necessário uma condição WHERE)
     * @return
     */
    <T> T last(Class entityClass, String... whereCondition);

    /**
     * Retorna o primeiro registro da entidade no banco de dados. Exemplo:
     * Cliente c = session.first(Cliente.class);
     *
     * @param <T> Tipo retornado
     * @param entityClass Classe de entidade em questão
     * @param whereCondition Condição WHERE adicional (Parametro opcional, pode
     * ser passado somente o Class, se não for necessário uma condição WHERE)
     * @return
     */
    <T> T first(Class entityClass, String... whereCondition);

    /**
     * Fecha a Session e limpa o cache de primeiro nivel (se habilitado)
     */
    void close();

    /**
     * Cria uma instância da interface Criteria
     *
     * @param entity Instância da entidade para fazer a pesquisa
     * @param result_type Tipo de resultado vindo da pesquisa (MULTIPLE ou
     * UNIQUE)
     * @return Instância de Criteria
     */
    Criteria createCriteria(Object entity, RESULT_TYPE result_type);

    /**
     * Cria uma instância da interface Query
     *
     * @param entity Instância da entidade para fazer a consulta
     * @param queryCommand Comando SQL ou referência de NamedQuery
     * @return Instância de Query
     */
    Query createQuery(Object entity, String queryCommand);

    /**
     * Retorna a conexão com o banco em uso pela Session
     *
     * @return Conexão com o banco em uso pela Session
     */
    Connection getActiveConnection();

    /**
     * Retorna a configuração atualmente em uso pela Session
     *
     * @return DBConfig em uso pela Session
     */
    DBConfig getConfig();

    /**
     * Realiza uma operação count no banco e retorna o resultado
     *
     * @param entityClass Classe da entidade em questão
     * @param whereCondition Condição WHERE adicional para o comando (Opcional)
     * @return Resultado da operação count realizada no banco
     */
    int count(Class entityClass, String... whereCondition);

    /**
     * Realiza uma operação sum no banco e retorna o resultado
     *
     * @param entityClass Classe da entidade em questão
     * @param whereCondition Condição WHERE adicional para o comando (Opcional)
     * @param columnName Nome da coluna onde será feito a operação
     * @return Resultado da operação sum realizada no banco
     */
    double sum(Class entityClass, String columnName, String... whereCondition);
}
