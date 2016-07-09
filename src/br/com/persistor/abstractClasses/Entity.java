package br.com.persistor.abstractClasses;

import java.util.ArrayList;
import java.util.List;

public abstract class Entity
{
    public List ResultList = new ArrayList<>();
    public boolean saved = false;
    public boolean updated = false;
    public boolean deleted = false;
    public String mountedQuery;

}
