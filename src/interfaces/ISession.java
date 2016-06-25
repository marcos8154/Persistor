package interfaces;

import enums.ResultType;
import sessionManager.Criteria;

public interface ISession 
{
	void save(Object obj);
	void update(Object obj);
	void update(Object obj, String andCondition);
	void delete(Object obj);
	void delete(Object obj, String andCondition);
	void commit();
	void rollback();
	void onID(Object obj, int id);
	void close();
	Criteria createCriteria(Object obj, ResultType result_type);
}
