package jmri.util.swing;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

/**
 * Renderer to edit multiple lines in a JTable cell
 *
 * @see jmri.util.swing.MultiLineCellRenderer
 */
public class MultiLineCellEditor  extends AbstractCellEditor implements TableCellEditor {
        
    public MultiLineCellEditor() {
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(true);
    }

    JTextArea textArea;
    
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        
        // formatting
        if (isSelected) {
            textArea.setForeground(table.getSelectionForeground());
            textArea.setBackground(table.getSelectionBackground());
        } else {
            textArea.setForeground(table.getForeground());
            textArea.setBackground(table.getBackground());
        }
        textArea.setFont(table.getFont());
        
        // handle Enter/Return key, which closes the
        // editor unless a modifier is down

        // Get the InputMap for WHEN_FOCUSED
        InputMap inputMap = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        // Get the ActionMap
        ActionMap actionMap = textArea.getActionMap();
        // Define a unique name for your custom action
        String enterActionKey = "processEnterKey";
        String enterModifiedActionKey = "processModifiedEnterKey";
        // Define the KeyStroke for the Enter key with and without modifiers
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke ctrlEnterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK);
        KeyStroke altEnterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK);
        KeyStroke shiftEnterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
        // Create custom Actions to handle these
        Action customEnterAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Done editing
                stopCellEditing();
            }
        };
        Action customModifiedEnterAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Insert a return
                // Get the current cursor (caret) position
                int caretPosition = textArea.getCaretPosition();
                // Insert the new text at that position
                textArea.insert("\n", caretPosition);
                // Move the cursor to the end of the newly inserted text
                textArea.setCaretPosition(caretPosition + 1);

                var model = table.getModel();
                if (model instanceof ResizableRowDataModel) {
                
                    ((ResizableRowDataModel)model).resizeRowToText(row, textArea.getText().split("\n").length);
                    
                    table.revalidate();
                    table.repaint();
                }
            }
        };
        // Bind the KeyStroke to the action name in the InputMap
        inputMap.put(enterKeyStroke, enterActionKey);
        inputMap.put(ctrlEnterKeyStroke, enterModifiedActionKey);
        inputMap.put(altEnterKeyStroke, enterModifiedActionKey);
        inputMap.put(shiftEnterKeyStroke, enterModifiedActionKey);
        // Bind the action name to the custom Action in the ActionMap
        actionMap.put(enterActionKey, customEnterAction);
        actionMap.put(enterModifiedActionKey, customModifiedEnterAction);
        
        // set value and return the text area for display
        textArea.setText((String)value);
        return textArea;
    }
  
    public Object getCellEditorValue() {
        return textArea.getText();
    }
}
