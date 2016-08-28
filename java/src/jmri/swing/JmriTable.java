package jmri.swing;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

/**
 * JTable configured to select all contents of editable cells when editing
 * starts.
 *
 * @author Randall Wood (C) 2016
 */
public class JmriTable extends JTable {

    public JmriTable(TableModel model) {
        super(model);
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        boolean editable = super.editCellAt(row, column, e);
        Component c = this.getEditorComponent();
        if (c instanceof JTextField) {
            ((JTextField) c).selectAll();
        }
        return editable;
    }

}
