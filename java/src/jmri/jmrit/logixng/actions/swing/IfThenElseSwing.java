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
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.actions.IfThenElse.Type;
import jmri.jmrit.logixng.util.TimerUnit;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class IfThenElseSwing extends AbstractDigitalActionSwing {

    private JComboBox<Type> _type;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof IfThenElse)) {
            throw new IllegalArgumentException("object must be an IfThenElse but is a: "+object.getClass().getName());
        }
        
        IfThenElse action = (IfThenElse)object;
        
        panel = new JPanel();
        
        _type = new JComboBox<>();
        for (Type type : Type.values()) _type.addItem(type);
        if (action != null) _type.setSelectedItem(action.getType());
        
        panel.add(_type);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        IfThenElse action = new IfThenElse(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof IfThenElse)) {
            throw new IllegalArgumentException("object must be an IfThenElse but is a: "+object.getClass().getName());
        }
        
        IfThenElse action = (IfThenElse)object;
        
        action.setType(_type.getItemAt(_type.getSelectedIndex()));
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("IfThenElse_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
