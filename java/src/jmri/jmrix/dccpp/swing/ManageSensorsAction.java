package jmri.jmrix.dccpp.swing;
/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision: 21510 $
 */

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractAction;
import jmri.jmrix.dccpp.DCCppConstants;
import jmri.jmrix.dccpp.DCCppSensor;
import jmri.jmrix.dccpp.DCCppSensorManager; // Need this?
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.jmrix.dccpp.DCCppInterface;
import jmri.jmrix.dccpp.DCCppMessage;

//import jmri.Block;
//import jmri.BlockManager;
//import jmri.PhysicalLocationReporter;
//import jmri.Reporter;
//import jmri.ReporterManager;
//import jmri.jmrit.operations.locations.Location;
//import jmri.jmrit.operations.locations.LocationManager;
//import jmri.jmrit.vsdecoder.VSDecoderManager;
//import jmri.jmrit.vsdecoder.listener.ListeningSpot;
//import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageSensorsAction extends AbstractAction {

    /**
     *
     */
    private ManageSensorsFrame f = null;
    private HashMap<String, DCCppSensor> sensorMap;
    
    public ManageSensorsAction(String s, String a) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            
            // Get info on Sensors
            DCCppSystemConnectionMemo systemMemo = jmri.InstanceManager.getDefault(DCCppSystemConnectionMemo.class);
            DCCppSensorManager smgr = (DCCppSensorManager)systemMemo.getSensorManager();
            // Send query for sensor values
            DCCppTrafficController tc = systemMemo.getDCCppTrafficController();
    
            f = new ManageSensorsFrame(smgr, tc);
            tc.addDCCppListener(DCCppInterface.CS_INFO, f);
            tc.sendDCCppMessage(DCCppMessage.getSensorListMsg(), f); // TODO: Put this in Constants?
        }
        f.setExtendedState(Frame.NORMAL);
    }

    static private Logger log = LoggerFactory
            .getLogger(ManageSensorsAction.class.getName());

}
