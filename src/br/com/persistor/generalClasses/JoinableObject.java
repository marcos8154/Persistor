/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import br.com.persistor.enums.RESULT_TYPE;

/**
 *
 * @author Marcos Vinícius
 */
public class JoinableObject
{
    public RESULT_TYPE result_type;
    public Object objectToJoin;
    public String joinGetMethod;
}
