package jmri.jmrit.throttle.list;

import java.awt.Cursor;
import java.awt.datatransfer.*;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.implementation.SimpleThrottlePanel;
import jmri.jmrit.throttle.implementation.ThrottleFrame;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;
import jmri.util.datatransfer.RosterEntrySelection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to handle transfers (drag'n drop) within and to the throttle list panel 
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Lionel Jeanson
 */
public class ThrottlesTableTransferHandler extends TransferHandler {

    private JTable table = null;

    public ThrottlesTableTransferHandler(JTable throttleControllers) {
        table = throttleControllers;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        assert (c == table);
        return new ThrottleUITransferable(((ThrottlesTableModel) table.getModel()).getValueAt(table.getSelectedRow(), table.getSelectedColumn()));
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        boolean b = info.getComponent() == table && info.isDrop() &&
                ( info.isDataFlavorSupported(ThrottleUITransferable.ThrottleFrameObjectFlavor) || 
                  info.isDataFlavorSupported(ThrottleUITransferable.ThrottleSimplePanelObjectFlavor) || 
                  info.isDataFlavorSupported(RosterEntrySelection.rosterEntryFlavor) );

        table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
        return b;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        try {
            JTable target = (JTable) info.getComponent();
            JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();            
            target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            if (info.isDataFlavorSupported(ThrottleUITransferable.ThrottleFrameObjectFlavor)) {
                try {
                    ThrottleFrame tf = (ThrottleFrame) info.getTransferable().getTransferData(ThrottleUITransferable.ThrottleFrameObjectFlavor);
                    if (tf != null) {
                        return ((ThrottlesTableModel) table.getModel()).moveThrottleController(tf, dl.getRow(), dl.getColumn());                        
                    }
                    return true;
                } catch ( UnsupportedFlavorException | IOException e) {
                    log.error("Could not drag'n drop throttle frame.", e);
                }
            }
            if (info.isDataFlavorSupported(ThrottleUITransferable.ThrottleSimplePanelObjectFlavor)) {
                try {
                    SimpleThrottlePanel tf = (SimpleThrottlePanel) info.getTransferable().getTransferData(ThrottleUITransferable.ThrottleSimplePanelObjectFlavor);
                    if (tf != null) {
                        return ((ThrottlesTableModel) table.getModel()).moveThrottleController(tf, dl.getRow(), dl.getColumn());                        
                    }
                    return true;
                } catch ( UnsupportedFlavorException | IOException e) {
                    log.error("Could not drag'n drop throttle frame.", e);
                }
            }            
            if (info.isDataFlavorSupported(RosterEntrySelection.rosterEntryFlavor)) {
                try {
                    ArrayList<RosterEntry> REs = RosterEntrySelection.getRosterEntries(info.getTransferable());
                    ThrottleControllersUIContainer tw = ((ThrottlesTableModel) table.getModel()).getThrottleControllersContainerAt(dl.getColumn());
                    for (RosterEntry re : REs) {
                        ThrottleControllerUI tf = tw.newThrottleController();
                        tf.toFront();
                        tf.setRosterEntry(re);
                    }
                    return true;
                } catch (
                        UnsupportedFlavorException |
                        IOException e) {
                    log.error("Could not drag'n drop roster entry.", e);
                }
            }
        } catch (ClassCastException e) {
            log.error("CastException ", e);
        }
        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int act) {
        table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

   private static final Logger log = LoggerFactory.getLogger(ThrottlesTableTransferHandler.class);
}
