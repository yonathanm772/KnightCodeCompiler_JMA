public class Variable<T> {
    private T value;
    private Class<?> type;

    // Constructor
    public Variable(T value) {
        this.value = value;
        this.type = value.getClass();
    }

    // Getter for value
    public T getValue() {
        return value;
    }

    // Getter for type
    public Class<?> getType() {
        return type;
    }

    // Setter for value
    public void setValue(T value) {
        this.value = value;
        this.type = value.getClass();
    }

    // Override toString method to display value and type
    @Override
    public String toString() {
        return "Variable{" +
                "value=" + value +
                ", type=" + type.getSimpleName() +
                '}';
    }
}