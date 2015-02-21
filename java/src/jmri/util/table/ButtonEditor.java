package jmri.util.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

/**
 * Make a JButton in a table cell function.
 * <P>
 * Works with {@link ButtonRenderer}.
 * <P>
 * This was adapted from Core Swing Advanced Programming, Prentice Hall
 * <P>
 * Changes: Remove DataWithIcon reference. Change package
 * <P>
 * This also now implements and registers as a MouseListener, so you can change
 * the mouse-event behavior by overriding the needed methods.
 */
public class ButtonEditor extends BasicCellEditor
        implements ActionListener,
        TableCellEditor,
        MouseListener {

    public ButtonEditor(JButton button) {
        super(button);
        button.addActionListener(this);
        button.addMouseListener(this);
        button.putClientProperty("JComponent.sizeVariant", "small");
        button.putClientProperty("JButton.buttonType", "square");
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
        editor.setForeground(foreground);
    }

    public void setBackground(Color background) {
        this.background = background;
        editor.setBackground(background);
    }

    public void setFont(Font font) {
        this.font = font;
        editor.setFont(font);
    }

    public Object getCellEditorValue() {
        return value;
    }

    public void editingStarted(EventObject event) {
        // Edit starting - click the button if necessary
        if (!(event instanceof MouseEvent)) {
            // Keyboard event - click the button
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ((JButton) editor).doClick();
                }
            });
        }
    }

    public Component getTableCellEditorComponent(JTable tbl,
            Object value, boolean isSelected,
            int row, int column) {
        editor.setForeground(foreground != null ? foreground
                : tbl.getForeground());
        editor.setBackground(background != null ? background
                : tbl.getBackground());
        editor.setFont(font != null ? font : tbl.getFont());

        this.value = value;
        setValue(value);
        return editor;
    }

    protected void setValue(Object value) {
        JButton button = (JButton) editor;
        if (value == null) {
            button.setText("");
            button.setIcon(null);
            button.setEnabled(false);
        } else if (value instanceof Icon) {
            button.setText("");
            button.setIcon((Icon) value);
            button.setEnabled(true);
        } else {
            button.setText(value.toString());
            button.setIcon(null);
            button.setEnabled(true);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        // Button pressed - stop the edit
        stopCellEditing();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    protected Object value;
    protected Color foreground;
    protected Color background;
    protected Font font;
}
