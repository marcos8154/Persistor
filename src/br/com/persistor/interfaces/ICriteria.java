package br.com.persistor.interfaces;

import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.generalClasses.Expressions;
import br.com.persistor.generalClasses.Limit;
import java.util.List;

public interface ICriteria<T>
{

    /**
     * Adiciona um expressão a Criteria. A expression deve ser obtida atravez
     * dos métodos estáticos da classe Restrictions. Exemplo:
     * criteria.add(Restrictions.eq(....));
     *
     * @param expression Instância de Expression obtida atraves dos métodos
     * estáticos da classe Restrictions
     * @return
     */
    ICriteria add(Expressions expression);

    /**
     * Adiciona um Join na critéria
     *
     * @param join_type Tipo de Join
     * @param entity Entidade do Join
     * @param joinCondition Condição do Join. Não utilizar a clausula "on".
     * Exemplo: criteria.add(JOIN_TYPE.LEFT, cliente, "cliente.id =
     * historico.cliente_id");
     * @return
     */
    ICriteria add(JOIN_TYPE join_type, Object entity, String joinCondition);

    /**
     * Adiciona um Limit na criteria.
     *
     * @param limit Instância de Limit obtida atravez dos metodos estáticos da
     * classe Limit
     * @return
     */
    ICriteria addLimit(Limit limit);

    /**
     * O conjunto de expressoes anteriormente incluidos serão transformados em
     * Query SQL e executado no banco
     *
     * @return
     */
    ICriteria execute();

    /**
     * Caso a Criteria utilize Joins, será necessário acionar este método para
     * recuperar uma entidade do resultado da operação
     *
     * @param entity Instância da entidade a ser populada
     * @return
     */
    T loadEntity(T entity);

    /**
     * Caso a Criteria utilize Joins, será necessário acionar este metodo para
     * recuperar uma lista de objetos do resultado da operação. Apos acionado
     * este método, a lista pode ser obtida atraves do método toList(), contido
     * na propria dentidade, herdado da classe Entity. Excemplo:
     * criteria.loadList(historico); historico.toList()
     *
     * @param entity Instância da entidade a ser populada
     */
    void loadList(Object entity);

    /**
     * Inicia o isolamento de um conjunto de condiçoes da query. Equivalente ao
     * parentesis. Exemplo: select * from historico where (data = ......
     *
     * @return
     */
    ICriteria beginPrecedence();

    /**
     * Termina o isolamento de um conjunto de condiçoes da query. Equivalente ao
     * parentesis. Exemplo: select * from historico where (id = 10 or
     * arquivo_morto = false)
     *
     * @return
     */
    ICriteria endPrecedence();

    /**
     * Adiciona um campo a ser ignorado durante a geração do SQL. Utilizar somente em casos de Join
     * @param fieldName Nome do campo a ser ignorado na montagem da query
     * @return 
     */
    ICriteria addJoinIgnoreField(String fieldName);

    /**
     * Utilizado para informar campos especificos durante a geração do SQL. Não utilizar em caso de Join
     * @param fields Array de String, contendo os campos especificos da query
     * @return 
     */
    ICriteria setSpecificFields(String... fields);

    /**
     * Habilita o encerramento automatico da Session após o termino da execução da query
     * @return 
     */
    ICriteria enableCloseSessionAfterExecute();
}
