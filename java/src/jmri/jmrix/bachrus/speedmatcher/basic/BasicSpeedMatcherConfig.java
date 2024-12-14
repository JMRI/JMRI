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
public class BasicSpeedMatcherConfig extends SpeedMatcherConfig {

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
     * @param address              Address of locomotive to speed match
     * @param targetStartSpeed     Target speed at vStart in the given speedUnit
     * @param targetTopSpeed       Target speed at vHigh in the given speedUnit
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
            JButton startStopButton) {
        super(address, speedUnit, trimReverseSpeed, warmUpForwardSeconds, warmUpReverseSeconds, powerManager, statusLabel, startStopButton);

        this.targetStartSpeed = targetStartSpeed;
        this.targetTopSpeed = targetTopSpeed;
    }

}
