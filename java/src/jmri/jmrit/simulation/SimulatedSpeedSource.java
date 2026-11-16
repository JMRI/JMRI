package jmri.jmrit.simulation;

import java.util.Objects;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;
import jmri.DccLocoAddress;
import jmri.SpeedStepMode;

/**
 * Where simulated trains get their speed commands: for each loco address, the
 * speed step, speed-step mode and direction last commanded, and a notice when
 * they change. Each system's simulator provides one from its own command
 * station's state; the Internal connection's is
 * {@link InternalThrottleSpeedSource}.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public interface SimulatedSpeedSource {

    /**
     * The command last sent to an address. Called on the simulator's own
     * thread, so it must be safe to call from any thread.
     *
     * @param address the loco address
     * @return the command, or null if none has been sent
     */
    @CheckForNull
    @InvokeOnAnyThread
    Command getCommand(@Nonnull DccLocoAddress address);

    /**
     * Ask to be told when the command for an address changes.
     *
     * @param listener the listener
     */
    @InvokeOnAnyThread
    void addCommandListener(@Nonnull CommandListener listener);

    /**
     * Stop telling a listener.
     *
     * @param listener the listener
     */
    @InvokeOnAnyThread
    void removeCommandListener(@Nonnull CommandListener listener);

    /**
     * Told which addresses the placed trains' locos obey, whenever that set
     * changes, so a source that has to ask for commands address by address
     * can do so. Called with an empty set when the last train is removed, and
     * when the source stops being used. Calls come one at a time, never with
     * the simulator's lock held, on whichever thread placed or removed the
     * train. Does nothing by default.
     *
     * @param addresses the addresses the placed trains' locos obey
     */
    @InvokeOnAnyThread
    default void watch(@Nonnull Set<DccLocoAddress> addresses) {
        // most sources hear every address anyway
    }

    /**
     * Told when the command for an address changes. Called on whatever thread
     * the source receives commands on; keep it short.
     */
    @FunctionalInterface
    interface CommandListener {

        /**
         * @param address the address whose command changed
         */
        @InvokeOnAnyThread
        void commandChanged(@Nonnull DccLocoAddress address);
    }

    /**
     * A speed command, as the decoder would receive it.
     */
    @javax.annotation.concurrent.Immutable
    final class Command {

        private final int speedStep;
        private final SpeedStepMode mode;
        private final boolean forward;
        private final boolean emergencyStop;
        private final long timeNanos;

        /**
         * @param speedStep     speed step, 0 (stop) to the mode's number of
         *                      steps
         * @param mode          speed-step mode
         * @param forward       true for forward
         * @param emergencyStop true for an emergency stop, which stops the
         *                      loco at once whatever its momentum
         * @param timeNanos     when it was received, on the
         *                      {@link System#nanoTime()} scale
         * @throws IllegalArgumentException if the speed step is outside the
         *                                  mode's range; a programming error
         *                                  in the speed source, so the message
         *                                  is in English
         */
        @InvokeOnAnyThread
        public Command(int speedStep, @Nonnull SpeedStepMode mode, boolean forward, boolean emergencyStop,
                long timeNanos) {
            if (speedStep < 0 || speedStep > mode.numSteps) {
                throw new IllegalArgumentException("speed step " + speedStep + " outside 0 to " + mode.numSteps);
            }
            this.speedStep = speedStep;
            this.mode = mode;
            this.forward = forward;
            this.emergencyStop = emergencyStop;
            this.timeNanos = timeNanos;
        }

        /**
         * @return the speed step, 0 (stop) to the mode's number of steps
         */
        @InvokeOnAnyThread
        public int getSpeedStep() {
            return speedStep;
        }

        /**
         * @return the speed-step mode
         */
        @Nonnull
        @InvokeOnAnyThread
        public SpeedStepMode getMode() {
            return mode;
        }

        /**
         * @return true for forward
         */
        @InvokeOnAnyThread
        public boolean isForward() {
            return forward;
        }

        /**
         * @return true for an emergency stop
         */
        @InvokeOnAnyThread
        public boolean isEmergencyStop() {
            return emergencyStop;
        }

        /**
         * @return when the command was received, on the {@link System#nanoTime()} scale
         */
        @InvokeOnAnyThread
        public long getTimeNanos() {
            return timeNanos;
        }

        /**
         * @return the commanded throttle setting, 0 to 1
         */
        @InvokeOnAnyThread
        public double getSetting() {
            return emergencyStop ? 0.0 : (double) speedStep / mode.numSteps;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @InvokeOnAnyThread
        public boolean equals(Object o) {
            if (!(o instanceof Command)) {
                return false;
            }
            Command c = (Command) o;
            return c.speedStep == speedStep && c.mode == mode && c.forward == forward
                    && c.emergencyStop == emergencyStop && c.timeNanos == timeNanos;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @InvokeOnAnyThread
        public int hashCode() {
            return Objects.hash(speedStep, mode, forward, emergencyStop, timeNanos);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @InvokeOnAnyThread
        public String toString() {
            return (emergencyStop ? "emergency stop" : speedStep + "/" + mode.numSteps)
                    + (forward ? " forward" : " reverse");
        }
    }
}
