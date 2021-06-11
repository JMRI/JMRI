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
import jmri.jmrit.logixng.actions.LogData;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Configures an LogData object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class LogDataSwing extends AbstractDigitalActionSwing {

    private JCheckBox _logToLogCheckBox;
    private JCheckBox _logToScriptOutputCheckBox;
    private JComboBox<LogData.FormatType> _formatType;
    private JTextField _format;
    private JTable _logDataTable;
    private LogDataTableModel _logDataTableModel;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && (! (object instanceof LogData))) {
            throw new IllegalArgumentException("object is not a LogData: " + object.getClass().getName());
        }
        LogData logData = (LogData)object;
        
        panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        
        _logToLogCheckBox = new JCheckBox(Bundle.getMessage("LogData_LogToLog"));
        panel.add(_logToLogCheckBox);
        
        _logToScriptOutputCheckBox = new JCheckBox(Bundle.getMessage("LogData_LogToScriptOutput"));
        panel.add(_logToScriptOutputCheckBox);
        
        
        JPanel formatTypePanel = new JPanel();
        _formatType = new JComboBox<>();
        for (LogData.FormatType formatType : LogData.FormatType.values()) {
            _formatType.addItem(formatType);
        }
        formatTypePanel.add(new JLabel(Bundle.getMessage("LogData_FormatType")));
        formatTypePanel.add(_formatType);
        panel.add(formatTypePanel);
        
        JPanel formatPanel = new JPanel();
        _format = new JTextField(20);
        formatPanel.add(new JLabel(Bundle.getMessage("LogData_Format")));
        formatPanel.add(_format);
        panel.add(formatPanel);
        
        
        if (logData != null) {
            _logToLogCheckBox.setSelected(logData.getLogToLog());
            _logToScriptOutputCheckBox.setSelected(logData.getLogToScriptOutput());
            _formatType.setSelectedItem(logData.getFormatType());
            _format.setText(logData.getFormat());
        }
        
        
        JPanel tablePanel = new JPanel();
        _logDataTable = new JTable();
        
        if (logData != null) {
            List<LogData.Data> dataList
                    = new ArrayList<>(logData.getDataList());

            _logDataTableModel = new LogDataTableModel(dataList);
        } else {
            _logDataTableModel = new LogDataTableModel(null);
        }
        
        _logDataTable.setModel(_logDataTableModel);
        _logDataTable.setDefaultRenderer(LogData.DataType.class,
                new LogDataTableModel.CellRenderer());
        _logDataTable.setDefaultEditor(LogData.DataType.class,
                new LogDataTableModel.DataTypeCellEditor());
        _logDataTableModel.setColumnsForComboBoxes(_logDataTable);
        _logDataTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        _logDataTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        
        JButton testButton = new JButton("XXXXXX");  // NOI18N
        _logDataTable.setRowHeight(testButton.getPreferredSize().height);
        TableColumn deleteColumn = _logDataTable.getColumnModel()
                .getColumn(LogDataTableModel.COLUMN_DUMMY);
        deleteColumn.setMinWidth(testButton.getPreferredSize().width);
        deleteColumn.setMaxWidth(testButton.getPreferredSize().width);
        deleteColumn.setResizable(false);
        
        // The dummy column is used to be able to force update of the
        // other columns when the panel is closed.
        TableColumn dummyColumn = _logDataTable.getColumnModel()
                .getColumn(LogDataTableModel.COLUMN_DUMMY);
        dummyColumn.setMinWidth(0);
        dummyColumn.setPreferredWidth(0);
        dummyColumn.setMaxWidth(0);
        
        JScrollPane scrollpane = new JScrollPane(_logDataTable);
        scrollpane.setPreferredSize(new Dimension(400, 200));
        tablePanel.add(scrollpane, BorderLayout.CENTER);
        panel.add(tablePanel);
        
        // Add parameter
        JButton add = new JButton(Bundle.getMessage("LogData_TableAdd"));
        buttonPanel.add(add);
        add.addActionListener((ActionEvent e) -> {
            _logDataTableModel.add();
        });
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        for (LogData.Data data : _logDataTableModel.getDataList()) {
            if (data.getDataType() == LogData.DataType.Formula) {
                try {
                    Map<String, Variable> variables = new HashMap<>();
                    RecursiveDescentParser parser = new RecursiveDescentParser(variables);
                    parser.parseExpression(data.getData());
                } catch (ParserException e) {
                    errorMessages.add(e.getLocalizedMessage());
                }
            }
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        LogData action = new LogData(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof LogData)) {
            throw new IllegalArgumentException("object is not a LogData: " + object.getClass().getName());
        }
        LogData logData = (LogData)object;
        
        
        logData.setLogToLog(_logToLogCheckBox.isSelected());
        logData.setLogToScriptOutput(_logToScriptOutputCheckBox.isSelected());
        
        logData.setFormatType(_formatType.getItemAt(_formatType.getSelectedIndex()));
        logData.setFormat(_format.getText());
        
        
        // Do this to force update of the table
        _logDataTable.editCellAt(0, 2);
        
        logData.getDataList().clear();
        
        for (LogData.Data data : _logDataTableModel.getDataList()) {
            logData.getDataList().add(data);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("LogData_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
