package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import enums.JOIN_TYPE;
import enums.LOAD;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne 
{
	String source();
	String target();	
	JOIN_TYPE join_type();
	LOAD load();
}
