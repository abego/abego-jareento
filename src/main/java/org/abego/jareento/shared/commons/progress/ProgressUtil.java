package org.abego.jareento.shared.commons.progress;

import org.abego.commons.io.IORunnable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class ProgressUtil {

    public static void withProgressAndDurationDo(
            Supplier<String> startMessage,
            LongFunction<String> endMessage,
            Consumer<String> progress,
            IORunnable runnable) {

        progress.accept(startMessage.get());
        long start = System.currentTimeMillis();
        try {
            runnable.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        long durationMillis = System.currentTimeMillis() - start;
        progress.accept(endMessage.apply(durationMillis));
    }
}
