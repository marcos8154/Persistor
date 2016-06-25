package interfaces;

import java.util.List;

import enums.JOIN_TYPE;
import sessionManager.SessionFactory;

public interface IJoin 
{
	 void addJoin(JOIN_TYPE join_type, Object obj, String condition);
	 
	// void addJoin(JOIN_TYPE join_type, Object obj);
	 
	 void addFinalCondition(String final_and_or_where_condition);
	 
	 String decodeJoin(JOIN_TYPE join_type);
	 
	 List<Object> getResultList(Object obj);
	 
	 void Execute(SessionFactory session);

}
