package jmri.jmrit.logixng.actions.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.TableColumn;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ShowDialog;
import jmri.jmrit.logixng.actions.ShowDialog.Button;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Configures an ShowDialog object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ShowDialogSwing extends AbstractDigitalActionSwing {

    private List<ButtonCheckBox> _buttonCheckBoxes;
    private JComboBox<ShowDialog.FormatType> _formatType;
    private JTextField _format;
    private JTable _showDialogTable;
    private ShowDialogTableModel _showDialogTableModel;
    private JCheckBox _modalCheckBox;
    private JCheckBox _multiLineCheckBox;
    private JTextField _localVariable;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && (! (object instanceof ShowDialog))) {
            throw new IllegalArgumentException("object is not a ShowDialog: " + object.getClass().getName());
        }
        ShowDialog showDialog = (ShowDialog)object;
        
        panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        
        JPanel buttonCheckBoxPanel = new JPanel();
        buttonCheckBoxPanel.setBorder(BorderFactory.createTitledBorder(
                Bundle.getMessage("ShowDialog_Buttons")));
        _buttonCheckBoxes = new ArrayList<>();
        for (Button button : ShowDialog.Button.values()) {
            ButtonCheckBox buttonCheckBox =
                    new ButtonCheckBox(button,
                            new JCheckBox(Integer.toString(button.getValue())
                                    + ": " + button.toString()));
            _buttonCheckBoxes.add(buttonCheckBox);
            buttonCheckBoxPanel.add(buttonCheckBox._checkBox);
            if ((showDialog != null) && (showDialog.getEnabledButtons().contains(button))) {
                buttonCheckBox._checkBox.setSelected(true);
            }
        }
        panel.add(buttonCheckBoxPanel);
        
        
        JPanel formatTypePanel = new JPanel();
        _formatType = new JComboBox<>();
        for (ShowDialog.FormatType formatType : ShowDialog.FormatType.values()) {
            _formatType.addItem(formatType);
        }
        formatTypePanel.add(new JLabel(Bundle.getMessage("ShowDialog_FormatType")));
        formatTypePanel.add(_formatType);
        panel.add(formatTypePanel);
        
        JPanel formatPanel = new JPanel();
        _format = new JTextField(40);
        formatPanel.add(new JLabel(Bundle.getMessage("ShowDialog_Format")));
        formatPanel.add(_format);
        panel.add(formatPanel);
        
        
        JPanel tablePanel = new JPanel();
        _showDialogTable = new JTable();
        
        if (showDialog != null) {
            List<ShowDialog.Data> dataList
                    = new ArrayList<>(showDialog.getDataList());

            _showDialogTableModel = new ShowDialogTableModel(dataList);
        } else {
            _showDialogTableModel = new ShowDialogTableModel(null);
        }
        
        _showDialogTable.setModel(_showDialogTableModel);
        _showDialogTable.setDefaultRenderer(ShowDialog.DataType.class,
                new ShowDialogTableModel.CellRenderer());
        _showDialogTable.setDefaultEditor(ShowDialog.DataType.class,
                new ShowDialogTableModel.DataTypeCellEditor());
        _showDialogTableModel.setColumnsForComboBoxes(_showDialogTable);
        _showDialogTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        _showDialogTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        
        JButton testButton = new JButton("XXXXXX");  // NOI18N
        _showDialogTable.setRowHeight(testButton.getPreferredSize().height);
        TableColumn deleteColumn = _showDialogTable.getColumnModel()
                .getColumn(ShowDialogTableModel.COLUMN_DUMMY);
        deleteColumn.setMinWidth(testButton.getPreferredSize().width);
        deleteColumn.setMaxWidth(testButton.getPreferredSize().width);
        deleteColumn.setResizable(false);
        
        // The dummy column is used to be able to force update of the
        // other columns when the panel is closed.
        TableColumn dummyColumn = _showDialogTable.getColumnModel()
                .getColumn(ShowDialogTableModel.COLUMN_DUMMY);
        dummyColumn.setMinWidth(0);
        dummyColumn.setPreferredWidth(0);
        dummyColumn.setMaxWidth(0);
        
        JScrollPane scrollpane = new JScrollPane(_showDialogTable);
        scrollpane.setPreferredSize(new Dimension(400, 200));
        tablePanel.add(scrollpane, BorderLayout.CENTER);
        panel.add(tablePanel);
        
        // Add parameter
        JButton add = new JButton(Bundle.getMessage("ShowDialog_TableAdd"));
        buttonPanel.add(add);
        add.addActionListener((ActionEvent e) -> {
            _showDialogTableModel.add();
        });
        
        
        _modalCheckBox = new JCheckBox(Bundle.getMessage("ShowDialog_Modal"));
        panel.add(_modalCheckBox);
        
        _multiLineCheckBox = new JCheckBox(Bundle.getMessage("ShowDialog_MultiLine"));
        panel.add(_multiLineCheckBox);
        
        panel.add(new JLabel(Bundle.getMessage("ShowDialog_MultiLineHelp")));
        
        
        JPanel localVariablePanel = new JPanel();
        _localVariable = new JTextField(20);
        localVariablePanel.add(new JLabel(Bundle.getMessage("ShowDialog_LocalVariable")));
        localVariablePanel.add(_localVariable);
        panel.add(localVariablePanel);
        
        
        if (showDialog != null) {
            _modalCheckBox.setSelected(showDialog.getModal());
            _multiLineCheckBox.setSelected(showDialog.getMultiLine());
            _localVariable.setText(showDialog.getLocalVariable());
            _formatType.setSelectedItem(showDialog.getFormatType());
            _format.setText(showDialog.getFormat());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        boolean result = true;
        boolean hasEnabledButton = false;
        for (ButtonCheckBox buttonCheckBox : _buttonCheckBoxes) {
            hasEnabledButton |= buttonCheckBox._checkBox.isSelected();
        }
        if (!hasEnabledButton) {
            errorMessages.add(Bundle.getMessage("ShowDialog_ErrorNoEnabledButton"));
            result = false;
        }
        
        for (ShowDialog.Data data : _showDialogTableModel.getDataList()) {
            if (data.getDataType() == ShowDialog.DataType.Formula) {
                try {
                    Map<String, Variable> variables = new HashMap<>();
                    RecursiveDescentParser parser = new RecursiveDescentParser(variables);
                    parser.parseExpression(data.getData());
                } catch (ParserException e) {
                    errorMessages.add(e.getLocalizedMessage());
                    result = false;
                }
            }
        }
        return result;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ShowDialog action = new ShowDialog(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ShowDialog)) {
            throw new IllegalArgumentException("object is not a ShowDialog: " + object.getClass().getName());
        }
        ShowDialog showDialog = (ShowDialog)object;
        
        
        Set<Button> enabledButtons = showDialog.getEnabledButtons();
        enabledButtons.clear();
        
        for (ButtonCheckBox buttonCheckBox : _buttonCheckBoxes) {
            if (buttonCheckBox._checkBox.isSelected()) {
                enabledButtons.add(buttonCheckBox._button);
            }
        }
        
        
        showDialog.setLocalVariable(_localVariable.getText());
        
        showDialog.setFormatType(_formatType.getItemAt(_formatType.getSelectedIndex()));
        showDialog.setFormat(_format.getText());
        
        
        showDialog.setModal(_modalCheckBox.isSelected());
        showDialog.setMultiLine(_multiLineCheckBox.isSelected());
        
        showDialog.setFormatType(_formatType.getItemAt(_formatType.getSelectedIndex()));
        showDialog.setFormat(_format.getText());
        
        
        // Do this to force update of the table
        _showDialogTable.editCellAt(0, 2);
        
        showDialog.getDataList().clear();
        
        for (ShowDialog.Data data : _showDialogTableModel.getDataList()) {
            showDialog.getDataList().add(data);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ShowDialog_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private static class ButtonCheckBox {
        
        private final Button _button;
        private final JCheckBox _checkBox;
        
        private ButtonCheckBox(Button button, JCheckBox checkBox) {
            this._button = button;
            this._checkBox = checkBox;
        }
        
    }
    
}
