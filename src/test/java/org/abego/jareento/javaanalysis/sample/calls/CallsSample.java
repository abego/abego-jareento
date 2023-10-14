package org.abego.jareento.javaanalysis.sample.calls;

import java.util.function.Consumer;

public class CallsSample {

    public static class Root {
        void meth1(Consumer<String> out) {
            out.accept("Base#meth1()");
        }

        void meth2(Consumer<String> out) {
            out.accept("Base#meth2()");
        }
    }

    public static class SubA extends Root {
        void meth1(Consumer<String> out) {
            out.accept("SubA#meth1()");
        }

        void meth3(SubA target, Consumer<String> out) {
            target.meth1(out);
            target.meth2(out);
        }

        void meth4(Root target, Consumer<String> out) {
            target.meth1(out);
            target.meth2(out);
        }
    }

    public static class Main {
        void meth3(SubA target, Consumer<String> out) {
            target.meth1(out);
            target.meth2(out);
        }

        void meth4(Root target, Consumer<String> out) {
            target.meth1(out);
            target.meth2(out);
        }
    }
}
