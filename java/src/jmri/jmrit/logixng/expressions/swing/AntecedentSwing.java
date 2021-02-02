package jmri.jmrit.logixng.expressions.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.Antecedent;
import jmri.jmrit.logixng.util.parser.*;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 */
public class AntecedentSwing extends AbstractDigitalExpressionSwing {

    private JTextField _antecedent;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        Antecedent expression = (Antecedent)object;
        panel = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage("Antecedent_Antecedent"));
        _antecedent = new JTextField();
        _antecedent.setColumns(40);
        if (expression != null) _antecedent.setText(expression.getAntecedent());
        panel.add(label);
        panel.add(_antecedent);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        Antecedent expression = new Antecedent(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof Antecedent)) {
            throw new IllegalArgumentException("object must be an Antecedent but is a: "+object.getClass().getName());
        }
        
        Antecedent expression = (Antecedent)object;
        
        try {
            expression.setAntecedent(_antecedent.getText());
        } catch (JmriException ex) {
            log.error("Error when parsing formula", ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Antecedent_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AntecedentSwing.class);
    
}
