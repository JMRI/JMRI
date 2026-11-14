package jmri.jmrit.trainrecord;

import java.util.List;

import javax.annotation.Nonnull;

import jmri.InvokeOnLayoutThread;

/**
 * A set-up check from another part of JMRI, listed with train tracking's own
 * in {@link TrainTrackingChecks#run()}. Like them, it only reads, and each
 * finding says what is wrong in the user's terms.
 * <p>
 * Implementations are found with {@link java.util.ServiceLoader}
 * ({@code @ServiceProvider(service = SetUpCheck.class)}), so train tracking
 * needs to know nothing of the parts of JMRI they check.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public interface SetUpCheck {

    /**
     * Check now.
     *
     * @return the findings, possibly none
     */
    @Nonnull
    @InvokeOnLayoutThread
    List<TrainTrackingChecks.Finding> check();
}
