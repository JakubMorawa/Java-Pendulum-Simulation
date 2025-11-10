package pendulum;

public class DataElement {
    private final String variableName;
    private double value;
    private final boolean changeable;

    public DataElement(boolean changeable, double value, String variableName) {
        this.changeable = changeable;
        this.value = value;
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    public double getValue() {
        return value;
    }

    public boolean isChangeable() {
        return changeable;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
}
