package generalClasses;

import java.lang.reflect.Field;

public class Util 
{
	public static boolean extendsEntity(Class cls)
	{
		for(Field field : cls.getFields())
		{
			if(field.getName() == "saved") return true;
		}
		
		return false;
	}
}
