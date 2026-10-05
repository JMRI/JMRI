package jmri.jmrit.roster.swing.functiontable;

import java.awt.datatransfer.*;
import java.io.IOException;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.util.iharder.dnd.URIDrop;
import jmri.util.swing.EditableResizableImagePanel;

/**
 * A TransferHandler that handl drag'n drop over the FunctionTable. 
 * It can handle rows drag'n drop but also drag'n drop into the EditableResizableImagePanel in some of the table cells.
 * 
 * It is used in the FunctionLabelPane.
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
 * @author Lionel Jeanson 2009-2026
 */

public class FunctionTableRowTransferHandler extends TransferHandler {
    private final JTable table;
    private final DataFlavor rowFlavor = new DataFlavor(Integer.class, "row index");

    public FunctionTableRowTransferHandler(JTable table) {
        this.table = table;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        return new DataHandler(table.getSelectedRow(), rowFlavor);
    }

    @Override
    public boolean canImport(TransferSupport support) {
        EditableResizableImagePanel impan = FunctionTableMouseListener.getEditableResizableImagePanelAt(table, support.getDropLocation().getDropPoint().x, support.getDropLocation().getDropPoint().y);
        if (impan != null) {
            for (DataFlavor df :  support.getDataFlavors()) {
                if (df.isRepresentationClassReader()) {
                    return true;
                }
            }
            return ( support.isDataFlavorSupported(DataFlavor.imageFlavor) ||
                     support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                     support.isDataFlavorSupported(DataFlavor.stringFlavor));                        
        }
        return support.isDrop() && support.isDataFlavorSupported(rowFlavor);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            log.debug("TransferSupport() can't do import : data flavor");
            return false;
        }
        if (!(support.getDropLocation() instanceof JTable.DropLocation)) {
            log.debug("TransferSupport() can't do import : not a drop location");
            return false;
        }
        JTable.DropLocation drop = (JTable.DropLocation) support.getDropLocation();        
        // is if a drop on an imageicon
        EditableResizableImagePanel impan = FunctionTableMouseListener.getEditableResizableImagePanelAt(table, drop.getDropPoint().x, drop.getDropPoint().y);
        if (impan != null) {
            return URIDrop.importExternalTransferable(support.getTransferable(), impan);
        }
        // else play with rows
        try {
            int from = (Integer) support.getTransferable().getTransferData(rowFlavor);
            int to = drop.getRow();
            if (to < 0 || from == to || from + 1 == to) {
                log.debug("TransferSupport() can't do import : same indexes");
                return false;
            }
            ((FunctionTableModel) table.getModel()).moveRow(from, to > from ? to - 1 : to);
            table.getSelectionModel().setSelectionInterval( to > from ? to - 1 : to, to > from ? to - 1 : to );
            return true;
        } catch (UnsupportedFlavorException | IOException e) {
            log.debug("importData() Exception : ", e);
            return false;
        }
    }

    private static class DataHandler implements Transferable {
        private final Integer row;
        private final DataFlavor flavor;

        DataHandler(Integer row, DataFlavor flavor) {
            this.row = row;
            this.flavor = flavor;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { flavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor f) {
            return flavor.equals(f);
        }

        @Override
        public Object getTransferData(DataFlavor f)
                throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(f)) {
                throw new UnsupportedFlavorException(f);
            }
            return row;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(FunctionTableRowTransferHandler.class);
}
