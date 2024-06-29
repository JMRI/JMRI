package jmri.jmrix.bachrus.speedmatcher;

import javax.swing.JButton;
import javax.swing.JLabel;

import jmri.DccLocoAddress;
import jmri.PowerManager;
import jmri.jmrix.bachrus.Speed;

import org.slf4j.Logger;

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
