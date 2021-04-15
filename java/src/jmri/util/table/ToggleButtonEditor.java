package jmri.util.table;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

/**
 * Make a JToggleButton in a table cell function. Used eg. in OBlock tables for units
 * <p>
 * Works with {@link ToggleButtonRenderer}.
 * <p>
 * Adapted from {@link ButtonRenderer}
 * <p>
 * This also now implements and registers as a MouseListener, so you can change
 * the mouse-event behavior by overriding the needed methods.
 * @author Egbert Broerse 2020
 */
public class ToggleButtonEditor extends BasicCellEditor
        implements ActionListener,
        TableCellEditor,
        MouseListener {

    public ToggleButtonEditor(JToggleButton button, String on, String off) {
        super(button);
        onText = on;
        offText = off;
        button.addActionListener(this);
        button.addMouseListener(this);
        //button.addItemListener(new ItemListener() {
        //            public void itemStateChanged(ItemEvent ev) {
        //                if (ev.getStateChange() == ItemEvent.SELECTED) {
        //                    System.out.println("button is selected");
        //                } else if (ev.getStateChange() == ItemEvent.DESELECTED) {
        //                    System.out.println("button is not selected");
        //                }
        //            }
        //});
        button.putClientProperty("JComponent.sizeVariant", "small");
        button.putClientProperty("JToggleButton.buttonType", "square");
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
                    ((JToggleButton) editor).doClick();
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
        setValue(value, isSelected);
        return editor;
    }

    protected void setValue(Object value, Boolean sel) {
        JToggleButton button = (JToggleButton) editor;
            switch (value.toString()) {
                case "true":
                    button.setText(onText);
//                    button.setSelected(sel);
                    button.setEnabled(true);
                    break;
                case "false":
                    button.setText(offText);
//                    button.setSelected(sel);
                    button.setEnabled(true);
                    break;
                default:
                    button.setText("?");
                    button.setEnabled(false);
            }
        }

    @Override
    public void actionPerformed(ActionEvent evt) {
        JToggleButton tBtn = (JToggleButton)evt.getSource();
        if (tBtn.isSelected()) {
            tBtn.setText(onText);
            value = Boolean.TRUE;
        } else {
            tBtn.setText(offText);
            value = Boolean.FALSE;
        }
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

    protected String onText;
    protected String offText;
    protected Object value;
    protected Color foreground;
    protected Color background;
    protected Font font;
}
