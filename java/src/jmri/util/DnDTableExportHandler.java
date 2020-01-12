package jmri.util;

/**
 * Simple TransferHandler that exports a string value of a cell in a JTable.
 *
 * @author Pete Cressman Copyright 2010
 */
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnDTableExportHandler extends TransferHandler {

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    @Override
    public Transferable createTransferable(JComponent c) {
        JTable table = (JTable) c;
        int col = table.getSelectedColumn();
        int row = table.getSelectedRow();
        if (col < 0 || row < 0) {
            return null;
        }
        row = table.convertRowIndexToModel(row);
        col = table.convertColumnIndexToModel(col);
        if (log.isDebugEnabled()) {
            log.debug("TransferHandler.createTransferable: from ("
                    + row + ", " + col + ") for \""
                    + table.getModel().getValueAt(row, col) + "\"");
        }
        Object obj = table.getModel().getValueAt(row, col);
        if (obj instanceof String) {
            return new StringSelection((String) obj);
        } else if (obj != null) {
            return new StringSelection(obj.getClass().getName());
        } else {
            return null;
        }
    }

    @Override
    public void exportDone(JComponent c, Transferable t, int action) {
        if (log.isDebugEnabled()) {
            log.debug("TransferHandler.exportDone ");
        }
    }
    private final static Logger log = LoggerFactory.getLogger(DnDTableExportHandler.class);
}
