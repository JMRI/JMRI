package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSignalMast;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ExpressionSignalMast object with a Swing JPanel.
 */
public class ExpressionSignalMastSwing extends AbstractDigitalExpressionSwing {

    private BeanSelectCreatePanel<SignalMast> signalMastBeanPanel;
    private JComboBox<ExpressionSignalMast.QueryType> queryTypeComboBox;
    private JComboBox<String> signalMastAspectComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ExpressionSignalMast)) {
            throw new IllegalArgumentException("object must be an ExpressionSignalMast but is a: "+object.getClass().getName());
        }
        ExpressionSignalMast expression = (ExpressionSignalMast)object;
        
        panel = new JPanel();
        signalMastBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SignalMastManager.class), null);
        
        signalMastAspectComboBox = new JComboBox<>();
        
        queryTypeComboBox = new JComboBox<>();
        for (ExpressionSignalMast.QueryType e : ExpressionSignalMast.QueryType.values()) {
            queryTypeComboBox.addItem(e);
        }
        
        if (expression != null) {
            if (expression.getSignalMast() != null) {
                signalMastBeanPanel.setDefaultNamedBean(expression.getSignalMast().getBean());
            }
            queryTypeComboBox.setSelectedItem(expression.getQueryType());
        }
        
        if ((expression != null) && (expression.getSignalMast() != null)) {
            SignalMast sm = expression.getSignalMast().getBean();
            
            for (String aspect : sm.getValidAspects()) {
                signalMastAspectComboBox.addItem(aspect);
                if (aspect.equals(expression.getAspect())) signalMastAspectComboBox.setSelectedItem(aspect);
            }
        }
        panel.add(new JLabel(Bundle.getMessage("BeanNameSignalMast")));
        panel.add(signalMastBeanPanel);
        panel.add(queryTypeComboBox);
        panel.add(signalMastAspectComboBox);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionSignalMast expression = new ExpressionSignalMast(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSignalMast)) {
            throw new IllegalArgumentException("object must be an ExpressionSignalMast but is a: "+object.getClass().getName());
        }
        ExpressionSignalMast expression = (ExpressionSignalMast)object;
        if (!signalMastBeanPanel.isEmpty()) {
            try {
                SignalMast signalMast = signalMastBeanPanel.getNamedBean();
                if (signalMast != null) {
                    NamedBeanHandle<SignalMast> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(signalMast.getDisplayName(), signalMast);
                    expression.setSignalMast(handle);
                }
            } catch (JmriException ex) {
                log.error("Cannot get NamedBeanHandle for signalMast", ex);
            }
        }
        
        expression.setQueryType(queryTypeComboBox.getItemAt(queryTypeComboBox.getSelectedIndex()));
        if (signalMastAspectComboBox.getItemCount() > 0) {
            expression.setAspect(signalMastAspectComboBox.getItemAt(signalMastAspectComboBox.getSelectedIndex()));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("SignalMast_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalMastSwing.class);
    
}
