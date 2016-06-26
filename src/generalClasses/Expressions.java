package generalClasses;

public class Expressions {

    private String currentValue;

    public String getCurrentValue()
    {
        return currentValue;
    }

    public void setCurrentValue(String currentValue)
    {
        this.currentValue = currentValue;
    }

    public Expressions(String value)
    {
        this.setCurrentValue(value);
    }
}
