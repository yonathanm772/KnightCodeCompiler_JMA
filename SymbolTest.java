
public class SymbolTest {
	public static void main(String[] args) {
    	Variable <Integer> intVariable = new Variable<>(42);
        Variable <String> stringVariable = new Variable<>("Hello, world!");
        SymbolTable<String, Variable> st = new SymbolTable<>();
        
        st.put("apple", intVariable);
        System.out.println(st.get("apple"));
    }
}