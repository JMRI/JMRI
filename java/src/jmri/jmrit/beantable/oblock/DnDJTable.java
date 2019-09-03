package jmri.jmrit.beantable.oblock;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
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
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks
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
 * @author	Pete Cressman (C) 2010
 */
public class DnDJTable extends JTable implements DropTargetListener,
        DragGestureListener, DragSourceListener, Transferable {

    public static final String TableCellFlavorMime = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=jmri.jmrit.beantable.oblock.DnDJTable.TableCellSelection";
    public static final DataFlavor TABLECELL_FLAVOR = new DataFlavor(
            jmri.jmrit.beantable.oblock.DnDJTable.TableCellSelection.class,
            "application/x-jmri.jmrit.beantable.oblock.DnDJTable.TableCellSelection");

    private Point _dropPoint;
    private int[] _skipCols = new int[0];

    DnDJTable(TableModel model, int[] skipCols) {
        super(model);
        this.setTransferHandler(new DnDHandler(this));
        _skipCols = skipCols;
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY_OR_MOVE, this);
        new DropTarget(this, DnDConstants.ACTION_COPY, this);
    }

    @Override
    public boolean editCellAt(int row, int column, java.util.EventObject e) {
        boolean res = super.editCellAt(row, column, e);
        java.awt.Component c = this.getEditorComponent();
        if (c instanceof javax.swing.JTextField) {
            ((JTextField) c).selectAll();
        }
        return res;
    }

    Point getDropPoint() {
        return _dropPoint;
    }

    private boolean dropOK(DropTargetDragEvent evt) {
        Transferable tr = evt.getTransferable();
        if (tr.isDataFlavorSupported(TABLECELL_FLAVOR)
                || tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            _dropPoint = evt.getLocation();
            //DnDHandler handler = (DnDHandler)getTransferHandler();
            int col = columnAtPoint(_dropPoint);
            int row = rowAtPoint(_dropPoint);
            for (int i = 0; i < _skipCols.length; i++) {
                if (_skipCols[i] == col) {
                    return false;
                }
            }
            if (tr.isDataFlavorSupported(TABLECELL_FLAVOR)) {
                try {
                    // don't allow a cell import back into the cell exported from 
                    TableCellSelection tcss = (TableCellSelection) tr.getTransferData(TABLECELL_FLAVOR);
                    if (row == tcss.getRow() && col == tcss.getCol() && this == tcss.getTable()) {
                        return false;
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    log.warn("DnDJTable.importData: at table {} e= ", getName(), ex);
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * ************************* DropTargetListener ***********************
     */
    @Override
    public void dragExit(DropTargetEvent evt) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragExit ");
        //evt.getDropTargetContext().acceptDrag(DnDConstants.ACTION_COPY);
    }

    @Override
    public void dragEnter(DropTargetDragEvent evt) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragEnter ");
        if (!dropOK(evt)) {
            evt.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent evt) {
        if (!dropOK(evt)) {
            evt.rejectDrag();
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dropActionChanged ");
    }

    @Override
    public void drop(DropTargetDropEvent evt) {
        try {
            Point pt = evt.getLocation();
            String data = null;
            Transferable tr = evt.getTransferable();
            if (tr.isDataFlavorSupported(TABLECELL_FLAVOR)
                    || tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                AbstractTableModel model = (AbstractTableModel) getModel();
                int col = columnAtPoint(pt);
                int row = rowAtPoint(pt);
                if (col >= 0 && row >= 0) {
                    row = convertRowIndexToModel(row);
                    TableCellSelection sel = (TableCellSelection) tr.getTransferData(TABLECELL_FLAVOR);
                    data = (String) sel.getTransferData(DataFlavor.stringFlavor);
                    model.setValueAt(data, row, col);
                    model.fireTableDataChanged();
                    //if (log.isDebugEnabled()) 
                    //    log.debug("DnDJTable.drop: data= "+data+" dropped at ("+row+", "+col+")");
                    evt.dropComplete(true);
                    return;
                }
            } else {
                log.warn("TransferHandler.importData: supported DataFlavors not avaialable at table from "
                        + tr.getClass().getName());
            }
        } catch (IOException ioe) {
            log.warn("caught IOException", ioe);
        } catch (UnsupportedFlavorException ufe) {
            log.warn("caught UnsupportedFlavorException", ufe);
        }
        if (log.isDebugEnabled()) {
            log.debug("DropJTree.drop REJECTED!");
        }
        evt.rejectDrop();
    }

    /**
     * ************** DragGestureListener **************
     */
    @Override
    public void dragGestureRecognized(DragGestureEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragGestureRecognized ");
        //Transferable t = getTransferable(this);
        //e.startDrag(DragSource.DefaultCopyDrop, this, this); 
    }

    /**
     * ************** DragSourceListener ***********
     */
    @Override
    public void dragDropEnd(DragSourceDropEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragDropEnd ");
    }

    @Override
    public void dragEnter(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.DragSourceDragEvent ");
    }

    @Override
    public void dragExit(DragSourceEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragExit ");
    }

    @Override
    public void dragOver(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragOver ");
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dropActionChanged ");
    }

    /**
     * ************* Transferable ********************
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.getTransferDataFlavors ");
        return new DataFlavor[]{TABLECELL_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.isDataFlavorSupported ");
        return TABLECELL_FLAVOR.equals(flavor);
    }

    @Nonnull
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.getTransferData ");
        if (isDataFlavorSupported(TABLECELL_FLAVOR)) {
            int row = getSelectedRow();
            int col = getSelectedColumn();
            if (col >= 0 && row >= 0) {
                row = convertRowIndexToModel(row);
                return getValueAt(row, col);
            }
        }
        return "";
    }

    class TableCellSelection extends StringSelection {

        int _row;
        int _col;
        JTable _table;

        TableCellSelection(String data, int row, int col, JTable table) {
            super(data);
            _row = row;
            _col = col;
            _table = table;
        }

        int getRow() {
            return _row;
        }

        int getCol() {
            return _col;
        }

        JTable getTable() {
            return _table;
        }
    }

    static class TableCellTransferable implements Transferable {

        TableCellSelection _tcss;

        TableCellTransferable(TableCellSelection tcss) {
            _tcss = tcss;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{TABLECELL_FLAVOR, DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return (flavor.equals(TABLECELL_FLAVOR) || flavor.equals(DataFlavor.stringFlavor));
        }

        @Nonnull
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(TABLECELL_FLAVOR)) {
                return _tcss;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                return _tcss.getTransferData(DataFlavor.stringFlavor);
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    class DnDHandler extends TransferHandler {

        JTable _table;

        DnDHandler(JTable table) {
            _table = table;
        }

        //////////////export
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        @Override
        public Transferable createTransferable(JComponent c) {
            if (c instanceof JTable) {
                JTable table = (JTable) c;
                int col = table.getSelectedColumn();
                int row = table.getSelectedRow();
                if (col < 0 || row < 0) {
                    return null;
                }
                row = table.convertRowIndexToModel(row);
                //if (log.isDebugEnabled()) log.debug("DnDHandler.createTransferable: at table "+
                //                                    getName()+" from ("+row+", "+col+") data= \""
                //                                    +table.getModel().getValueAt(row, col)+"\"");
                TableCellSelection tcss = new TableCellSelection((String) table.getModel().getValueAt(row, col),
                        row, col, _table);
                return new TableCellTransferable(tcss);
            }
            return null;
        }
    
        @Override
        public void exportDone(JComponent c, Transferable t, int action) {
            //if (log.isDebugEnabled()) log.debug("DnDHandler.exportDone at table ");
        }

        /////////////////////import
        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {

            boolean canDoIt = false;
            for (int k = 0; k < transferFlavors.length; k++) {
                if (transferFlavors[k].equals(TABLECELL_FLAVOR)
                        || transferFlavors[k].equals(DataFlavor.stringFlavor)) {
                    if (comp instanceof JTable) {
                        canDoIt = true;
                        break;
                    }
                }
            }
            return canDoIt;
        }

        @Override
        public boolean importData(JComponent comp, Transferable tr) {
            DataFlavor[] flavors = new DataFlavor[]{TABLECELL_FLAVOR, DataFlavor.stringFlavor};

            if (!canImport(comp, flavors)) {
                return false;
            }

            try {
                if (tr.isDataFlavorSupported(TABLECELL_FLAVOR)
                        || tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    if (comp instanceof DnDJTable) {
                        DnDJTable table = (DnDJTable) comp;
                        AbstractTableModel model = (AbstractTableModel) table.getModel();
                        int col = table.getSelectedColumn();
                        int row = table.getSelectedRow();
                        if (col >= 0 && row >= 0) {
                            row = table.convertRowIndexToView(row);
                            String data = (String) tr.getTransferData(DataFlavor.stringFlavor);
                            model.setValueAt(data, row, col);
                            model.fireTableDataChanged();
                            java.awt.Container parent = table;
                            do {
                                parent = parent.getParent();
                            } while (parent != null && !(parent instanceof JInternalFrame));
                            if (parent != null) {
                                ((JInternalFrame) parent).moveToFront();
                            }
                            log.debug("DnDHandler.importData: data= {} dropped at ({}, {})", data, row, col);
                            return true;
                        }
                    }
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                log.warn("DnDHandler.importData: at table e= " + ex);
            }
            return false;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DnDJTable.class);

}
