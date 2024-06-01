/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.bachrus.speedmatcher;

import javax.swing.JLabel;
import jmri.DccLocoAddress;
import jmri.PowerManager;
import jmri.jmrix.bachrus.Speed;
import org.slf4j.Logger;

/**
 *
 * @author toddt
 */
public class SpeedMatcherConfig {

    public enum SpeedMatcherType {
        BASIC, SPEEDSTEPSCALE
    }

    public enum SpeedTable {
        SIMPLE, ADVANCED, ESU
    }

    public SpeedMatcherType type;
    public SpeedTable speedTable; 

    public DccLocoAddress dccLocoAddress;
    public Logger logger;
    public PowerManager powerManager;

    public float targetStartSpeed;
    public float targetTopSpeed;
    public Speed.Unit speedUnit;
    public boolean warmUpLoco;
    public boolean trimReverseSpeed;
    
    public JLabel statusLabel;

    public SpeedMatcherConfig(
            SpeedMatcherType type, 
            SpeedTable speedTable, 
            DccLocoAddress address, 
            float targetStartSpeed, 
            float targetTopSpeed, 
            Speed.Unit speedUnit, 
            boolean warmUpLoco, 
            boolean trimReverseSpeed,
            PowerManager powerManager, 
            Logger logger, 
            JLabel statusLabel
    ) {
        this.type = type;
        this.speedTable = speedTable;
        this.targetStartSpeed = targetStartSpeed;
        this.targetTopSpeed = targetTopSpeed;
        this.speedUnit = speedUnit;
        
        this.warmUpLoco = warmUpLoco;
        this.trimReverseSpeed = trimReverseSpeed;
        
        this.dccLocoAddress = address;
        this.powerManager = powerManager;

        this.logger = logger;
        this.statusLabel = statusLabel;
    }

}