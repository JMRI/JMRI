package jmri.jmrix.loconet.logixng.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.swing.AbstractExpressionSwing;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage;
// import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.TurnoutState;
import jmri.jmrit.logixng.Is_IsNot_Enum;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 */
public class ExpressionSlotUsageSwing extends AbstractExpressionSwing {

//    private BeanSelectCreatePanel<Turnout> turnoutBeanPanel;
    private JComboBox<Is_IsNot_Enum> is_IsNot_ComboBox;
//    private JComboBox<TurnoutState> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ExpressionSlotUsage)) {
            throw new IllegalArgumentException("object must be an ExpressionSlotUsage but is a: "+object.getClass().getName());
        }
//        ExpressionSlotUsage expression = (ExpressionSlotUsage)object;
        
        panel = new JPanel();
//        turnoutBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        
        is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            is_IsNot_ComboBox.addItem(e);
        }
/*        
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
*/        
//        panel.add(new JLabel(Bundle.getMessage("BeanNameTurnout")));
//        panel.add(turnoutBeanPanel);
        panel.add(is_IsNot_ComboBox);
//        panel.add(stateComboBox);
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
        ExpressionSlotUsage expression = new ExpressionSlotUsage(systemName, userName);
/*        
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
*/        
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSlotUsage)) {
            throw new IllegalArgumentException("object must be an ExpressionTurnout but is a: "+object.getClass().getName());
        }
/*        
        ExpressionSlotUsage expression = (ExpressionSlotUsage)object;
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
*/        
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ExpressionSlotUsage_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSlotUsageSwing.class);
    
}
