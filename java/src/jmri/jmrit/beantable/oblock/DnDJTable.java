package jmri.jmrit.beantable.oblock;

/**
 * GUI to define OBlocks
 * <P>
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
 * @author	Pete Cressman (C) 2010
 * @version $Revision$
 */
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
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnDJTable extends JTable implements DropTargetListener,
        DragGestureListener, DragSourceListener, Transferable {

    /**
     *
     */
    private static final long serialVersionUID = -6622218098364227729L;
    public static final String TableCellFlavorMime = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=jmri.jmrit.beantable.oblock.DnDJTable.TableCellSelection";
    public static final DataFlavor TABLECELL_FLAVOR = new DataFlavor(
            jmri.jmrit.beantable.oblock.DnDJTable.TableCellSelection.class,
            "application/x-jmri.jmrit.beantable.oblock.DnDJTable.TableCellSelection");

    Point _dropPoint;
    int[] _skipCols = new int[0];

    DnDJTable(TableModel model, int[] skipCols) {
        super(model);
        this.setTransferHandler(new DnDHandler(this));
        _skipCols = skipCols;
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY_OR_MOVE, this);
        new DropTarget(this, DnDConstants.ACTION_COPY, this);
    }

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
                } catch (UnsupportedFlavorException ufe) {
                    log.warn("DnDJTable.importData: at table " + getName() + " e= " + ufe);
                    return false;
                } catch (IOException ioe) {
                    log.warn("DnDJTable.importData: at table " + getName() + " e= " + ioe);
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
    public void dragExit(DropTargetEvent evt) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragExit ");
        //evt.getDropTargetContext().acceptDrag(DnDConstants.ACTION_COPY);
    }

    public void dragEnter(DropTargetDragEvent evt) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragEnter ");
        if (!dropOK(evt)) {
            evt.rejectDrag();
        }
    }

    public void dragOver(DropTargetDragEvent evt) {
        if (!dropOK(evt)) {
            evt.rejectDrag();
        }
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dropActionChanged ");
    }

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
    public void dragGestureRecognized(DragGestureEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragGestureRecognized ");
        //Transferable t = getTransferable(this);
        //e.startDrag(DragSource.DefaultCopyDrop, this, this); 
    }

    /**
     * ************** DragSourceListener ***********
     */
    public void dragDropEnd(DragSourceDropEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragDropEnd ");
    }

    public void dragEnter(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.DragSourceDragEvent ");
    }

    public void dragExit(DragSourceEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragExit ");
    }

    public void dragOver(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dragOver ");
    }

    public void dropActionChanged(DragSourceDragEvent e) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.dropActionChanged ");
    }

    /**
     * ************* Transferable ********************
     */
    public DataFlavor[] getTransferDataFlavors() {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.getTransferDataFlavors ");
        return new DataFlavor[]{TABLECELL_FLAVOR};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.isDataFlavorSupported ");
        return TABLECELL_FLAVOR.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        //if (log.isDebugEnabled()) log.debug("DnDJTable.getTransferData ");
        if (isDataFlavorSupported(TABLECELL_FLAVOR)) {
            int row = getSelectedRow();
            int col = getSelectedColumn();
            if (col >= 0 && row >= 0) {
                return getValueAt(row, col);
            }
        }
        return null;
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

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{TABLECELL_FLAVOR, DataFlavor.stringFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor.equals(TABLECELL_FLAVOR)) {
                return true;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                return true;
            }
            return false;
        }

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

        /**
         *
         */
        private static final long serialVersionUID = 8476413554238761109L;
        JTable _table;

        DnDHandler(JTable table) {
            _table = table;
        }

        //////////////export
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        public Transferable createTransferable(JComponent c) {
            JTable table = (JTable) c;
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            if (col < 0 || row < 0) {
                return null;
            }
            //if (log.isDebugEnabled()) log.debug("DnDHandler.createTransferable: at table "+
            //                                    getName()+" from ("+row+", "+col+") data= \""
            //                                    +table.getModel().getValueAt(row, col)+"\"");
            TableCellSelection tcss = new TableCellSelection(
                    (String) table.getModel().getValueAt(row, col), row, col, _table);
            return new TableCellTransferable(tcss);
        }

        public void exportDone(JComponent c, Transferable t, int action) {
            //if (log.isDebugEnabled()) log.debug("DnDHandler.exportDone at table ");
        }

        /////////////////////import
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            //if (log.isDebugEnabled()) log.debug("DnDHandler.canImport ");

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
            if (!canDoIt) {
                return false;
            }
            return true;
        }

        public boolean importData(JComponent comp, Transferable tr) {
            //if (log.isDebugEnabled()) log.debug("DnDHandler.importData ");
            DataFlavor[] flavors = new DataFlavor[]{TABLECELL_FLAVOR, DataFlavor.stringFlavor};

            if (!canImport(comp, flavors)) {
                return false;
            }

            try {
                if (tr.isDataFlavorSupported(TABLECELL_FLAVOR)
                        || tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    DnDJTable table = (DnDJTable) comp;
                    AbstractTableModel model = (AbstractTableModel) table.getModel();
                    int col = table.getSelectedColumn();
                    int row = table.getSelectedRow();
                    if (col >= 0 && row >= 0) {
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
                        //if (log.isDebugEnabled()) 
                        //    log.debug("DnDHandler.importData: data= "+data+" dropped at ("+row+", "+col+")");
                        return true;
                    }
                }
            } catch (UnsupportedFlavorException ufe) {
                log.warn("DnDHandler.importData: at table e= " + ufe);
            } catch (IOException ioe) {
                log.warn("DnDHandler.importData: at table e= " + ioe);
            }
            return false;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DnDJTable.class.getName());
}
