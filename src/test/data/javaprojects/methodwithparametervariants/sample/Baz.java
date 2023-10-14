package sample;

@SuppressWarnings("all")
public abstract class Baz extends Foo {

    //override from Bar, ignoring the problem between Bar.m2(Number) and
    // Foo.m2(String), as Bar.m2(Number) "wins", being the first defining
    // "m2" in this hierarchy.
    public void m2(Number p) {
    }

    // PROBLEM 6: Redefining method sample.I2.m8(java.lang.Number) with different parameters: sample.Baz.m8(java.lang.Object)
    public void m8(Object p) {
    }
}
