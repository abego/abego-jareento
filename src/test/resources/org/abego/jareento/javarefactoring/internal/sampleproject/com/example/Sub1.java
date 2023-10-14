package com.example;

public class Sub1 extends Base {
    public static class Sub1_InnerClass extends Base_InnerClass {
        @Override
        void innerMethodBase1(String a) {
            super.innerMethodBase1(a);
        }
    }

    void methodSub1() {
    }

    @Override
    void methodBase1(String a) {
        super.methodBase1(a);
    }
}
