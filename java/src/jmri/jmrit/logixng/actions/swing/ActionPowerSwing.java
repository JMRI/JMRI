package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionPower;
import jmri.jmrit.logixng.actions.ActionPower.PowerState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionPower object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionPowerSwing extends AbstractDigitalActionSwing {

    private JComboBox<PowerState> _stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionPower action = (ActionPower)object;
        
        panel = new JPanel();
        
        _stateComboBox = new JComboBox<>();
        for (PowerState e : PowerState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        
        if (action != null) {
            _stateComboBox.setSelectedItem(action.getBeanState());
        }
        
        JComponent[] components = new JComponent[]{
            _stateComboBox};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionPower_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionPower action = new ActionPower(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionPower)) {
            throw new IllegalArgumentException("object must be an ActionPower but is a: "+object.getClass().getName());
        }
        ActionPower action = (ActionPower)object;
        action.setBeanState(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Power_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionPowerSwing.class);
    
}
