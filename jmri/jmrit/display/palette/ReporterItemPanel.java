package jmri.jmrit.display.palette;

import java.awt.datatransfer.Transferable; 
import javax.swing.JTable;

import jmri.jmrit.display.Editor;
import jmri.jmrit.picker.PickListModel;

import jmri.NamedBean;
import jmri.util.JmriJFrame;
import jmri.jmrit.display.ReporterIcon;

public class ReporterItemPanel extends TableItemPanel {

    public ReporterItemPanel(JmriJFrame parentFrame, String  type, String family, PickListModel model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    public void init() {
        add(initTablePanel(_model, _editor));        // NORTH Panel
        _table.setTransferHandler(new ReporterDnD(_editor));
    }

    /**
    * Extend handler to export from PicklistTable(JTable and import to panel
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
            r.setLevel(Editor.REPORTERS);
            return new PositionableDnD(r, bean.getDisplayName());
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReporterItemPanel.class.getName());
}
