// DragJLabel.java
package jmri.jmrit.catalog;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.io.IOException;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gives a JLabel the capability to Drag and Drop
 * <P>
 *
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author	Pete Cressman Copyright 2009
 *
 */
public class DragJLabel extends JLabel implements DragGestureListener, DragSourceListener, Transferable {

    /**
     *
     */
    private static final long serialVersionUID = 7320850014520972400L;
    protected DataFlavor _dataFlavor;

    public DragJLabel(DataFlavor flavor) {
        super();
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY, this);
        _dataFlavor = flavor;
    }

    /**
     * ************** DragGestureListener **************
     */
    public void dragGestureRecognized(DragGestureEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("DragJLabel.dragGestureRecognized ");
        }
        //Transferable t = getTransferable(this);
        e.startDrag(DragSource.DefaultCopyDrop, this, this);
    }

    /**
     * ************** DragSourceListener ***********
     */
    public void dragDropEnd(DragSourceDropEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("DragJLabel.dragDropEnd ");
        }
    }

    public void dragEnter(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.DragSourceDragEvent ");
    }

    public void dragExit(DragSourceEvent e) {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.dragExit ");
    }

    public void dragOver(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.dragOver ");
    }

    public void dropActionChanged(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.dropActionChanged ");
    }

    /**
     * ************* Transferable ********************
     */
    public DataFlavor[] getTransferDataFlavors() {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.getTransferDataFlavors ");
        return new DataFlavor[]{_dataFlavor, DataFlavor.stringFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.isDataFlavorSupported ");
        if (DataFlavor.stringFlavor.equals(flavor)) {
            return true;
        }
        return _dataFlavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("DragJLabel.getTransferData ");
        }
        if (_dataFlavor.equals(flavor)) {
            return getIcon();
        }
        if (DataFlavor.stringFlavor.equals(flavor)) {
            NamedIcon icon = (NamedIcon) getIcon();
            return icon.getURL();
        }
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(DragJLabel.class.getName());
}
