package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gives a JComponent the capability to Drag and Drop
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
 * @author Pete Cressman Copyright 2011
 *
 */
public abstract class DragJComponent extends JPanel implements DragGestureListener, DragSourceListener, Transferable {

    DataFlavor _dataFlavor;

    public DragJComponent(DataFlavor flavor,  JComponent comp) {
        super();
        String borderName = ItemPalette.convertText("dragToPanel");
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                borderName));
        // guestimate border is about 5 pixels thick. plus some margin
        add(comp);
        Dimension dim = comp.getPreferredSize();
        int width = Math.max(100, dim.width + 20);
        int height = Math.max(65, dim.height + 20);
        setPreferredSize(new java.awt.Dimension(width, height));
        setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY, this);
        _dataFlavor = flavor;
    }

    protected boolean okToDrag() {
        return true;
    }
    
    /**
     * ************** DragGestureListener **************
     */
    @Override
    public void dragGestureRecognized(DragGestureEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("DragJLabel.dragGestureRecognized ");
        }
        if (okToDrag()) {
            e.startDrag(DragSource.DefaultCopyDrop, this, this);            
        }
    }

    /**
     * ************** DragSourceListener ***********
     */
    @Override
    public void dragDropEnd(DragSourceDropEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("DragJLabel.dragDropEnd ");
        }
    }

    @Override
    public void dragEnter(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.DragSourceDragEvent ");
    }

    @Override
    public void dragExit(DragSourceEvent e) {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.dragExit ");
    }

    @Override
    public void dragOver(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.dragOver ");
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.dropActionChanged ");
    }

    /**
     * ************* Transferable ********************
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.getTransferDataFlavors ");
        return new DataFlavor[]{_dataFlavor, DataFlavor.stringFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        //if (log.isDebugEnabled()) log.debug("DragJLabel.isDataFlavorSupported ");
        return _dataFlavor.equals(flavor);
    }

    private final static Logger log = LoggerFactory.getLogger(DragJComponent.class);
}
