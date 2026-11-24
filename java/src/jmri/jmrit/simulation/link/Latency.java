package jmri.jmrit.simulation.link;

import java.util.Random;

import javax.annotation.Nonnull;

/**
 * A fixed delay with jitter, as USB, a network or a gateway's processing
 * adds. Each message is held for the mean plus a random part, spread evenly
 * up to the jitter either side, but never passed on before the one in front:
 * order is kept, as on a serial line or a TCP stream.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class Latency extends Stage {

    private final long meanNanos;
    private final long jitterNanos;
    private final Random random;
    private long lastOnNanos = Long.MIN_VALUE;

    /**
     * @param meanNanos   the mean delay, not negative
     * @param jitterNanos how far either side of the mean the delay may be, not
     *                    negative and not more than the mean
     * @param seed        the seed of the random part, so a run can be repeated
     */
    public Latency(long meanNanos, long jitterNanos, long seed) {
        if (meanNanos < 0 || jitterNanos < 0 || jitterNanos > meanNanos) {
            throw new IllegalArgumentException("mean " + meanNanos + ", jitter " + jitterNanos);
        }
        this.meanNanos = meanNanos;
        this.jitterNanos = jitterNanos;
        this.random = new Random(seed);
    }

    @Override
    protected void take(@Nonnull LinkFrame frame, long atNanos) {
        long delay = meanNanos;
        if (jitterNanos > 0) {
            delay += (long) ((random.nextDouble() * 2 - 1) * jitterNanos);
        }
        long on = atNanos + delay;
        if (lastOnNanos != Long.MIN_VALUE && on - lastOnNanos < 0) {
            on = lastOnNanos;
        }
        lastOnNanos = on;
        pass(frame, atNanos, on);
    }

    @Override
    @Nonnull
    public String describe() {
        return Bundle.getMessage("StageLatency", ms(meanNanos), ms(jitterNanos));
    }

    static String ms(long nanos) {
        return String.format(java.util.Locale.ROOT, "%.1f", nanos / 1e6);
    }
}
