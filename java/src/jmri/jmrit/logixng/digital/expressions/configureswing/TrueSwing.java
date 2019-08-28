package jmri.jmrit.logixng.digital.expressions.configureswing;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.expressions.True;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 */
public class TrueSwing implements SwingConfiguratorInterface {

    private JPanel panel;
    
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel() throws IllegalArgumentException {
        createPanel(null);
        return panel;
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel(@Nonnull Base object) throws IllegalArgumentException {
        createPanel(object);
        return panel;
    }
    
    private void createPanel(Base object) {
        panel = new JPanel();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull StringBuilder errorMessage) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName) {
        True expression = new True(systemName);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @Nonnull String userName) {
        True expression = new True(systemName);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        // Nothing to update
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("True_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
//    private final static Logger log = LoggerFactory.getLogger(TrueSwing.class);
    
}
