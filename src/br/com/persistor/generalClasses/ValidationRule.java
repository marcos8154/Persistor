/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import br.com.persistor.enums.ValidationRuleType;

/**
 *
 * @author Marcos Vinícius
 */
public class ValidationRule
{

    private ValidationRuleType type;
    private String field;
    private double value;
    private String message;

    public ValidationRule(ValidationRuleType type, String field, String message)
    {
        this.field = field;
        this.type = type;
        this.message = message;
    }

    public ValidationRule(ValidationRuleType type, String field, String message, double value)
    {
        this.field = field;
        this.type = type;
        this.message = message;
        this.value = value;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public double getValue()
    {
        return value;
    }

    public void setValue(double value)
    {
        this.value = value;
    }

    public ValidationRuleType getType()
    {
        return type;
    }

    public void setType(ValidationRuleType type)
    {
        this.type = type;
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

}
