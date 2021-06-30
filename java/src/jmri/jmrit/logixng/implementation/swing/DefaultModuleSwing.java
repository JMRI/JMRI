package jmri.jmrit.logixng.implementation.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.Module.Parameter;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable.DefaultParameter;
import jmri.jmrit.logixng.swing.AbstractSwingConfigurator;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.swing.SwingTools;
import jmri.jmrit.logixng.tools.swing.swing.ModuleParametersTableModel;

/**
 * Configures an DefaultModule object with a Swing JPanel.
 */
public class DefaultModuleSwing extends AbstractSwingConfigurator {

    static final java.util.ResourceBundle rbx =
            java.util.ResourceBundle.getBundle("jmri.jmrit.logixng.tools.swing.LogixNGSwingBundle");  // NOI18N
    
    protected JPanel panel;
    ModuleParametersTableModel _moduleParametersTableModel;
    
    /** {@inheritDoc} */
    @Override
    public String getExecuteEvaluateMenuText() {
        return Bundle.getMessage("MenuText_ExecuteEvaluate");
    }
    
    /** {@inheritDoc} */
    @Override
    public void executeEvaluate(@Nonnull Base object) {
        
        if (! (object instanceof MaleSocket)) {
            throw new IllegalArgumentException("object is not a MaleSocket");
        }
        if (! (((MaleSocket)object).getObject() instanceof Module)) {
            throw new IllegalArgumentException("((MaleSocket)object).getObject() is not a Module");
        }
        Module module = (Module)((MaleSocket)object).getObject();
        
        FemaleSocket femaleSocket = module.getRootSocket();
        if (!femaleSocket.isConnected()) {
            // Nothing to do since nothing is connected to the female socket
            jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("ExecuteEvaluate_ActionCompleted"),
                        Bundle.getMessage("ExecuteEvaluate_Title"),
                        JOptionPane.PLAIN_MESSAGE);
            });
            return;
        }
        
        Base obj = femaleSocket.getConnectedSocket();
        if (obj == null) throw new NullPointerException("object is null");
        while (obj instanceof MaleSocket) {
            obj = ((MaleSocket)obj).getObject();
        }
        SwingConfiguratorInterface swi =
                SwingTools.getSwingConfiguratorForClass(obj.getClass());
        
        swi.executeEvaluate(femaleSocket.getConnectedSocket());
    }
    
    /** {@inheritDoc} */
    @Override
    public BaseManager<? extends NamedBean> getManager() {
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel(@Nonnull JPanel buttonPanel) throws IllegalArgumentException {
        // This method is used to create a new item.
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel(@Nonnull Base object, @Nonnull JPanel buttonPanel) throws IllegalArgumentException {
        createPanel(object, buttonPanel);
        return panel;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getExampleSystemName() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        // This method is never used to create a module so we expect to have a module
        if (! (object instanceof Module)) {
            throw new IllegalArgumentException("object is not a Module: " + object.getClass().getName());
        }
        Module module = (Module)object;
        
        panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JPanel tablePanel = new JPanel();
        JTable table = new JTable();
        _moduleParametersTableModel = new ModuleParametersTableModel(module);
        table.setModel(_moduleParametersTableModel);
        table.setDefaultRenderer(SymbolTable.InitialValueType.class,
                new ModuleParametersTableModel.TypeCellRenderer());
        table.setDefaultEditor(SymbolTable.InitialValueType.class,
                new ModuleParametersTableModel.TypeCellEditor());
        table.setDefaultRenderer(ModuleParametersTableModel.Menu.class,
                new ModuleParametersTableModel.MenuCellRenderer());
        table.setDefaultEditor(ModuleParametersTableModel.Menu.class,
                new ModuleParametersTableModel.MenuCellEditor(table, _moduleParametersTableModel));
        _moduleParametersTableModel.setColumnForMenu(table);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(400, 200));
        tablePanel.add(scrollpane, BorderLayout.CENTER);
        panel.add(tablePanel);
        
        // Add parameter
        JButton add = new JButton(Bundle.getMessage("TableAddParameter"));
        buttonPanel.add(add);
        add.addActionListener((ActionEvent e) -> {
            _moduleParametersTableModel.add();
        });
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        boolean hasErrors = false;
        for (DefaultParameter p : _moduleParametersTableModel.getParameters()) {
            if (p.getName().isEmpty()) {
                errorMessages.add(Bundle.getMessage("ParameterNameIsEmpty", p.getName()));
                hasErrors = true;
            } else if (! SymbolTable.validateName(p.getName())) {
                errorMessages.add(Bundle.getMessage("ParameterNameIsNotValid", p.getName()));
                hasErrors = true;
            }
            if (!p.isInput() && !p.isOutput()) {
                errorMessages.add(Bundle.getMessage("ParameterIsNotInNorOut", p.getName()));
                hasErrors = true;
            }
        }
        
        return !hasErrors;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        // This method is never used to create a module so we expect to have a module
        if (! (object instanceof Module)) {
            throw new IllegalArgumentException("object is not a Module: " + object.getClass().getName());
        }
        Module module = (Module)object;
        
        module.getParameters().clear();
        for (Parameter p : _moduleParametersTableModel.getParameters()) {
            module.addParameter(p);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("DefaultModule_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
