package generics;

public class GenericsMain {
    public static class Sub<T> {
        String calc(T object) {
            return object.toString();
        }
    }
    
    public String func(Sub<String> sub, String s) {
        return sub.calc(s);
    }
    
    Sub<Integer> field = new Sub<Integer>();
}
