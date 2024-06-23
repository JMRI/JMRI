package jmri.jmrix.bachrus.speedmatcher;

import javax.swing.JButton;
import javax.swing.JLabel;

import jmri.DccLocoAddress;
import jmri.PowerManager;
import jmri.jmrix.bachrus.Speed;

import org.slf4j.Logger;

/**
 * Configuration data for a speed matcher
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class SpeedMatcherConfig {

    //<editor-fold defaultstate="collapsed" desc="Enums">    
    public enum SpeedMatcherType {
        BASIC, SPEEDSTEPSCALE
    }

    public enum SpeedTable {
        SIMPLE, ADVANCED, ESU
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    public SpeedMatcherType type;
    public SpeedTable speedTable;

    public DccLocoAddress dccLocoAddress;
    public PowerManager powerManager;

    public float targetStartSpeed;
    public float targetTopSpeed;
    public Speed.Unit speedUnit;

    public boolean trimReverseSpeed;

    public int warmUpForwardSeconds;
    public int warmUpReverseSeconds;

    public JLabel statusLabel;
    public JButton startStopButton;
    //</editor-fold>

    public SpeedMatcherConfig(
            SpeedMatcherType type,
            SpeedTable speedTable,
            DccLocoAddress address,
            float targetStartSpeed,
            float targetTopSpeed,
            Speed.Unit speedUnit,
            boolean trimReverseSpeed,
            int warmUpForwardSeconds,
            int warmUpReverseSeconds,
            PowerManager powerManager,
            Logger logger,
            JLabel statusLabel,
            JButton startStopButton
    ) {
        this.type = type;
        this.speedTable = speedTable;
        this.targetStartSpeed = targetStartSpeed;
        this.targetTopSpeed = targetTopSpeed;
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
