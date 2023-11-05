package org.abego.jareento.javaanalysis.internal;

import java.util.function.Consumer;
import java.util.function.Function;

public class LogFromConsumer<T> implements Consumer<T> {
    private final StringBuilder log = new StringBuilder();
    private final Function<T, String> toLogLine;

    public LogFromConsumer(Function<T, String> toLogLine) {
        this.toLogLine = toLogLine;
    }

    @Override
    public void accept(T t) {
        log.append(toLogLine.apply(t));
        log.append('\n');
    }

    public String getText() {
        return log.toString();
    }
}
