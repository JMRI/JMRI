package jmri.jmrit.simulation;

import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InvokeOnAnyThread;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;

/**
 * A loco in a simulated train: its address, length, momentum CVs, consist
 * CVs, which way it faces, and its speeds.
 * <p>
 * Speeds come from the measured steps of a roster speed profile where there
 * is one, read by the simulator's own code. Without one, speed is in
 * proportion to the throttle setting up to a full speed, by default a scale
 * 100 mph at the layout scale set in the Warrant preferences.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.Immutable
public final class SimulatedLoco {

    /** Scale 100 mph in mm/s at full size. */
    static final double FULL_SIZE_100_MPH_MM_PER_S = 44704.0;

    private final DccLocoAddress address;
    @CheckForNull
    private final String rosterId;
    private final double lengthMm;
    private final int cv3;
    private final int cv4;
    private final int cv19;
    private final int cv23;
    private final int cv24;
    private final boolean facingRear;
    private final SpeedTable forward;
    private final SpeedTable reverse;

    private SimulatedLoco(Builder b) {
        address = b.address;
        rosterId = b.rosterId;
        lengthMm = b.lengthMm;
        cv3 = b.cv3;
        cv4 = b.cv4;
        cv19 = b.cv19;
        cv23 = b.cv23;
        cv24 = b.cv24;
        facingRear = b.facingRear;
        double full = (b.fullSpeed > 0) ? b.fullSpeed : defaultFullSpeed();
        SpeedTable f = SpeedTable.fromProfile(b.profile, true);
        SpeedTable r = SpeedTable.fromProfile(b.profile, false);
        forward = (f != null) ? f : SpeedTable.proportional(full);
        reverse = (r != null) ? r : SpeedTable.proportional(full);
    }

    /**
     * @return a scale 100 mph at the layout scale, in mm/s
     */
    static double defaultFullSpeed() {
        float scale = InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
        return FULL_SIZE_100_MPH_MM_PER_S / scale;
    }

    /**
     * Start describing a loco with no roster entry.
     *
     * @param address its address
     * @return a builder
     */
    @InvokeOnAnyThread
    public static Builder builder(@Nonnull DccLocoAddress address) {
        return new Builder(address);
    }

    /**
     * Start describing a loco from its roster entry: its address, speed
     * profile, and the CVs 3, 4, 19, 23 and 24 stored in its roster file.
     *
     * @param entry the roster entry
     * @return a builder
     */
    @InvokeOnAnyThread
    public static Builder builder(@Nonnull RosterEntry entry) {
        Builder b = new Builder(entry.getDccLocoAddress());
        b.rosterId = entry.getId();
        b.profile = entry.getSpeedProfile();
        Map<String, Integer> cvs = RosterCvs.read(entry);
        b.cv3 = cvs.getOrDefault("3", 0);
        b.cv4 = cvs.getOrDefault("4", 0);
        b.cv19 = cvs.getOrDefault("19", 0);
        b.cv23 = cvs.getOrDefault("23", 0);
        b.cv24 = cvs.getOrDefault("24", 0);
        return b;
    }

    /** Builds a {@link SimulatedLoco}. */
    public static final class Builder {

        private final DccLocoAddress address;
        private String rosterId;
        private double lengthMm;
        private int cv3;
        private int cv4;
        private int cv19;
        private int cv23;
        private int cv24;
        private boolean facingRear;
        private RosterSpeedProfile profile;
        private double fullSpeed;

        private Builder(DccLocoAddress address) {
            this.address = address;
        }

        /**
         * @param mm the loco's length, which Head Only detection sees
         * @return this
         */
        @InvokeOnAnyThread
        public Builder length(double mm) {
            lengthMm = mm;
            return this;
        }

        /**
         * @param cv3 acceleration CV
         * @param cv4 deceleration CV
         * @return this
         * @throws IllegalArgumentException with a message for the user if a
         *                                  value is outside 0 to 255
         */
        @InvokeOnAnyThread
        public Builder momentum(int cv3, int cv4) {
            this.cv3 = checkCv(cv3);
            this.cv4 = checkCv(cv4);
            return this;
        }

        /**
         * @param cv19 consist address (bits 0-6) and direction (bit 7)
         * @param cv23 acceleration adjustment
         * @param cv24 deceleration adjustment
         * @return this
         * @throws IllegalArgumentException with a message for the user if a
         *                                  value is outside 0 to 255
         */
        @InvokeOnAnyThread
        public Builder consist(int cv19, int cv23, int cv24) {
            this.cv19 = checkCv(cv19);
            this.cv23 = checkCv(cv23);
            this.cv24 = checkCv(cv24);
            return this;
        }

        /**
         * The loco faces the rear of the train, so running forward moves the
         * train rear first.
         *
         * @return this
         */
        @InvokeOnAnyThread
        public Builder facingRear() {
            facingRear = true;
            return this;
        }

        /**
         * @param profile a speed profile to take measured speeds from
         * @return this
         */
        @InvokeOnAnyThread
        public Builder speedProfile(@CheckForNull RosterSpeedProfile profile) {
            this.profile = profile;
            return this;
        }

        /**
         * @param mmPerSecond speed at full throttle, used where the speed
         *                    profile has no measured speeds
         * @return this
         */
        @InvokeOnAnyThread
        public Builder fullSpeed(double mmPerSecond) {
            fullSpeed = mmPerSecond;
            return this;
        }

        private static int checkCv(int v) {
            if (v < 0 || v > 255) {
                throw new IllegalArgumentException(Bundle.getMessage("CvOutOfRange", v));
            }
            return v;
        }

        /**
         * @return the loco
         */
        @InvokeOnAnyThread
        public SimulatedLoco build() {
            return new SimulatedLoco(this);
        }
    }

    /**
     * @return the loco's own address
     */
    @Nonnull
    @InvokeOnAnyThread
    public DccLocoAddress getAddress() {
        return address;
    }

    /**
     * @return the roster entry's id, or null for a loco with no roster entry
     */
    @CheckForNull
    @InvokeOnAnyThread
    public String getRosterId() {
        return rosterId;
    }

    /**
     * @return the loco's length, mm, which Head Only detection sees; 0 if not given
     */
    @InvokeOnAnyThread
    public double getLengthMm() {
        return lengthMm;
    }

    /**
     * @return CV3, the acceleration rate
     */
    @InvokeOnAnyThread
    public int getCv3() {
        return cv3;
    }

    /**
     * @return CV4, the deceleration rate
     */
    @InvokeOnAnyThread
    public int getCv4() {
        return cv4;
    }

    /**
     * @return CV19, the consist address (bits 0-6) and direction (bit 7)
     */
    @InvokeOnAnyThread
    public int getCv19() {
        return cv19;
    }

    /**
     * @return CV23, the consist acceleration adjustment
     */
    @InvokeOnAnyThread
    public int getCv23() {
        return cv23;
    }

    /**
     * @return CV24, the consist deceleration adjustment
     */
    @InvokeOnAnyThread
    public int getCv24() {
        return cv24;
    }

    /**
     * @return true if the loco faces the rear of the train
     */
    @InvokeOnAnyThread
    public boolean isFacingRear() {
        return facingRear;
    }

    /**
     * @return true if both directions' speeds come from measurements, false if
     *         either is approximate (in proportion to the throttle)
     */
    @InvokeOnAnyThread
    public boolean hasMeasuredSpeeds() {
        return forward.isMeasured() && reverse.isMeasured();
    }

    /**
     * @return true if CV19 puts the loco in a consist
     */
    @InvokeOnAnyThread
    public boolean isInConsist() {
        return (cv19 & 0x7F) != 0;
    }

    /**
     * @return the address the loco obeys speed commands sent to: its consist
     *         address while in a consist, otherwise its own
     */
    @Nonnull
    @InvokeOnAnyThread
    public DccLocoAddress getRespondingAddress() {
        if (isInConsist()) {
            return new DccLocoAddress(cv19 & 0x7F, false);
        }
        return address;
    }

    SpeedTable forwardTable() {
        return forward;
    }

    SpeedTable reverseTable() {
        return reverse;
    }
}
