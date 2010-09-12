package jmri.jmrit.display.palette;

import java.awt.datatransfer.Transferable; 
import javax.swing.TransferHandler;
import javax.swing.JTable;

import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.picker.PickListModel;

import jmri.NamedBean;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.ReporterIcon;

public class ReporterItemPanel extends TableItemPanel {

    public ReporterItemPanel(ItemPalette parentFrame, String  itemType, PickListModel model, Editor editor) {
        super(parentFrame, itemType, model, editor);
    }

    public void init() {
        initTablePanel(_model, _editor);        // NORTH Panel
        _table.setTransferHandler(new ReporterDnD(_editor));
    }

    /**
    * Extend handler to export from JList and import to PicklistTable
    */
    protected class ReporterDnD extends DnDTableItemHandler {

        ReporterDnD(Editor editor) {
            super(editor);
        }

        public Transferable createPositionableDnD(JTable table) {
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            if (log.isDebugEnabled()) log.debug("TransferHandler.createTransferable: from table \""+_itemType+ "\" at ("
                                                +row+", "+col+") for data \""
                                                +table.getModel().getValueAt(row, col)+"\"");
            if (col<0 || row<0) {
                return null;
            }            
            PickListModel model = (PickListModel)table.getModel();
            NamedBean bean = model.getBeanAt(row);

            ReporterIcon r = new ReporterIcon(_editor);
            r.setReporter(bean.getDisplayName());
            r.setDisplayLevel(Editor.REPORTERS);
            return new PositionableDnD(r, bean.getDisplayName());
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReporterItemPanel.class.getName());
}
