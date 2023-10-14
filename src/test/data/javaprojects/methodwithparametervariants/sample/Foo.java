package sample;

@SuppressWarnings("all")
public abstract class Foo<T> extends Bar implements I1, I2 {
    //override from Bar
    public void m1(Integer p) {
    }

    // PROBLEM 1: Redefining method Bar.m2(java.lang.Number) with different parameters: Foo.m2(java.lang.String)
    public void m2(String p) {
    }

    //override from I2, ignoring the conflict between m5(Double) and
    // m5(Double, Double) in I2, as m5(Double) "wins" in I2, being the first
    // with the name "m5"
    public void m5(Double p) {
    }

    //override from I2
    public void m6(Double p) {
    }

    // PROBLEM 5: Redefining method sample.I2.m7(java.lang.Number) with different parameters: sample.Foo.m7(java.lang.Double)
    public void m7(Double p) {
    }

    // no problem/clash with m1(Integer), as parameterless methods are ignored
    public void m1() {
    }

    // PROBLEM 3: Redefining method sample.Bar.m9(java.lang.String, java.lang.Integer, java.lang.Number) with different parameters: sample.Foo.m9(java.lang.String, java.lang.Number, java.lang.Double)
    void m9(String p1, Number p2, Double p3) {
        // empty by intent
    }

    // PROBLEM 2: Redefining method sample.Bar.m3(java.lang.Object) with different parameters: sample.Foo.m3(java.util.List<java.lang.Object>)
    // PROBLEM 4: Redefining method sample.I1.m3(java.lang.Number) with different parameters: sample.Foo.m3(java.util.List<java.lang.Object>)
    void m3(java.util.List<Object> o) {
        // empty by intent
    }

    void m3(T o) {
        // empty by intent
    }

    void m10(String a) {
        // empty by intent
    }

    // no problem with m10(String) as overloads in same class are ignored
    void m10(Double a) {
        // empty by intent
    }
}
