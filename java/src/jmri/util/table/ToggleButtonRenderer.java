package jmri.util.table;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Works with {@link ToggleButtonRenderer}.
 * <p>
 * Adapted from {@link ButtonRenderer}
 * @author Egbert Broerse 2020
*/

// A holder for data and and associated state display text strings
public class ToggleButtonRenderer extends JToggleButton
        implements TableCellRenderer {

    public ToggleButtonRenderer(String on, String off) {
        onText = on;
        offText = off;
        this.border = getBorder();
        this.setOpaque(true);
        putClientProperty("JComponent.sizeVariant", "small");
        putClientProperty("JToggleButton.buttonType", "square");
    }

    @Override
    public void setForeground(Color foreground) {
        this.foreground = foreground;
        super.setForeground(foreground);
    }

    @Override
    public void setBackground(Color background) {
        this.background = background;
        super.setBackground(background);
    }

    @Override
    public void setFont(Font font) {
        this.font = font;
        super.setFont(font);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected,
            boolean hasFocus,
            int row, int column) {
        Color cellForeground = foreground != null ? foreground
                : table.getForeground();
        Color cellBackground = background != null ? background
                : table.getBackground();

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
        setValue(value, isSelected);

        return this;
    }

    protected void setValue(Object value, Boolean sel) {
        switch (value.toString()) {
            case "true":
                setText(onText);
                setSelected(sel);
                setEnabled(true);
                break;
            case "false":
                setText(offText);
                setSelected(sel);
                setEnabled(true);
                break;
            default:
                setText("?");
                setEnabled(false);
        }
    }

    protected String onText;
    protected String offText;
    protected Color foreground;
    protected Color background;
    protected Font font;
    protected Border border;
}
