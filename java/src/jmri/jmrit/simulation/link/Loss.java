package jmri.jmrit.simulation.link;

import java.util.Random;

import javax.annotation.Nonnull;

/**
 * Drops messages: each with a probability, and the next few when told to,
 * for tests that need a message lost at a chosen moment. The rest go on at
 * once.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class Loss extends Stage {

    private final double probability;
    private final Random random;
    private final java.util.concurrent.atomic.AtomicInteger toDrop = new java.util.concurrent.atomic.AtomicInteger();

    /**
     * @param probability the chance of dropping each message, 0 to 1
     * @param seed        the seed of the random choice, so a run can be
     *                    repeated
     */
    public Loss(double probability, long seed) {
        if (!(probability >= 0 && probability <= 1)) {
            throw new IllegalArgumentException("probability " + probability);
        }
        this.probability = probability;
        this.random = new Random(seed);
    }

    /**
     * Drop the next messages that arrive, whatever the probability.
     *
     * @param count how many
     */
    public void dropNext(int count) {
        toDrop.addAndGet(Math.max(0, count));
    }

    @Override
    protected void take(@Nonnull LinkFrame frame, long atNanos) {
        if (toDrop.getAndUpdate(n -> Math.max(0, n - 1)) > 0
                || (probability > 0 && random.nextDouble() < probability)) {
            drop(frame);
        } else {
            pass(frame, atNanos, atNanos);
        }
    }

    @Override
    @Nonnull
    public String describe() {
        return Bundle.getMessage("StageLoss", String.format(java.util.Locale.ROOT, "%.3f", probability));
    }
}
