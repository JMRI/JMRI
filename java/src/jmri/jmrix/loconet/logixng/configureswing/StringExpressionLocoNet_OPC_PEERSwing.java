package jmri.jmrix.loconet.logixng.configureswing;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.analog.expressions.configureswing.AbstractExpressionSwing;
import jmri.jmrix.loconet.logixng.AnalogExpressionLocoNet_OPC_PEER;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 */
public class StringExpressionLocoNet_OPC_PEERSwing extends AbstractExpressionSwing {

    @Override
    protected void createPanel(Base object) {
        panel = new JPanel();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull StringBuilder errorMessage) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        AnalogExpressionLocoNet_OPC_PEER action = new AnalogExpressionLocoNet_OPC_PEER(systemName, userName);
        return InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("AnalogExpressionLocoNet_OPC_PEER_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
