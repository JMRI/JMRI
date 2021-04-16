package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ShutdownComputer;
import jmri.jmrit.logixng.actions.ShutdownComputer.Operation;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ShutdownComputerSwing extends AbstractDigitalActionSwing {

    private JComboBox<Operation> _operationComboBox;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ShutdownComputer action = (ShutdownComputer)object;
        
        panel = new JPanel();
        _operationComboBox = new JComboBox<>();
        for (Operation e : Operation.values()) {
            _operationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_operationComboBox);
        panel.add(_operationComboBox);
        if (action != null) {
            _operationComboBox.setSelectedItem(action.getOperation());
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
        ShutdownComputer action = new ShutdownComputer(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ShutdownComputer)) {
            throw new IllegalArgumentException("object must be an ShutdownComputer but is a: "+object.getClass().getName());
        }
        ShutdownComputer action = (ShutdownComputer)object;
        action.setOperation(_operationComboBox.getItemAt(_operationComboBox.getSelectedIndex()));
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ShutdownComputer_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
