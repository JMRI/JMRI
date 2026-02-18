package jmri.util.swing;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;
import javax.swing.UIManager;

/**
 * Renderer to display multiple lines in a JTable cell
 *
 * @see jmri.util.swing.MultiLineCellEditor
 */
public class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

    public MultiLineCellRenderer() {
    
        // match these to jmri.util.swing.MultiLineCellEditor
        setLineWrap(false);
        setWrapStyleWord(true);
        setOpaque(true);
        customize();
    }

    /**
     * Allow the creator of one of these objects to customize its
     * appearance, tooltips, etc
     */
    protected void customize() {}
    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setFont(table.getFont());
        if (hasFocus) {
            if (table.isCellEditable(row, column)) {
                setForeground(UIManager.getColor("Table.focusCellForeground"));
                setBackground(UIManager.getColor("Table.focusCellBackground"));
            }
        }
        setText((value == null) ? "" : value.toString());
        return this;
    }
}
