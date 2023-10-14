package sample;

@SuppressWarnings("all")
public class Foo {

    public static String checkInIf(Object a, Object b, Object c, Object d) {
        if (a == null) {
            return "a is null";
        }
        if (b != null) {
            return "b is not null";
        }
        if (null == c) {
            return "c is null";
        }
        if (null != d) {
            return "d is not null";
        }
        if (a != d) {
            return "a is not d";
        }
        return "done.";
    }

    public static String checkInTenary(Object a, Object b, Object c, Object d) {
        return a == null
                ? "a is null"
                : b != null
                ? "b is not null"
                : null == c
                ? "c is null"
                : null != d
                ? "d is not null"
                : a != d
                ? "a is not d"
                : "done.";
    }

    public static String castAndCheckInIf(Object a, Object b, Object c, Object d) {
        if ((Foo) a == null) {
            return "a is null";
        }
        if ((String) b != null) {
            return "b is not null";
        }
        if (null == (Integer) c) {
            return "c is null";
        }
        if (null != (Object) d) {
            return "d is not null";
        }
        if ((Foo) a != d) {
            return "a is not d";
        }
        return "done.";
    }

    public static String castAndCheckInTenary(Object a, Object b, Object c, Object d) {
        return (Foo) a == null
                ? "a is null"
                : (String) b != null
                ? "b is not null"
                : null == (Integer) c
                ? "c is null"
                : null != (Object) d
                ? "d is not null"
                : (Foo) a != d
                ? "a is not d"
                : "done.";
    }

    public static String enclosedCastAndCheckInIf(Object a, Object b, Object c, Object d) {
        if (((Foo) a) == null) {
            return "a is null";
        }
        if (((String) b) != null) {
            return "b is not null";
        }
        if (null == ((Integer) c)) {
            return "c is null";
        }
        if (null != ((Object) d)) {
            return "d is not null";
        }
        if (((Foo) a) != d) {
            return "a is not d";
        }
        return "done.";
    }

    public static String enclosedCastAndCheckInTenary(Object a, Object b, Object c, Object d) {
        return ((Foo) a) == null
                ? "a is null"
                : ((String) b) != null
                ? "b is not null"
                : null == ((Integer) c)
                ? "c is null"
                : null != ((Object) d)
                ? "d is not null"
                : ((Foo) a) != d
                ? "a is not d"
                : "done.";
    }
}
