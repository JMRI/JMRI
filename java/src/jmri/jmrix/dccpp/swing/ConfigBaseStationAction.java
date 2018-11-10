package jmri.jmrix.dccpp.swing;
/*
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <p>
 *
 * @author   Mark Underwood Copyright (C) 2011
 * 
 */

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.dccpp.DCCppInterface;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppSensorManager; // Need this?
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.jmrix.dccpp.DCCppTurnoutManager;

public class ConfigBaseStationAction extends AbstractAction {

    /**
     *
     */
    private ConfigBaseStationFrame f = null;
    
    public ConfigBaseStationAction(String s, String a) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            
            // Get info on Sensors
            DCCppSystemConnectionMemo systemMemo = jmri.InstanceManager.getDefault(DCCppSystemConnectionMemo.class);
            DCCppSensorManager smgr = (DCCppSensorManager)systemMemo.getSensorManager();
            DCCppTurnoutManager tmgr = (DCCppTurnoutManager)systemMemo.getTurnoutManager();
            // Send query for sensor values
            DCCppTrafficController tc = systemMemo.getDCCppTrafficController();
    
            f = new ConfigBaseStationFrame(smgr, tmgr, tc);
            tc.addDCCppListener(DCCppInterface.CS_INFO, f);
            tc.sendDCCppMessage(DCCppMessage.makeSensorListMsg(), f); // TODO: Put this in Constants?
            tc.sendDCCppMessage(DCCppMessage.makeTurnoutListMsg(), f); // TODO: Put this in Constants?
            tc.sendDCCppMessage(DCCppMessage.makeOutputListMsg(), f); // TODO: Put this in Constants?
        }
        f.setExtendedState(Frame.NORMAL);
    }

}
