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
 * <p>
 * Works with {@link ButtonRenderer}.
 * <p>
 * This was adapted from Core Swing Advanced Programming, Prentice Hall
 * <p>
 * Changes: Remove DataWithIcon reference. Change package
 * <p>
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

    @Override
    public Object getCellEditorValue() {
        return value;
    }

    @Override
    public void editingStarted(EventObject event) {
        // Edit starting - click the button if necessary
        if (!(event instanceof MouseEvent)) {
            // Keyboard event - click the button
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ((JButton) editor).doClick();
                }
            });
        }
    }

    @Override
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

    @Override
    public void actionPerformed(ActionEvent evt) {
        // Button pressed - stop the edit
        stopCellEditing();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    protected Object value;
    protected Color foreground;
    protected Color background;
    protected Font font;
}
