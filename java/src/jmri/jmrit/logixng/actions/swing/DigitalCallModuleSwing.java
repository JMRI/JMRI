package jmri.jmrit.logixng.actions.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.Module.ParameterData;
import jmri.jmrit.logixng.actions.DigitalCallModule;
import jmri.jmrit.logixng.tools.swing.CallModuleParameterTableModel;

/**
 * Configures an ModuleDigitalAction object with a Swing JPanel.
 */
public class DigitalCallModuleSwing extends AbstractDigitalActionSwing {

    CallModuleParameterTableModel _moduleParametersTableModel;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && (! (object instanceof DigitalCallModule))) {
            throw new IllegalArgumentException("object is not a Module: " + object.getClass().getName());
        }
        DigitalCallModule callModule = (DigitalCallModule)object;
        
        panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        Module module = null;
        List<ParameterData> parameterData;
        if (callModule != null) {
            module = callModule.getModule() != null ? callModule.getModule().getBean() : null;
            parameterData = callModule.getParameterData();
        } else {
            parameterData = new ArrayList<>();
        }
        JPanel tablePanel = new JPanel();
        JTable table = new JTable();
        _moduleParametersTableModel = new CallModuleParameterTableModel(module, parameterData);
        table.setModel(_moduleParametersTableModel);
        table.setDefaultRenderer(SymbolTable.InitialValueType.class,
                new CallModuleParameterTableModel.TypeCellRenderer());
        table.setDefaultEditor(SymbolTable.InitialValueType.class,
                new CallModuleParameterTableModel.TypeCellEditor());
//        table.setDefaultRenderer(CallModuleParameterTableModel.Menu.class,
//                new CallModuleParameterTableModel.MenuCellRenderer());
//        table.setDefaultEditor(CallModuleParameterTableModel.Menu.class,
//                new CallModuleParameterTableModel.MenuCellEditor(table, _moduleParametersTableModel));
//        _moduleParametersTableModel.setColumnForMenu(table);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(400, 200));
        tablePanel.add(scrollpane, BorderLayout.CENTER);
        panel.add(tablePanel);
        
//        // Add parameter
//        JButton add = new JButton(Bundle.getMessage("TableAddParameter"));
//        buttonPanel.add(add);
//        add.addActionListener((ActionEvent e) -> {
//            _moduleParametersTableModel.add();
//        });
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
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("DigitalCallModule_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
