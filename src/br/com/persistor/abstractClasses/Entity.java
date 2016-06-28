package br.com.persistor.abstractClasses;

import java.util.ArrayList;

public abstract class Entity
{
    public ArrayList<Object> ResultList = new ArrayList<>();
    public boolean saved = false;
    public boolean updated = false;
    public boolean deleted = false;
    public String mountedQuery;
}
