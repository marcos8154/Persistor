/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.abstractClasses;

import br.com.persistor.generalClasses.ValidationDomain;
import br.com.persistor.generalClasses.ValidationRule;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.translation.messages_bg;

/**
 *
 * @author Marcos Vinícius
 */
public abstract class EntityValidator
{

    private final List<ValidationDomain> domains = new ArrayList<>();
    private boolean hasErrors = false;
    private String validationMessage = "";
    
    public boolean hasErrors()
    {
        return hasErrors;
    }
    
    public String getValidationMessage()
    {
        return validationMessage;
    }
    
    public void addValidationDomain(ValidationDomain domain)
    {
        domains.add(domain);
    }

    public void validate(Object entity)
    {
        try
        {
            for(ValidationDomain domain : domains)
            {
                for(ValidationRule rule : domain.getRules())
                {
                    if(entity.getClass() == domain.getValidationDomainClass())
                    {
                        Field field = entity.getClass().getDeclaredField(rule.getField());
                        field.setAccessible(true);
                        
                        switch(rule.getType())
                        {
                            case NOT_NULL:
                                if(field.get(entity) == null)
                                {
                                    hasErrors = true;
                                    validationMessage = rule.getMessage();
                                }
                                break;
                                
                            case NOT_EMPTY:
                                if(field.get(entity).toString().isEmpty())
                                {
                                    hasErrors = true;
                                    validationMessage = rule.getMessage();
                                }
                                break;
                                
                            case MAX:
                                if((double)field.get(entity) > rule.getValue())
                                {
                                    hasErrors = true;
                                    validationMessage = rule.getMessage();
                                }
                                break;
                                
                            case MIN:
                                if((double)field.get(entity) < rule.getValue())
                                {
                                    hasErrors = true;
                                    validationMessage = rule.getMessage();
                                }
                                break;
                                
                            case SIZE:
                                if(field.get(entity).toString().length() > rule.getValue())
                                {
                                    hasErrors = true;
                                    validationMessage = rule.getMessage();
                                }
                                break;
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
