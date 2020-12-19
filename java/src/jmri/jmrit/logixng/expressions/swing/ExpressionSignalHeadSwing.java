package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSignalHead_old;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ExpressionSignalHead object with a Swing JPanel.
 */
public class ExpressionSignalHeadSwing extends AbstractDigitalExpressionSwing {

    private BeanSelectCreatePanel<SignalHead> signalHeadBeanPanel;
    private JComboBox<ExpressionSignalHead_old.QueryType> queryTypeComboBox;
    private JComboBox<SignalHeadState> signalHeadStateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ExpressionSignalHead_old)) {
            throw new IllegalArgumentException("object must be an ExpressionSignalHead but is a: "+object.getClass().getName());
        }
        ExpressionSignalHead_old expression = (ExpressionSignalHead_old)object;
        
        panel = new JPanel();
        signalHeadBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SignalHeadManager.class), null);
        
        signalHeadStateComboBox = new JComboBox<>();
        
        queryTypeComboBox = new JComboBox<>();
        for (ExpressionSignalHead_old.QueryType e : ExpressionSignalHead_old.QueryType.values()) {
            queryTypeComboBox.addItem(e);
        }
        
        if (expression != null) {
            if (expression.getSignalHead() != null) {
                signalHeadBeanPanel.setDefaultNamedBean(expression.getSignalHead().getBean());
            }
            queryTypeComboBox.setSelectedItem(expression.getQueryType());
        }
        
        if ((expression != null) && (expression.getSignalHead() != null)) {
            SignalHead sh = expression.getSignalHead().getBean();
            
            int[] states = sh.getValidStates();
            for (int s : states) {
                SignalHeadState shs = new SignalHeadState();
                shs._state = s;
                shs._name = sh.getAppearanceName(s);
                signalHeadStateComboBox.addItem(shs);
                if (expression.getAppearance() == s) signalHeadStateComboBox.setSelectedItem(shs);
            }
        }
        panel.add(new JLabel(Bundle.getMessage("BeanNameSignalHead")));
        panel.add(signalHeadBeanPanel);
        panel.add(queryTypeComboBox);
        panel.add(signalHeadStateComboBox);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionSignalHead_old expression = new ExpressionSignalHead_old(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSignalHead_old)) {
            throw new IllegalArgumentException("object must be an ExpressionSignalHead but is a: "+object.getClass().getName());
        }
        ExpressionSignalHead_old expression = (ExpressionSignalHead_old)object;
        if (!signalHeadBeanPanel.isEmpty()) {
            try {
                SignalHead signalHead = signalHeadBeanPanel.getNamedBean();
                if (signalHead != null) {
                    NamedBeanHandle<SignalHead> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(signalHead.getDisplayName(), signalHead);
                    expression.setSignalHead(handle);
                }
            } catch (JmriException ex) {
                log.error("Cannot get NamedBeanHandle for signalHead", ex);
            }
        }
        
        expression.setQueryType(queryTypeComboBox.getItemAt(queryTypeComboBox.getSelectedIndex()));
        if (signalHeadStateComboBox.getItemCount() > 0) {
            expression.setAppearance(signalHeadStateComboBox.getItemAt(signalHeadStateComboBox.getSelectedIndex())._state);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("SignalHead_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private static class SignalHeadState {
        
        private int _state;
        private String _name;
        
        @Override
        public String toString() {
            return _name;
        }
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalHeadSwing.class);
    
}
