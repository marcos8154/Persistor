/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Marcos Vinícius
 */
@Retention( RetentionPolicy.RUNTIME )
public @interface NamedQueryes
{
    NamedQuery[] value();
}
