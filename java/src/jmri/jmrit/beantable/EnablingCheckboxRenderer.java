package jmri.jmrit.beantable;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Handle painting checkbox classes in a JTable.
 * <p>
 * Beyond the normal behavior of providing a checkbox to show the value, this
 * disables the JCheckBox if the cell is not editable. This makes the visual
 * behavior more in line with user expectations.
 * The checkbox cell background is set to match selected status.
 *
 * @author Bob Jacobsen
 */
public class EnablingCheckboxRenderer extends JCheckBox implements TableCellRenderer {

    public EnablingCheckboxRenderer() {
        super();
        super.setHorizontalAlignment(0);
    }

    /**
     * Override this method from the parent class.
     * {@inheritDoc}
     *
     * @param table      the JTable component
     * @param value      the cell content's object
     * @param isSelected boolean so we know if this is the currently selected
     *                   row
     * @param hasFocus   does this cell currently have focus?
     * @param row        the row number
     * @param column     the column number
     * @return the JCheckBox to display
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, java.lang.Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setSelected(value != null && ((Boolean) value));
        setEnabled(table.isCellEditable(row, column));
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground() );
        return this;
    }
}
