package toostrictcastinforeach;

import java.util.List;

public class Foo {
    void useInterable(Object a) {
        for (String s : (Iterable<String>) a) {
            // empty
        }
    }

    void useList(Object a) {
        for (String s : (List<String>) a) {
            // empty
        }
    }

    void useListExtraParenthesis(Object a) {
        for (String s : ((List<String>) a)) {
            // empty
        }
    }
}
