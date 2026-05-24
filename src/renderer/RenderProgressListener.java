package renderer;

import java.util.Arrays;

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
