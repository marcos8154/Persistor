/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author marcosvinicius
 */

@Repeatable(NamedQueryes.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface NamedQuery
{
    String queryName();
    String queryValue();
}
