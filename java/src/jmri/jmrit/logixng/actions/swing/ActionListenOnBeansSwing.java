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
import jmri.jmrit.logixng.actions.ActionListenOnBeans;
import jmri.jmrit.logixng.actions.NamedBeanType;
import jmri.jmrit.logixng.actions.ActionListenOnBeans.NamedBeanReference;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Configures an ActionListenOnBeans object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionListenOnBeansSwing extends AbstractDigitalActionSwing {

    private JTable _listenOnBeansTable;
    private ListenOnBeansTableModel _listenOnBeansTableModel;
    private JTextField _localVariableNamedBean;
    private JTextField _localVariableEvent;
    private JTextField _localVariableNewValue;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && (! (object instanceof ActionListenOnBeans))) {
            throw new IllegalArgumentException("object is not a ActionListenOnBeans: " + object.getClass().getName());
        }
        ActionListenOnBeans listenOnBeans = (ActionListenOnBeans)object;
        
        panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JPanel tablePanel = new JPanel();
        _listenOnBeansTable = new JTable();
        
        if (listenOnBeans != null) {
            List<NamedBeanReference> namedBeanReference
                    = new ArrayList<>(listenOnBeans.getReferences());

            namedBeanReference.sort((o1, o2) -> {
                int result = o1.getType().toString().compareTo(o2.getType().toString());
                if (result == 0) result = o1.getName().compareTo(o2.getName());
                return result;
            });
            _listenOnBeansTableModel = new ListenOnBeansTableModel(namedBeanReference);
        } else {
            _listenOnBeansTableModel = new ListenOnBeansTableModel(null);
        }
        
        _listenOnBeansTable.setModel(_listenOnBeansTableModel);
        _listenOnBeansTable.setDefaultRenderer(NamedBeanType.class,
                new ListenOnBeansTableModel.CellRenderer());
        _listenOnBeansTable.setDefaultEditor(NamedBeanType.class,
                new ListenOnBeansTableModel.NamedBeanTypeCellEditor());
        _listenOnBeansTable.setDefaultEditor(String.class,
                _listenOnBeansTableModel.getNamedBeanCellEditor());
        _listenOnBeansTableModel.setColumnsForComboBoxes(_listenOnBeansTable);
        _listenOnBeansTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        _listenOnBeansTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        
        JButton testButton = new JButton("XXXXXX");  // NOI18N
        _listenOnBeansTable.setRowHeight(testButton.getPreferredSize().height);
        TableColumn deleteColumn = _listenOnBeansTable.getColumnModel()
                .getColumn(ListenOnBeansTableModel.COLUMN_DUMMY);
        deleteColumn.setMinWidth(testButton.getPreferredSize().width);
        deleteColumn.setMaxWidth(testButton.getPreferredSize().width);
        deleteColumn.setResizable(false);
        
        // The dummy column is used to be able to force update of the
        // other columns when the panel is closed.
        TableColumn dummyColumn = _listenOnBeansTable.getColumnModel()
                .getColumn(ListenOnBeansTableModel.COLUMN_DUMMY);
        dummyColumn.setMinWidth(0);
        dummyColumn.setPreferredWidth(0);
        dummyColumn.setMaxWidth(0);
        
        JScrollPane scrollpane = new JScrollPane(_listenOnBeansTable);
        scrollpane.setPreferredSize(new Dimension(400, 200));
        tablePanel.add(scrollpane, BorderLayout.CENTER);
        panel.add(tablePanel);
        
        JPanel localVariableNamedBeanPanel = new JPanel();
        localVariableNamedBeanPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableNamedBean")));
        _localVariableNamedBean = new JTextField(20);
        localVariableNamedBeanPanel.add(_localVariableNamedBean);
        panel.add(localVariableNamedBeanPanel);
        
        JPanel localVariableNamedEventPanel = new JPanel();
        localVariableNamedEventPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableEvent")));
        _localVariableEvent = new JTextField(20);
        localVariableNamedEventPanel.add(_localVariableEvent);
        panel.add(localVariableNamedEventPanel);
        
        JPanel localVariableNewValuePanel = new JPanel();
        localVariableNewValuePanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableNewValue")));
        _localVariableNewValue = new JTextField(20);
        localVariableNewValuePanel.add(_localVariableNewValue);
        panel.add(localVariableNewValuePanel);
        
        if (listenOnBeans != null) {
            _localVariableNamedBean.setText(listenOnBeans.getLocalVariableNamedBean());
            _localVariableEvent.setText(listenOnBeans.getLocalVariableEvent());
            _localVariableNewValue.setText(listenOnBeans.getLocalVariableNewValue());
        }
        
        // Add parameter
        JButton add = new JButton(Bundle.getMessage("ActionListenOnBeans_TableAddReference"));
        buttonPanel.add(add);
        add.addActionListener((ActionEvent e) -> {
            _listenOnBeansTableModel.add();
        });
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionListenOnBeans action = new ActionListenOnBeans(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionListenOnBeans)) {
            throw new IllegalArgumentException("object is not a ActionListenOnBeans: " + object.getClass().getName());
        }
        ActionListenOnBeans listenOnBeans = (ActionListenOnBeans)object;
        
        // Do this to force update of the table
        _listenOnBeansTable.editCellAt(0, 2);
        
        listenOnBeans.clearReferences();
        
        for (NamedBeanReference reference : _listenOnBeansTableModel.getReferences()) {
            listenOnBeans.addReference(reference);
        }
        
        listenOnBeans.setLocalVariableNamedBean(_localVariableNamedBean.getText());
        listenOnBeans.setLocalVariableEvent(_localVariableEvent.getText());
        listenOnBeans.setLocalVariableNewValue(_localVariableNewValue.getText());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionListenOnBeans_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
