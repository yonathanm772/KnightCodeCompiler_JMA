package compiler;
public class Variable {
    private String name;
    private String type;
    private int memory_location;

    // Constructor
    public Variable(String name, String type, int memory_location) {
        this.name = name;
        this.type = type;
        this.memory_location = memory_location;
    }

    public String getName() {
        return type;
    }
    // Getter for type
    public String getType() {
        return type;
    }

    public int getMemLocation() {
        return memory_location;
    }
    
    // Override toString method to display value and type
    @Override
    public String toString() {
        return "Variable{" +
                "name=" + name +
                ", type=" + type +
                ", memory location=" + memory_location +
                '}';
    }
}