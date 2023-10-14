package com.example.barbazfoo;

class Foo {
    Bar bar;

    class Zum {
        Bar bar;

        class Bar {
            void doo() {
                // empty by intent
            }

        }
    }

    public static String getSomeText(Object object) {
        return object instanceof com.example.barbazfoo.Bar
                ? ((com.example.barbazfoo.Bar) object).someText()
                : "unknown-text";
    }
}
