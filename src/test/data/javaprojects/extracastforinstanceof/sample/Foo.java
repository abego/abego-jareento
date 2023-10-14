package sample;

@SuppressWarnings("all")
public class Foo {

    public static String checkIt(Object a, Object b, Object c) {
        if (a instanceof String) {
            return "a is String";
        }
        if ((String) b instanceof String) {
            return "b is String";
        }
        if ((Object) c instanceof String) {
            return "c is String";
        }
        if ((a) instanceof String) {
            return "a is String";
        }
        if (((String) b) instanceof String) {
            return "b is String";
        }
        if (((Object) c) instanceof String) {
            return "c is String";
        }
        return "done.";
    }

}
