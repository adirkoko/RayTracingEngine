package renderer;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Listener for render progress events.
 */
@FunctionalInterface
public interface RenderProgressListener {

    /**
     * Listener that ignores all progress events.
     */
    RenderProgressListener NONE = progress -> {
    };

    /**
     * Listener that prints pixel rendering progress to the console.
     */
    RenderProgressListener CONSOLE = progress -> {
        if (progress.stage() == RenderStage.RENDER_PIXELS)
            System.out.printf("%5.1f%%\r", progress.percent());
        if (progress.stage() == RenderStage.DONE)
            System.out.println();
    };

    /**
     * Receives one progress event.
     *
     * @param progress progress event
     */
    void onProgress(RenderProgress progress);

    /**
     * Releases listener resources.
     */
    default void close() {
    }

    /**
     * Creates a listener that suppresses delegate failures.
     *
     * @param listener listener to protect
     * @return resilient listener
     */
    static RenderProgressListener resilient(RenderProgressListener listener) {
        return resilient(listener, failure -> {
        });
    }

    /**
     * Creates a listener that suppresses delegate failures and reports them to a handler.
     *
     * @param listener       listener to protect
     * @param failureHandler handler for listener failures
     * @return resilient listener
     */
    static RenderProgressListener resilient(
            RenderProgressListener listener,
            Consumer<RuntimeException> failureHandler) {
        if (listener == null)
            throw new IllegalArgumentException("Progress listener cannot be null");
        if (failureHandler == null)
            throw new IllegalArgumentException("Failure handler cannot be null");

        return new RenderProgressListener() {
            @Override
            public void onProgress(RenderProgress progress) {
                try {
                    listener.onProgress(progress);
                } catch (RuntimeException e) {
                    handleFailure(e);
                }
            }

            @Override
            public void close() {
                try {
                    listener.close();
                } catch (RuntimeException e) {
                    handleFailure(e);
                }
            }

            /**
             * Reports a listener failure without rethrowing it.
             *
             * @param failure listener failure
             */
            private void handleFailure(RuntimeException failure) {
                try {
                    failureHandler.accept(failure);
                } catch (RuntimeException ignore) {
                }
            }
        };
    }

    /**
     * Creates a listener that forwards each progress event to all provided listeners.
     *
     * @param listeners listeners to notify in order
     * @return combined listener
     */
    static RenderProgressListener combine(RenderProgressListener... listeners) {
        if (listeners == null)
            throw new IllegalArgumentException("Progress listeners cannot be null");

        RenderProgressListener[] listenersCopy = Arrays.copyOf(listeners, listeners.length);
        for (RenderProgressListener listener : listenersCopy)
            if (listener == null)
                throw new IllegalArgumentException("Progress listener cannot be null");

        return new RenderProgressListener() {
            @Override
            public void onProgress(RenderProgress progress) {
                for (RenderProgressListener listener : listenersCopy)
                    listener.onProgress(progress);
            }

            @Override
            public void close() {
                RuntimeException failure = null;
                for (RenderProgressListener listener : listenersCopy) {
                    try {
                        listener.close();
                    } catch (RuntimeException e) {
                        if (failure == null)
                            failure = e;
                        else
                            failure.addSuppressed(e);
                    }
                }
                if (failure != null) throw failure;
            }
        };
    }
}
