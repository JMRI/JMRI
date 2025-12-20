package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import javax.swing.JButton;
import javax.swing.JLabel;

import jmri.DccLocoAddress;
import jmri.PowerManager;
import jmri.jmrix.bachrus.Speed;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcherConfig;

/**
 * Configuration data for a speed step scale speed matcher
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class SpeedStepScaleSpeedMatcherConfig extends SpeedMatcherConfig {

    //<editor-fold defaultstate="collapsed" desc="Enums">
    public enum SpeedTable {
        ADVANCED, ESU
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    public SpeedTableStepSpeed targetMaxSpeedStep;
    public JLabel actualMaxSpeedField;
    //</editor-fold>

    /**
     * Create a config object for a Speed Step Scale Speed Matcher
     *
     * @param address              Address of locomotive to speed match
     * @param targetMaxSpeedStep   Target maximum speed step (corresponds to
     *                             maximum speed)
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
     * @param actualMaxSpeedField  JLabel for indicating the locomotive's actual
     *                             max speed
     * @param startStopButton      JButton for starting and stopping speed
     *                             matching
     */
    public SpeedStepScaleSpeedMatcherConfig(
            DccLocoAddress address,
            SpeedTableStepSpeed targetMaxSpeedStep,
            Speed.Unit speedUnit,
            boolean trimReverseSpeed,
            int warmUpForwardSeconds,
            int warmUpReverseSeconds,
            PowerManager powerManager,
            JLabel statusLabel,
            JLabel actualMaxSpeedField,
            JButton startStopButton) {
        super(address, speedUnit, trimReverseSpeed, warmUpForwardSeconds, warmUpReverseSeconds, powerManager, statusLabel, startStopButton);

        this.targetMaxSpeedStep = targetMaxSpeedStep;
        this.actualMaxSpeedField = actualMaxSpeedField;
    }

}
