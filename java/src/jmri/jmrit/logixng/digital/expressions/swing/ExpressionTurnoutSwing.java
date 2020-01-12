package jmri.jmrit.logixng.digital.expressions.swing;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.expressions.ExpressionTurnout;
import jmri.jmrit.logixng.digital.expressions.ExpressionTurnout.TurnoutState;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 */
public class ExpressionTurnoutSwing extends AbstractExpressionSwing {

    private BeanSelectCreatePanel<Turnout> turnoutBeanPanel;
    private JComboBox<Is_IsNot_Enum> is_IsNot_ComboBox;
    private JComboBox<TurnoutState> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ExpressionTurnout)) {
            throw new IllegalArgumentException("object must be an ExpressionTurnout but is a: "+object.getClass().getName());
        }
        ExpressionTurnout expression = (ExpressionTurnout)object;
        
        panel = new JPanel();
        turnoutBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        
        is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            is_IsNot_ComboBox.addItem(e);
        }
        
        stateComboBox = new JComboBox<>();
        for (TurnoutState e : TurnoutState.values()) {
            stateComboBox.addItem(e);
        }
        
        if (expression != null) {
            if (expression.getTurnout() != null) {
                turnoutBeanPanel.setDefaultNamedBean(expression.getTurnout().getBean());
            }
            is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            stateComboBox.setSelectedItem(expression.getTurnoutState());
        }
        
        panel.add(new JLabel(Bundle.getMessage("BeanNameTurnout")));
        panel.add(turnoutBeanPanel);
        panel.add(is_IsNot_ComboBox);
        panel.add(stateComboBox);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        if (1==0) {
            errorMessages.add("An error");
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionTurnout expression = new ExpressionTurnout(systemName, userName);
        try {
            if (!turnoutBeanPanel.isEmpty()) {
                Turnout turnout = turnoutBeanPanel.getNamedBean();
                if (turnout != null) {
                    NamedBeanHandle<Turnout> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(turnout.getDisplayName(), turnout);
                    expression.setTurnout(handle);
                }
            }
            expression.set_Is_IsNot((Is_IsNot_Enum)is_IsNot_ComboBox.getSelectedItem());
            expression.setTurnoutState((TurnoutState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for turnout", ex);
        }
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionTurnout)) {
            throw new IllegalArgumentException("object must be an ExpressionTurnout but is a: "+object.getClass().getName());
        }
        ExpressionTurnout expression = (ExpressionTurnout)object;
        try {
            Turnout turnout = turnoutBeanPanel.getNamedBean();
            if (turnout != null) {
                NamedBeanHandle<Turnout> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(turnout.getDisplayName(), turnout);
                expression.setTurnout(handle);
            }
            expression.set_Is_IsNot((Is_IsNot_Enum)is_IsNot_ComboBox.getSelectedItem());
            expression.setTurnoutState((TurnoutState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for turnout", ex);
        }
    }
    
    
    /**
     * Create Turnout object for the expression
     *
     * @param reference Turnout application description
     * @return The new output as Turnout object
     */
    protected Turnout getTurnoutFromPanel(String reference) {
        if (turnoutBeanPanel == null) {
            return null;
        }
        turnoutBeanPanel.setReference(reference); // pass turnout application description to be put into turnout Comment
        try {
            return turnoutBeanPanel.getNamedBean();
        } catch (jmri.JmriException ex) {
            log.warn("skipping creation of turnout not found for " + reference);
            return null;
        }
    }
    
//    private void noTurnoutMessage(String s1, String s2) {
//        log.warn("Could not provide turnout " + s2);
//        String msg = Bundle.getMessage("WarningNoTurnout", new Object[]{s1, s2});
//        JOptionPane.showMessageDialog(editFrame, msg,
//                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
//    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Turnout_Short");
    }
    
    @Override
    public void dispose() {
        if (turnoutBeanPanel != null) {
            turnoutBeanPanel.dispose();
        }
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ExpressionTurnoutSwing.class);
    
}
