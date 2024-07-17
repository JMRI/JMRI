package jmri.jmrix.bachrus.speedmatcher.basic;

import javax.swing.JButton;
import javax.swing.JLabel;

import jmri.DccLocoAddress;
import jmri.PowerManager;
import jmri.jmrix.bachrus.Speed;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcherConfig;

/**
 * Configuration data for a basic speed matcher
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class BasicSpeedMatcherConfig extends SpeedMatcherConfig{
    
    //<editor-fold defaultstate="collapsed" desc="Enums">
    public enum SpeedTable {
        SIMPLE, ADVANCED, ESU
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    public float targetStartSpeed;
    public float targetTopSpeed;
    //</editor-fold>
    
    /**
     * Create a config object for a Basic Speed Matcher
     * 
     * @param address
     * @param targetStartSpeed
     * @param targetTopSpeed
     * @param speedUnit
     * @param trimReverseSpeed
     * @param warmUpForwardSeconds
     * @param warmUpReverseSeconds
     * @param powerManager
     * @param statusLabel
     * @param startStopButton 
     */
    public BasicSpeedMatcherConfig(
            DccLocoAddress address, 
            float targetStartSpeed,
            float targetTopSpeed,
            Speed.Unit speedUnit, 
            boolean trimReverseSpeed, 
            int warmUpForwardSeconds, 
            int warmUpReverseSeconds, 
            PowerManager powerManager,
            JLabel statusLabel, 
            JButton startStopButton)
    {
        super(address, speedUnit, trimReverseSpeed, warmUpForwardSeconds, warmUpReverseSeconds, powerManager, statusLabel, startStopButton);
        
        this.targetStartSpeed = targetStartSpeed;
        this.targetTopSpeed = targetTopSpeed;
    }

}
