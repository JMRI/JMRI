package jmri.jmrit.vsdecoder.swing;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractAction;
import jmri.Block;
import jmri.BlockManager;
import jmri.PhysicalLocationReporter;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author   Mark Underwood Copyright (C) 2011
 * 
 */
public class ManageLocationsAction extends AbstractAction {

    private ManageLocationsFrame f = null;
    private HashMap<String, PhysicalLocation> reporterMap;
    private HashMap<String, PhysicalLocation> blockMap;
    private HashMap<String, PhysicalLocation> opsMap;
    private ListeningSpot listenerLoc;

    public ManageLocationsAction(String s, String a) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            // Handle the Listener
            listenerLoc = VSDecoderManager.instance().getVSDecoderPreferences().getListenerPosition();

            // Handle Reporters
            ReporterManager rmgr = jmri.InstanceManager.getDefault(jmri.ReporterManager.class);
            String[] reporterNameArray = rmgr.getSystemNameArray();
            Object[][] reporterTable = new Object[reporterNameArray.length][6];
            reporterMap = new HashMap<String, PhysicalLocation>();
            int i = 0;
            for (String s : reporterNameArray) {
                Reporter r = rmgr.getByDisplayName(s);
                if (r instanceof PhysicalLocationReporter) {
                    reporterMap.put(s, ((PhysicalLocationReporter) r).getPhysicalLocation());
                    PhysicalLocation p = ((PhysicalLocationReporter) r).getPhysicalLocation();
                    reporterTable[i][0] = s;
                    reporterTable[i][1] = true;
                    reporterTable[i][2] = p.getX();
                    reporterTable[i][3] = p.getY();
                    reporterTable[i][4] = p.getZ();
                    reporterTable[i][5] = p.isTunnel();
                } else {
                    reporterTable[i][0] = s;
                    reporterTable[i][1] = false;
                    reporterTable[i][2] = Float.valueOf(0.0f);
                    reporterTable[i][3] = Float.valueOf(0.0f);
                    reporterTable[i][4] = Float.valueOf(0.0f);
                    reporterTable[i][5] = false;
                }
                i++;
            }

            // Handle Blocks
            BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
            String[] blockNameArray = bmgr.getSystemNameArray();
            Object[][] blockTable = new Object[blockNameArray.length][6];
            blockMap = new HashMap<String, PhysicalLocation>();
            i = 0;
            for (String s : blockNameArray) {
                Block b = bmgr.getByDisplayName(s);
                // NOTE: Unlike Reporters, all Blocks are (now) PhysicalLocationReporters, so no need to do a check here.
                // We'll keep the explicit cast for now, but it's not actually necessary.
                blockMap.put(s, ((PhysicalLocationReporter) b).getPhysicalLocation());
                PhysicalLocation p = ((PhysicalLocationReporter) b).getPhysicalLocation();
                blockTable[i][0] = s;
                blockTable[i][1] = true;
                blockTable[i][2] = p.getX();
                blockTable[i][3] = p.getY();
                blockTable[i][4] = p.getZ();
                blockTable[i][5] = p.isTunnel();
                i++;
            }

            // Handle Ops Locations
            LocationManager lmgr = LocationManager.instance();
            List<Location> locations = lmgr.getLocationsByIdList();
            opsMap = new HashMap<String, PhysicalLocation>();
            log.debug("TableSize : " + locations.size());
            Object[][] opsTable = new Object[locations.size()][6];
            i = 0;
            for (Location l : locations) {
                if (log.isDebugEnabled()) {
                    log.debug("i = " + i + "MLA " + l.getId() + " Name: " + l.getName() + " table " + java.util.Arrays.toString(opsTable[i]));
                }
                PhysicalLocation p = l.getPhysicalLocation();
                Boolean use = false;
                if (p == PhysicalLocation.Origin) {
                    use = false;
                } else {
                    use = true;
                }
                opsTable[i][0] = l.getName();
                opsTable[i][1] = use;
                opsTable[i][2] = p.getX();
                opsTable[i][3] = p.getY();
                opsTable[i][4] = p.getZ();
                opsTable[i][5] = p.isTunnel();
                opsMap.put(l.getName(), l.getPhysicalLocation());
                i++;
            }

            f = new ManageLocationsFrame(listenerLoc, reporterTable, opsTable, blockTable);
        }
        f.setExtendedState(Frame.NORMAL);
    }

    private final static Logger log = LoggerFactory
            .getLogger(ManageLocationsAction.class);

}
