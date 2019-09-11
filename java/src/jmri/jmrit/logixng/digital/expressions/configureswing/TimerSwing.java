package jmri.jmrit.logixng.digital.expressions.configureswing;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.expressions.Timer;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 */
public class TimerSwing implements SwingConfiguratorInterface {

    private JPanel panel;
    
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel() throws IllegalArgumentException {
        createPanel();
//        createPanel(null);
        return panel;
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel(@Nonnull Base object) throws IllegalArgumentException {
        createPanel();
//        createPanel(object);
        return panel;
    }
    
    private void createPanel() {
//    private void createPanel(Base object) {
//        Timer expression = (Timer)object;
        
        panel = new JPanel();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull StringBuilder errorMessage) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @Nonnull String userName) {
        Timer expression = new Timer(systemName, userName);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
//        Timer expression = (Timer)object;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Timer_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
