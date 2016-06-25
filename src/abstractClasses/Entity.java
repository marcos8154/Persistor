package abstractClasses;

import java.util.ArrayList;

public abstract class Entity	
{
	public ArrayList<Object> ResultList;
	public boolean saved = false;
	public boolean updated = false;
	public boolean deleted = false;
	public String mountedQuery;
	public int version = 1;
}
