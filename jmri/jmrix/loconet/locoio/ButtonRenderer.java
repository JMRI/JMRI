package jmri.jmrix.loconet.locoio;

// This was adapted from Core Swing Advanced Programming, Prentice Hall

// Changes: Remove DataWithIcon reference.  Change package.

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

// A holder for data and an associated icon
public class ButtonRenderer extends JButton
    implements TableCellRenderer {

    public ButtonRenderer() {
        this.border = getBorder();
        this.setOpaque(true);
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
        super.setForeground(foreground);
    }

    public void setBackground(Color background) {
        this.background = background;
        super.setBackground(background);
    }

    public void setFont(Font font) {
        this.font = font;
        super.setFont(font);
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int column) {
        Color cellForeground = foreground != null ? foreground
            : table.getForeground();
        Color cellBackground = background != null ? background
            : table.getBackground();

        setFont(font != null ? font : table.getFont());

        if (hasFocus) {
            setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            if (table.isCellEditable(row, column)) {
                cellForeground = UIManager.getColor("Table.focusCellForeground");
                cellBackground = UIManager.getColor("Table.focusCellBackground");
            }
        } else {
            setBorder(border);
        }

        super.setForeground(cellForeground);
        super.setBackground(cellBackground);

        // Customize the component's appearance
        setValue(value);

        return this;
    }

    protected void setValue(Object value) {
        if (value == null) {
            setText("");
            setIcon(null);
        } else if (value instanceof Icon) {
            setText("");
            setIcon((Icon)value);
        } else {
            setText(value.toString());
            setIcon(null);
        }
    }

    protected Color foreground;
    protected Color background;
    protected Font font;
    protected Border border;
}
