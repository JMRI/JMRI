package jmri.jmrit.symbolicprog;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Specialization of ValueRenderer to add CV-usage tooltips and handle Integer values
 *
 * @author Bob Jacobsen Copyright (C) 2023
 */
public class CvValueRenderer extends ValueRenderer {

    public CvValueRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        // if (log.isDebugEnabled()) log.debug("getTableCellRendererComponent "
        //       +" "+row+" "+column
        //       +" "+isSelected+" "+hasFocus
        //       +" "+value);

        JComponent retval;

        if (value instanceof Integer) {
            retval = new JLabel(value.toString());
        } else {
            retval = (JComponent) super.getTableCellRendererComponent(table, value,
                                                                      isSelected, hasFocus,
                                                                      row, column);
        }

        // get the CV number
        var model = (CvTableModel)table.getModel();
        int modelRow = table.convertRowIndexToModel(row);
        var cvNum = model.getValueAt(modelRow, CvTableModel.NUMCOLUMN).toString();
        var nameSet = model.getCvToVariableMapping(cvNum);
        if (nameSet != null ) {
            var building = new StringBuilder();
            boolean first = true;
            for(String item : nameSet){
                if (! first) building.append("; ");
                first = false;
                building.append(item);
            }
            retval.setToolTipText(building.toString());
        }
        return retval;
    }
}
