package org.abego.jareento.shared.commons.progress;

import org.abego.commons.progress.ProgressWithRange;

import java.util.function.Consumer;

//TODO move to common. Make ProgressListenerWithStream use this class internally to avoid code duplication
public final class ProgressWithRangeListenerWithStringConsumer
        implements ProgressWithRange.Listener {

    private final Consumer<String> progress;

    private ProgressWithRangeListenerWithStringConsumer(Consumer<String> progress) {
        this.progress = progress;
    }

    public static ProgressWithRangeListenerWithStringConsumer newProgressListenerWithStringConsumer(
            Consumer<String> progress) {
        return new ProgressWithRangeListenerWithStringConsumer(progress);
    }

    @Override
    public void accept(ProgressWithRange.Event event) {
        boolean unknownCount = event.getRangeSize() == Integer.MAX_VALUE;
        String remainingTimeText = event.getRemainingTime()
                .map(t -> String.format("%d s", t.getSeconds()))
                .orElse("?"); //NON-NLS
        String decoratedText = !event.getText().isEmpty()
                ? String.format(" - %s", event.getText())  //NON-NLS
                : "";
        String line = unknownCount
                ? String.format("[%d s] %s (%d processed)%s", //NON-NLS
                event.getElapsedTime().getSeconds(),
                event.getTopic(),
                event.getOffsetInRange(),
                decoratedText)
                : String.format("[%d s] %s (%d of %d, %d %%, estimated remaining time: %s)%s", //NON-NLS
                event.getElapsedTime().getSeconds(),
                event.getTopic(),
                event.getOffsetInRange(),
                event.getRangeSize(),
                event.getPercentageDone(),
                remainingTimeText,
                decoratedText);
        progress.accept(line);
    }
}
