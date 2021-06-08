package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.TableForEach;
import jmri.jmrit.logixng.TableRowOrColumn;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an TableForEach object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class TableForEachSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPane;
    private JPanel _panelTable = new javax.swing.JPanel();
    private JPanel _panelReference = new javax.swing.JPanel();
    private JPanel _panelFormula = new javax.swing.JPanel();
    private BeanSelectPanel<NamedTable> tableBeanPanel;
    private JComboBox<TableRowOrColumn> _tableRowOrColumnComboBox;
    private JComboBox<String> _rowOrColumnNameComboBox;
    private JTextField _rowOrColumnNameTextField;
    private JTextField _localVariable;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof TableForEach)) {
            throw new IllegalArgumentException("object must be an TableForEach but is a: "+object.getClass().getName());
        }
        
        TableForEach action = (TableForEach)object;
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JPanel tableBeanPanelPanel = new JPanel();
        tableBeanPanelPanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_Table")));
        
        _tabbedPane = new JTabbedPane();
        _panelTable = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        
        _tabbedPane.addTab("Table", _panelTable); // NOI1aa8N
        _tabbedPane.addTab("Reference", _panelReference); // NOIaa18N
        _tabbedPane.addTab("Formula", _panelFormula); // NOI1aa8N
        
        _tabbedPane.addChangeListener((evt) -> {
            boolean isPanelTable = (_tabbedPane.getSelectedComponent() == _panelTable);
            _rowOrColumnNameComboBox.setVisible(isPanelTable);
            _rowOrColumnNameTextField.setVisible(!isPanelTable);
        });
        
        tableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);
        _panelTable.add(tableBeanPanel);
        
        JTextField referenceTextField = new JTextField();
        referenceTextField.setColumns(30);
        _panelReference.add(referenceTextField);
        
        JTextField formulaTextField = new JTextField();
        formulaTextField.setColumns(30);
        _panelFormula.add(formulaTextField);
        
        tableBeanPanelPanel.add(_tabbedPane);
        panel.add(tableBeanPanelPanel);
        
        _tableRowOrColumnComboBox = new JComboBox<>();
        for (TableRowOrColumn item : TableRowOrColumn.values()) {
            _tableRowOrColumnComboBox.addItem(item);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_tableRowOrColumnComboBox);
        _tableRowOrColumnComboBox.addActionListener((evt) -> {
            _rowOrColumnNameComboBox.removeAllItems();
            NamedTable table = tableBeanPanel.getNamedBean();
            if (table != null) {
                if (_tableRowOrColumnComboBox.getItemAt(_tableRowOrColumnComboBox.getSelectedIndex()) == TableRowOrColumn.Column) {
                    for (int column=0; column <= table.numColumns(); column++) {
                        // If the header is null or empty, treat the row as a comment
                        Object header = table.getCell(0, column);
                        if ((header != null) && (!header.toString().isEmpty())) {
                            _rowOrColumnNameComboBox.addItem(header.toString());
                        }
                    }
                } else {
                    for (int row=0; row <= table.numRows(); row++) {
                        // If the header is null or empty, treat the row as a comment
                        Object header = table.getCell(row, 0);
                        if ((header != null) && (!header.toString().isEmpty())) {
                            _rowOrColumnNameComboBox.addItem(header.toString());
                        }
                    }
                }
                if (action != null) {
                    _rowOrColumnNameComboBox.setSelectedItem(action.getRowOrColumnName());
                }
            }
        });
        
        JPanel tableRowOrColumnPanel = new JPanel();
        tableRowOrColumnPanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_RowOrColumn")));
        tableRowOrColumnPanel.add(_tableRowOrColumnComboBox);
        panel.add(tableRowOrColumnPanel);
        
        JPanel rowOrColumnNamePanel = new JPanel();
        rowOrColumnNamePanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_RowOrColumnName")));
        _rowOrColumnNameComboBox = new JComboBox<>();
        rowOrColumnNamePanel.add(_rowOrColumnNameComboBox);
        _rowOrColumnNameTextField = new JTextField(20);
        _rowOrColumnNameTextField.setVisible(false);
        rowOrColumnNamePanel.add(_rowOrColumnNameTextField);
        panel.add(rowOrColumnNamePanel);
        JComboBoxUtil.setupComboBoxMaxRows(_rowOrColumnNameComboBox);
        
        JPanel localVariablePanel = new JPanel();
        localVariablePanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_LocalVariable")));
        _localVariable = new JTextField(20);
        localVariablePanel.add(_localVariable);
        panel.add(localVariablePanel);
        
        if (action != null) {
            if (action.getTable() != null) {
                tableBeanPanel.setDefaultNamedBean(action.getTable().getBean());
            }
            _tableRowOrColumnComboBox.setSelectedItem(action.getTableRowOrColumn());
            _localVariable.setText(action.getLocalVariableName());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        TableForEach action = new TableForEach(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof TableForEach)) {
            throw new IllegalArgumentException("object must be an TableForEach but is a: "+object.getClass().getName());
        }
        
        // Create a temporary action in case we don't have one.
        TableForEach action = (TableForEach)object;
        NamedTable table = tableBeanPanel.getNamedBean();
        if (table != null) {
            NamedBeanHandle<NamedTable> handle
                    = InstanceManager.getDefault(NamedBeanHandleManager.class)
                            .getNamedBeanHandle(table.getDisplayName(), table);
            action.setTable(handle);
        } else {
            action.removeTable();
        }
        action.setTableRowOrColumn(_tableRowOrColumnComboBox.getItemAt(_tableRowOrColumnComboBox.getSelectedIndex()));
        if (_tabbedPane.getSelectedComponent() == _panelTable) {
            if (_rowOrColumnNameComboBox.getSelectedIndex() != -1) {
                action.setRowOrColumnName(_rowOrColumnNameComboBox.getItemAt(_rowOrColumnNameComboBox.getSelectedIndex()));
            } else {
                action.setRowOrColumnName("");
            }
        } else {
            action.setRowOrColumnName(_rowOrColumnNameTextField.getText());
        }
        action.setLocalVariableName(_localVariable.getText());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("TableForEach_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TableForEachSwing.class);
    
}
