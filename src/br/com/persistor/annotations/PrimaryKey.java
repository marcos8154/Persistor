package br.com.persistor.annotations;

import br.com.persistor.enums.INCREMENT;
import br.com.persistor.enums.PRIMARYKEY_TYPE;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey
{
     INCREMENT increment();
     PRIMARYKEY_TYPE primarykey_type() default  PRIMARYKEY_TYPE.HEAD;
}
