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
import jmri.InstanceManager;
import jmri.jmrix.dccpp.DCCppInterface;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;

public class ConfigBaseStationAction extends DCCppSystemConnectionAction {

    public ConfigBaseStationAction(String name, DCCppSystemConnectionMemo memo) {
        super(name, memo);
    }

    public ConfigBaseStationAction(DCCppSystemConnectionMemo memo) {
        super(Bundle.getMessage("FieldManageBaseStationFrameTitle"), memo);
    }

    public ConfigBaseStationAction() {
        this(InstanceManager.getNullableDefault(DCCppSystemConnectionMemo.class));
    }

    /**
     *
     */
    private ConfigBaseStationFrame f = null;
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            
            DCCppSystemConnectionMemo memo = getSystemConnectionMemo();
            f = new ConfigBaseStationFrame(memo);
            DCCppTrafficController tc = memo.getDCCppTrafficController();
            tc.addDCCppListener(DCCppInterface.CS_INFO, f);
            
            // Request definitions for Turnouts, Sensors and Outputs
            tc.sendDCCppMessage(DCCppMessage.makeSensorListMsg(), f); 
            tc.sendDCCppMessage(DCCppMessage.makeTurnoutListMsg(), f);
            tc.sendDCCppMessage(DCCppMessage.makeOutputListMsg(), f); 
        }
        f.setExtendedState(Frame.NORMAL);
    }

}
