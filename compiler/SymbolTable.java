package compiler;
import java.util.HashMap;

public class SymbolTable<Key, Variable> {
    private HashMap<Key, Variable> table;

    // Initialize an empty symbol table
    public SymbolTable() {
        table = new HashMap<>();
    }

    // Put a key-value pair into the symbol table
    public void put(Key key, Variable variable) {
        table.put(key, variable);
    }

    // Get the value associated with a key
    public Variable get(Key key) {
        return table.get(key);
    }

    // Check if the symbol table contains a key
    public boolean contains(Key key) {
        return table.containsKey(key);
    }

    // Delete a key-value pair from the symbol table
    public void delete(Key key) {
        table.remove(key);
    }

    // Get the number of key-value pairs in the symbol table
    public int size() {
        return table.size();
    }

    // Check if the symbol table is empty
    public boolean isEmpty() {
        return table.isEmpty();
    }

}