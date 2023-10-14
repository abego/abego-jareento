package sample;

public interface I2 {

    void m4(String p);

    void m5(Double p);

    // no problem/clash with I2.m5(java.lang.Double), as only methods with same
    // parameter count are checked for clashes
    void m5(Double p, Double p2);

    void m6(Double p);

    void m7(Number p);

    void m8(Number p);
}
