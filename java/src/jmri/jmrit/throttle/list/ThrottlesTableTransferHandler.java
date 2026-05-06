package jmri.jmrit.throttle.list;

import java.awt.Cursor;
import java.awt.datatransfer.*;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.ArrayList;

import javax.activation.DataHandler;
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

    private boolean dropDone = false;

    private final DataFlavor throttleFrameObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ThrottleFrame.class.getName(), "JMRI Throttle Controller UI");
    private final DataFlavor throttleSimplePanelObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + SimpleThrottlePanel.class.getName(), "JMRI Throttle Controller UI");

    private JTable table = null;

    public ThrottlesTableTransferHandler(JTable throttleControllers) {
        this.table = throttleControllers;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        assert (c == table);
        ThrottleControllerUI tf = ((ThrottlesTableModel) table.getModel()).getValueAt(table.getSelectedRow(), table.getSelectedColumn());
        if (tf instanceof ThrottleFrame) {
            return new DataHandler(tf, throttleFrameObjectFlavor.getMimeType());
        }
        if (tf instanceof SimpleThrottlePanel) {
            return new DataHandler(tf, throttleSimplePanelObjectFlavor.getMimeType());
        }
        return null;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        if (dropDone) { //  if the drop is done, then reset the cursor and return false.
            dropDone = false;
            table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return false;
        }

        boolean b = info.getComponent() == table && info.isDrop() &&
                ( info.isDataFlavorSupported(throttleFrameObjectFlavor) || 
                  info.isDataFlavorSupported(throttleSimplePanelObjectFlavor) || 
                  info.isDataFlavorSupported(RosterEntrySelection.rosterEntryFlavor) );

        table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
        return b;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        try {
            JTable target = (JTable) info.getComponent();
            JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();            
            target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            if (info.isDataFlavorSupported(throttleFrameObjectFlavor)) {
                try {
                    ThrottleFrame tf = (ThrottleFrame) info.getTransferable().getTransferData(throttleFrameObjectFlavor);
                    if (tf != null) {
                        return ((ThrottlesTableModel) table.getModel()).moveThrottleController(tf, dl.getRow(), dl.getColumn());                        
                    }
                } catch ( UnsupportedFlavorException | IOException e) {
                    log.error("Could not drag'n drop throttle frame.", e);
                }
            }
            if (info.isDataFlavorSupported(throttleSimplePanelObjectFlavor)) {
                try {
                    SimpleThrottlePanel tf = (SimpleThrottlePanel) info.getTransferable().getTransferData(throttleSimplePanelObjectFlavor);
                    if (tf != null) {
                        return ((ThrottlesTableModel) table.getModel()).moveThrottleController(tf, dl.getRow(), dl.getColumn());                        
                    }
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
        dropDone = true;
    }

   private static final Logger log = LoggerFactory.getLogger(ThrottlesTableTransferHandler.class);
}
