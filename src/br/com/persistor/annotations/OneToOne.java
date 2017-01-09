package br.com.persistor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.persistor.enums.JOIN_TYPE;
import br.com.persistor.enums.LOAD;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne
{

    String source();

    String target();

    JOIN_TYPE join_type();

    LOAD load();
    
    String[] ignore_onID() default {};
}
