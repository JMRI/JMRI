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
     * @param address
     * @param speedUnit
     * @param trimReverseSpeed
     * @param warmUpForwardSeconds
     * @param warmUpReverseSeconds
     * @param powerManager
     * @param statusLabel
     * @param startStopButton
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
