package jmri.jmrit.vsdecoder.swing;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import jmri.Block;
import jmri.BlockManager;
import jmri.PhysicalLocation;
import jmri.PhysicalLocationReporter;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.vsdecoder.VSDecoderManager;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
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
            ReporterManager rmgr = jmri.InstanceManager.getDefault(ReporterManager.class);
            Set<Reporter> reporterSet = rmgr.getNamedBeanSet();
            Object[][] reporterTable = new Object[reporterSet.size()][7];
            int i = 0;
            for (Reporter r : reporterSet) {
                if (r != null) {
                    if (r instanceof PhysicalLocationReporter) {
                        PhysicalLocation p = ((PhysicalLocationReporter) r).getPhysicalLocation();
                        reporterTable[i][ManageLocationsTableModel.SYSNAMECOL] = r.getSystemName();
                        reporterTable[i][ManageLocationsTableModel.USERNAMECOL] = r.getDisplayName();
                        reporterTable[i][ManageLocationsTableModel.USECOL + 1] = true;
                        reporterTable[i][ManageLocationsTableModel.XCOL + 1] = p.getX();
                        reporterTable[i][ManageLocationsTableModel.YCOL + 1] = p.getY();
                        reporterTable[i][ManageLocationsTableModel.ZCOL + 1] = p.getZ();
                        reporterTable[i][ManageLocationsTableModel.TUNNELCOL + 1] = p.isTunnel();
                    } else {
                        reporterTable[i][ManageLocationsTableModel.SYSNAMECOL] = r.getSystemName();
                        reporterTable[i][ManageLocationsTableModel.USERNAMECOL] = r.getDisplayName();
                        reporterTable[i][ManageLocationsTableModel.USECOL + 1] = false;
                        reporterTable[i][ManageLocationsTableModel.XCOL + 1] = Float.valueOf(0.0f);
                        reporterTable[i][ManageLocationsTableModel.YCOL + 1] = Float.valueOf(0.0f);
                        reporterTable[i][ManageLocationsTableModel.ZCOL + 1] = Float.valueOf(0.0f);
                        reporterTable[i][ManageLocationsTableModel.TUNNELCOL + 1] = false;
                    }
                }
                i++;
            }

            // Handle Blocks
            BlockManager bmgr = jmri.InstanceManager.getDefault(BlockManager.class);
            Set<Block> blockSet = bmgr.getNamedBeanSet();
            Object[][] blockTable = new Object[blockSet.size()][7];
            i = 0;
            for (Block b : blockSet) {
                // NOTE: Unlike Reporters, all Blocks are (now) PhysicalLocationReporters, so no need to do a check here.
                // We'll keep the explicit cast for now, but it's not actually necessary.
                if (b != null) {
                    PhysicalLocation p = ((PhysicalLocationReporter) b).getPhysicalLocation();
                    blockTable[i][ManageLocationsTableModel.SYSNAMECOL] = b.getSystemName();
                    blockTable[i][ManageLocationsTableModel.USERNAMECOL] = b.getDisplayName();
                    blockTable[i][ManageLocationsTableModel.USECOL + 1] = true;
                    blockTable[i][ManageLocationsTableModel.XCOL + 1] = p.getX();
                    blockTable[i][ManageLocationsTableModel.YCOL + 1] = p.getY();
                    blockTable[i][ManageLocationsTableModel.ZCOL + 1] = p.getZ();
                    blockTable[i][ManageLocationsTableModel.TUNNELCOL + 1] = p.isTunnel();
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
                opsTable[i][ManageLocationsTableModel.NAMECOL] = l.getName();
                PhysicalLocation p = l.getPhysicalLocation();
                if (p == PhysicalLocation.Origin) {
                    opsTable[i][ManageLocationsTableModel.USECOL] = false;
                } else {
                    opsTable[i][ManageLocationsTableModel.USECOL] = true;
                }
                opsTable[i][ManageLocationsTableModel.XCOL] = p.getX();
                opsTable[i][ManageLocationsTableModel.YCOL] = p.getY();
                opsTable[i][ManageLocationsTableModel.ZCOL] = p.getZ();
                opsTable[i][ManageLocationsTableModel.TUNNELCOL] = p.isTunnel();
                i++;
            }

            f = new ManageLocationsFrame(listenerLoc, reporterTable, opsTable, blockTable);
        }
        f.setExtendedState(Frame.NORMAL);
    }

    private final static Logger log = LoggerFactory.getLogger(ManageLocationsAction.class);

}
