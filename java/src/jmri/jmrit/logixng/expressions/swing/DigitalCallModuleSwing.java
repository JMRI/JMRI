package jmri.jmrit.logixng.expressions.swing;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.Module.ParameterData;
import jmri.jmrit.logixng.Module.ReturnValueType;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.expressions.DigitalCallModule;
import jmri.jmrit.logixng.tools.swing.CallModuleParameterTableModel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ModuleDigitalExpression object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class DigitalCallModuleSwing extends AbstractDigitalExpressionSwing {

    private JComboBox<ModuleItem> _moduleComboBox;
    private CallModuleParameterTableModel _moduleParametersTableModel;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && (! (object instanceof DigitalCallModule))) {
            throw new IllegalArgumentException("object is not a Module: " + object.getClass().getName());
        }
        DigitalCallModule callModule = (DigitalCallModule)object;
        
        panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JPanel beanPanel = new JPanel();
        beanPanel.add(new JLabel("Module:"));
        _moduleComboBox = new JComboBox<>();
        _moduleComboBox.addItem(new ModuleItem(null));
        for (Module m : InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet()) {
//            System.out.format("Root socket type: %s%n", m.getRootSocketType().getName());
            if ("DefaultFemaleDigitalExpressionSocket".equals(m.getRootSocketType().getName())) {
                ModuleItem mi = new ModuleItem(m);
                _moduleComboBox.addItem(mi);
                if ((callModule != null)
                        && (callModule.getModule() != null)
                        && (callModule.getModule().getBean() == m)) {
                    _moduleComboBox.setSelectedItem(mi);
                }
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(_moduleComboBox);
        beanPanel.add(_moduleComboBox);
        panel.add(beanPanel);
        
        Module module = null;
        List<ParameterData> parameterData;
        if (callModule != null) {
            if (callModule.getModule() != null) {
                module = callModule.getModule().getBean();
            }
            parameterData = callModule.getParameterData();
        } else {
            parameterData = new ArrayList<>();
        }
        JPanel tablePanel = new JPanel();
        JTable table = new JTable();
        _moduleParametersTableModel = new CallModuleParameterTableModel(module, parameterData);
        table.setModel(_moduleParametersTableModel);
        table.setDefaultRenderer(InitialValueType.class,
                new CallModuleParameterTableModel.TypeCellRenderer());
        table.setDefaultEditor(InitialValueType.class,
                new CallModuleParameterTableModel.InitialValueCellEditor());
        table.setDefaultRenderer(ReturnValueType.class,
                new CallModuleParameterTableModel.TypeCellRenderer());
        table.setDefaultEditor(ReturnValueType.class,
                new CallModuleParameterTableModel.ReturnValueCellEditor());
        _moduleParametersTableModel.setColumnsForComboBoxes(table);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(400, 200));
        tablePanel.add(scrollpane, BorderLayout.CENTER);
        panel.add(tablePanel);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        DigitalCallModule action = new DigitalCallModule(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof DigitalCallModule)) {
            throw new IllegalArgumentException("object is not a Module: " + object.getClass().getName());
        }
        DigitalCallModule callModule = (DigitalCallModule)object;
        
        ModuleItem mi = _moduleComboBox.getItemAt(_moduleComboBox.getSelectedIndex());
        if (mi._module != null) {
            callModule.setModule(mi._module);
            callModule.getParameterData().clear();
            callModule.getParameterData().addAll(_moduleParametersTableModel.getParameters());
//            for (ParameterData pd : _moduleParametersTableModel.getParameters()) {
//                callModule.addParameter(pd);
//            }
        }
        else {
            callModule.removeModule();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("DigitalCallModule_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private static class ModuleItem {
        
        private final Module _module;
        
        public ModuleItem(Module m) {
            _module = m;
        }
        
        @Override
        public String toString() {
            if (_module == null) return "";
            else return _module.getDisplayName();
        }
    }
    
}
