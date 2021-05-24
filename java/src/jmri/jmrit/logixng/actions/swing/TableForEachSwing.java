package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.TableForEach;
import jmri.jmrit.logixng.actions.TableForEach.TableRowOrColumn;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an TableForEach object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class TableForEachSwing extends AbstractDigitalActionSwing {

    private BeanSelectPanel<NamedTable> tableBeanPanel;
    private JComboBox<TableRowOrColumn> _tableRowOrColumnComboBox;
    private JTextField _rowOrColumnName;
    private JTextField _localVariable;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof TableForEach)) {
            throw new IllegalArgumentException("object must be an TableForEach but is a: "+object.getClass().getName());
        }
        
        TableForEach action = (TableForEach)object;
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        _tableRowOrColumnComboBox = new JComboBox<>();
        for (TableForEach.TableRowOrColumn item : TableForEach.TableRowOrColumn.values()) {
            _tableRowOrColumnComboBox.addItem(item);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_tableRowOrColumnComboBox);
        
        JPanel tableRowOrColumnPanel = new JPanel();
        tableRowOrColumnPanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_RowOrColumn")));
        tableRowOrColumnPanel.add(_tableRowOrColumnComboBox);
        panel.add(tableRowOrColumnPanel);
        
        JPanel tableBeanPanelPanel = new JPanel();
        tableBeanPanelPanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_Table")));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel panelTurnout = new javax.swing.JPanel();
        JPanel panelReference = new javax.swing.JPanel();
        JPanel panelFormula = new javax.swing.JPanel();
        
        tabbedPane.addTab("Table", panelTurnout); // NOI1aa8N
        tabbedPane.addTab("Reference", panelReference); // NOIaa18N
        tabbedPane.addTab("Formula", panelFormula); // NOI1aa8N
        
        tableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);
        panelTurnout.add(tableBeanPanel);
        
        JTextField referenceTextField = new JTextField();
        referenceTextField.setColumns(30);
        panelReference.add(referenceTextField);
        
        JTextField formulaTextField = new JTextField();
        formulaTextField.setColumns(30);
        panelFormula.add(formulaTextField);
        
        tableBeanPanelPanel.add(tabbedPane);
        panel.add(tableBeanPanelPanel);
        
        JPanel rowOrColumnNamePanel = new JPanel();
        rowOrColumnNamePanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_RowOrColumnName")));
        _rowOrColumnName = new JTextField(20);
        rowOrColumnNamePanel.add(_rowOrColumnName);
        panel.add(rowOrColumnNamePanel);
        
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
            _rowOrColumnName.setText(action.getRowOrColumnName());
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
        action.setTableRowOrColumn((TableForEach.TableRowOrColumn)_tableRowOrColumnComboBox.getSelectedItem());
        action.setRowOrColumnName(_rowOrColumnName.getText());
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
