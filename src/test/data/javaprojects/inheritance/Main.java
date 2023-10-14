public class Main {
    public static void entry(Base b, InterfaceA ia, Sub1 s1, Sub2 s2) {
        b.methodBase1("");
        b.methodBase2("");

        ia.methodInterfaceA();

        s1.methodBase1("");
        s1.methodBase2("");
        s1.methodSub1();

        s2.methodSub2();
        s2.methodInterfaceA();
    }
}
