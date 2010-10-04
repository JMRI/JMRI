package jmri.jmrit.display.palette;

import java.awt.datatransfer.Transferable; 
import javax.swing.JTable;

import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;

import jmri.NamedBean;
import jmri.jmrit.display.SignalMastIcon;

public class SignalMastItemPanel extends TableItemPanel {

    public SignalMastItemPanel(ItemPalette parentFrame, String  itemType, PickListModel model, Editor editor) {
        super(parentFrame, itemType, model, editor);
    }

    public void init() {
        initTablePanel(_model, _editor);        // NORTH Panel
    }

    protected void initTablePanel(PickListModel model, Editor editor) {
        super.initTablePanel(model, editor);
        _table.setTransferHandler(new SignalMastDnD(editor));
    }

    /**
    * Extend handler to export from JList and import to PicklistTable
    */
    protected class SignalMastDnD extends DnDTableItemHandler {

        SignalMastDnD(Editor editor) {
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

            SignalMastIcon sm  = new SignalMastIcon(_editor);
            sm.setSignalMast(bean.getDisplayName());
            sm.setDisplayLevel(Editor.SIGNALS);
            return new PositionableDnD(sm, bean.getDisplayName());
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalMastItemPanel.class.getName());
}
