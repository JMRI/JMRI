package jmri.jmrit.vsdecoder.swing;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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

/**
 * Loading of Reporters, Blocks, Locations and Listener attributes.
 *
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
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class ManageLocationsAction extends AbstractAction {

    private ManageLocationsFrame f = null;
    private ListeningSpot listenerLoc;

    public ManageLocationsAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (f == null || !f.isVisible()) {
            // Handle the Listener
            listenerLoc = VSDecoderManager.instance().getVSDecoderPreferences().getListenerPosition();

            // Handle Reporters
            ReporterManager rmgr = jmri.InstanceManager.getDefault(jmri.ReporterManager.class);
            Set<Reporter> reporterSet = rmgr.getNamedBeanSet();
            Object[][] reporterTable = new Object[reporterSet.size()][7];
            int i = 0;
            for (Reporter r : reporterSet) {
                if (r != null) {
                    if (r instanceof PhysicalLocationReporter) {
                        PhysicalLocation p = ((PhysicalLocationReporter) r).getPhysicalLocation();
                        reporterTable[i][0] = r.getSystemName();
                        reporterTable[i][1] = r.getDisplayName();
                        reporterTable[i][2] = true;
                        reporterTable[i][3] = p.getX();
                        reporterTable[i][4] = p.getY();
                        reporterTable[i][5] = p.getZ();
                        reporterTable[i][6] = p.isTunnel();
                    } else {
                        reporterTable[i][0] = r.getSystemName();
                        reporterTable[i][1] = r.getDisplayName();
                        reporterTable[i][2] = false;
                        reporterTable[i][3] = Float.valueOf(0.0f);
                        reporterTable[i][4] = Float.valueOf(0.0f);
                        reporterTable[i][5] = Float.valueOf(0.0f);
                        reporterTable[i][6] = false;
                    }
                }
                i++;
            }

            // Handle Blocks
            BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
            Set<Block> blockSet = bmgr.getNamedBeanSet();
            Object[][] blockTable = new Object[blockSet.size()][7];
            i = 0;
            for (Block b : blockSet) {
                // NOTE: Unlike Reporters, all Blocks are (now) PhysicalLocationReporters, so no need to do a check here.
                // We'll keep the explicit cast for now, but it's not actually necessary.
                if (b != null) {
                    PhysicalLocation p = ((PhysicalLocationReporter) b).getPhysicalLocation();
                    blockTable[i][0] = b.getSystemName();
                    blockTable[i][1] = b.getDisplayName();
                    blockTable[i][2] = true;
                    blockTable[i][3] = p.getX();
                    blockTable[i][4] = p.getY();
                    blockTable[i][5] = p.getZ();
                    blockTable[i][6] = p.isTunnel();
                }
                i++;
            }

            // Handle Ops Locations
            LocationManager lmgr = jmri.InstanceManager.getDefault(LocationManager.class);
            List<Location> locations = lmgr.getLocationsByIdList();
            log.debug("TableSize: {}", locations.size());
            Object[][] opsTable = new Object[locations.size()][6];
            i = 0;
            for (Location l : locations) {
                log.debug("i: {}, MLA: {}, Name: {}, table: {}", i, l.getId(), l.getName(), java.util.Arrays.toString(opsTable[i]));
                opsTable[i][0] = l.getName();
                PhysicalLocation p = l.getPhysicalLocation();
                if (p == PhysicalLocation.Origin) {
                    opsTable[i][1] = false;
                } else {
                    opsTable[i][1] = true;
                }
                opsTable[i][2] = p.getX();
                opsTable[i][3] = p.getY();
                opsTable[i][4] = p.getZ();
                opsTable[i][5] = p.isTunnel();
                i++;
            }

            f = new ManageLocationsFrame(listenerLoc, reporterTable, opsTable, blockTable);
        }
        f.setExtendedState(Frame.NORMAL);
    }

    private final static Logger log = LoggerFactory.getLogger(ManageLocationsAction.class);

}
