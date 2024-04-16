package compiler;

public class SymbolTest {
	public static void main(String[] args) {
    	Variable intVariable = new Variable("var", "INt", 0);
        SymbolTable<String, Variable> st = new SymbolTable<>();
        
        st.put("apple", intVariable);
        System.out.println(st.get("apple"));
    }
}