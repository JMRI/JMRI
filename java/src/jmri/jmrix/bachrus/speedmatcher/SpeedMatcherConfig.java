package jmri.jmrix.bachrus.speedmatcher;

import javax.swing.JButton;
import javax.swing.JLabel;

import jmri.DccLocoAddress;
import jmri.PowerManager;
import jmri.jmrix.bachrus.Speed;

/**
 * Shared configuration data for a speed matcher
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public abstract class SpeedMatcherConfig {

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    public DccLocoAddress dccLocoAddress;
    public PowerManager powerManager;
    public Speed.Unit speedUnit;

    public boolean trimReverseSpeed;

    public int warmUpForwardSeconds;
    public int warmUpReverseSeconds;

    public JLabel statusLabel;
    public JButton startStopButton;
    //</editor-fold>

    /**
     * Constructor for the abstract SpeedMatcherConfig at the core of any Speed
     * Matcher Config
     *
     * @param address              Address of locomotive to speed match
     * @param speedUnit            Speed.Unit to speed match the locomotive in
     * @param trimReverseSpeed     Set to true to trim the locomotive's reverse
     *                             speed, false otherwise
     * @param warmUpForwardSeconds Number of seconds to warm up the locomotive
     *                             before forward speed matching; set to 0 to
     *                             skip the forward warm up
     * @param warmUpReverseSeconds Number of seconds to warm up the locomotive
     *                             before trimming revers speed; set to 0 to
     *                             skip the reverse warm up
     * @param powerManager         PowerManager for turning on the DCC system
     *                             power
     * @param statusLabel          JLabel status label in the SpeedoConsoleFrame
     * @param startStopButton      JButton for starting and stopping speed
     *                             matching
     */
    public SpeedMatcherConfig(
            DccLocoAddress address,
            Speed.Unit speedUnit,
            boolean trimReverseSpeed,
            int warmUpForwardSeconds,
            int warmUpReverseSeconds,
            PowerManager powerManager,
            JLabel statusLabel,
            JButton startStopButton
    ) {
        this.speedUnit = speedUnit;

        this.trimReverseSpeed = trimReverseSpeed;

        this.warmUpForwardSeconds = warmUpForwardSeconds;
        this.warmUpReverseSeconds = warmUpReverseSeconds;

        this.dccLocoAddress = address;
        this.powerManager = powerManager;

        this.statusLabel = statusLabel;
        this.startStopButton = startStopButton;
    }
}
