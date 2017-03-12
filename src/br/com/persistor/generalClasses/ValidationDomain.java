/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import br.com.persistor.enums.ValidationRuleType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcos Vinícius
 */
public class ValidationDomain
{

    private Class clazz;
    private List<ValidationRule> rules;

    public ValidationDomain(Class clazz)
    {
        this.clazz = clazz;
        rules = new ArrayList<>();
    }

    public List<ValidationRule> getRules()
    {
        return rules;
    }

    public Class getValidationDomainClass()
    {
        return clazz;
    }
    
    public ValidationDomain notNull(String field, String message)
    {
        rules.add(new ValidationRule(ValidationRuleType.NOT_NULL, field, message));
        return this;
    }

    public ValidationDomain notEmpty(String field, String message)
    {
        rules.add(new ValidationRule(ValidationRuleType.NOT_NULL, field, message));
        return this;
    }

    public ValidationDomain min(String field, String message, double value)
    {
        rules.add(new ValidationRule(ValidationRuleType.MIN, field, message, value));
        return this;
    }

    public ValidationDomain max(String field, double value, String message)
    {
        rules.add(new ValidationRule(ValidationRuleType.MAX, field, message, value));
        return this;
    }
    
    public ValidationDomain size(String field, double value, String message)
    {
        rules.add(new ValidationRule(ValidationRuleType.SIZE, field, message, value));
        return this;
    }
}
