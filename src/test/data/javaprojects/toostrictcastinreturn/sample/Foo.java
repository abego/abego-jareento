package sample;

@SuppressWarnings("all")
public class Foo {

    interface Bar {
    }

    interface Baz extends Bar {
    }

    public static Object p1(Object a) {
        return a;
    }

    public static Object p2(Object a) {
        return (Object) a;
    }

    public static Object p3(Object a) {
        return (String) a;
    }

    public static Bar p4(Object a) {
        return (Bar) a;
    }

    public static Bar p5(Object a) {
        return (Baz) a;
    }

    public static Baz p6(Object a) {
        return (Baz) a;
    }

    public static <T> T p6(Object a) {
        return (T) a;
    }

    public static Integer p6(long a) {
        return (int) a;
    }

    public static Bar enclosedCast(Object a) {
        return ((Baz) a);
    }

}
