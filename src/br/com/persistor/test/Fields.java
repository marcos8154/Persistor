/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.test;

import br.com.persistor.abstractClasses.Entity;
import br.com.persistor.annotations.Column;

/**
 *
 * @author Frederico
 */
public class Fields extends Entity{
    
    private String Field;
    private String Type;
    private String Null;
    private String Key;
    private String Default;
    private String Extra;

    @Column(name="Field")
    public String getField() {
        return Field;
    }

    public void setField(String Field) {
        this.Field = Field;
    }

    @Column(name="Type")
    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    @Column(name="Null")
    public String getNull() {
        return Null;
    }

    public void setNull(String Null) {
        this.Null = Null;
    }

    @Column(name="Key")
    public String getKey() {
        return Key;
    }

    public void setKey(String Key) {
        this.Key = Key;
    }

    @Column(name="Default")
    public String getDefault() {
        return Default;
    }

    public void setDefault(String Default) {
        this.Default = Default;
    }

    @Column(name="Extra")
    public String getExtra() {
        return Extra;
    }

    public void setExtra(String Extra) {
        this.Extra = Extra;
    }
    
}
