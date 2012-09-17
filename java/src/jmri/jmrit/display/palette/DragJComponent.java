// DragJComponent.java
package jmri.jmrit.display.palette;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;

import jmri.jmrit.display.Editor;

/**
 * Gives a JComponent the capability to Drag and Drop 
 * <P>
 *
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
 * @author			Pete Cressman  Copyright 2011
 *
 */
 public abstract class DragJComponent extends JPanel implements DragGestureListener, DragSourceListener, Transferable {    

     DataFlavor _dataFlavor;
     public DragJComponent(DataFlavor flavor) {
         super();
         String borderName = ItemPalette.convertText("dragToPanel");
         setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                          borderName));
         int width = Math.max(100, getPreferredSize().width);
         int height = Math.max(65, getPreferredSize().height);
         setPreferredSize(new java.awt.Dimension(width, height));
         setToolTipText(ItemPalette.rbp.getString("ToolTipDragIcon"));
//         setBorder(new javax.swing.border.EmptyBorder(12,5,12,5));
         DragSource dragSource = DragSource.getDefaultDragSource();
         dragSource.createDefaultDragGestureRecognizer(this,
                     DnDConstants.ACTION_COPY, this);
         _dataFlavor = flavor;
     }

     /**************** DragGestureListener ***************/
     public void dragGestureRecognized(DragGestureEvent e) {
         if (log.isDebugEnabled()) log.debug("DragJLabel.dragGestureRecognized ");
         //Transferable t = getTransferable(this);
         e.startDrag(DragSource.DefaultCopyDrop, this, this); 
     }
     /**************** DragSourceListener ************/
     public void dragDropEnd(DragSourceDropEvent e) {
         if (log.isDebugEnabled()) log.debug("DragJLabel.dragDropEnd ");
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
     /*************** Transferable *********************/
     public DataFlavor[] getTransferDataFlavors() {
         //if (log.isDebugEnabled()) log.debug("DragJLabel.getTransferDataFlavors ");
         return new DataFlavor[] { _dataFlavor, DataFlavor.stringFlavor };
     }
     public boolean isDataFlavorSupported(DataFlavor flavor) {
         //if (log.isDebugEnabled()) log.debug("DragJLabel.isDataFlavorSupported ");
         return _dataFlavor.equals(flavor);
     }

     static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DragJComponent.class.getName());
}


