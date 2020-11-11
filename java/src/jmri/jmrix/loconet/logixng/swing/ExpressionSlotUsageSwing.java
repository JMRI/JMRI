package jmri.jmrix.loconet.logixng.swing;

import java.awt.Color;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.swing.AbstractExpressionSwing;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.Has_HasNot;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.AdvancedState;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.SimpleState;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.Compare;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.PercentPieces;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 */
public class ExpressionSlotUsageSwing extends AbstractExpressionSwing {

    private final List<LocoNetSystemConnectionMemo> _systemConnections = jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
    private JButton _findNumSlotsButton;
    private JComboBox<Has_HasNot> _has_HasNot_ComboBox = new JComboBox<>();
    private JComboBox<SimpleState> _simpleStateComboBox = new JComboBox<>();
    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox = new JComboBox<>();
    private JComboBox<Compare> _compareComboBox = new JComboBox<>();
    private JTextField _textField = new JTextField("99");
    private JComboBox<PercentPieces> _percentPiecesComboBox = new JComboBox<>();
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ExpressionSlotUsage)) {
            throw new IllegalArgumentException("object must be an ExpressionSlotUsage but is a: "+object.getClass().getName());
        }
        
        ExpressionSlotUsage expression = (ExpressionSlotUsage)object;
        
        panel = new JPanel();
        
        
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        
        JPanel queryPanel = new JPanel();
        queryPanel.setBorder(BorderFactory.createLineBorder(Color.black));
//        queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.X_AXIS));
        
//        turnoutBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        
        
        
        
        
        _has_HasNot_ComboBox = new JComboBox<>();
        for (Has_HasNot e : Has_HasNot.values()) {
            _has_HasNot_ComboBox.addItem(e);
        }
        
        _simpleStateComboBox = new JComboBox<>();
        for (SimpleState e : SimpleState.values()) {
            _simpleStateComboBox.addItem(e);
        }
        
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel simplePanel = new javax.swing.JPanel();
        JPanel advancedPanel = new javax.swing.JPanel();
        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
        
        tabbedPane.addTab(Bundle.getMessage("TabbedPaneSimple"), simplePanel); // NOI1aa8N
        tabbedPane.addTab(Bundle.getMessage("TabbedPaneAdvanced"), advancedPanel); // NOIaa18N
        
        _simpleStateComboBox = new JComboBox<>();
        for (SimpleState e : SimpleState.values()) {
            _simpleStateComboBox.addItem(e);
        }
        simplePanel.add(_simpleStateComboBox);
        
        
        JCheckBox inUseCheckBox = new JCheckBox(Bundle.getMessage("AdvancedStateType_InUse"));
        JCheckBox idleCheckBox = new JCheckBox(Bundle.getMessage("AdvancedStateType_Idle"));
        JCheckBox commonCheckBox = new JCheckBox(Bundle.getMessage("AdvancedStateType_Common"));
        JCheckBox freeCheckBox = new JCheckBox(Bundle.getMessage("AdvancedStateType_Free"));
        advancedPanel.add(inUseCheckBox);
        advancedPanel.add(idleCheckBox);
        advancedPanel.add(commonCheckBox);
        advancedPanel.add(freeCheckBox);
        
        if (expression != null) {
            if (expression.getAdvancedStates().contains(AdvancedState.InUse)) {
                inUseCheckBox.setSelected(true);
            }
            if (expression.getAdvancedStates().contains(AdvancedState.Idle)) {
                idleCheckBox.setSelected(true);
            }
            if (expression.getAdvancedStates().contains(AdvancedState.Common)) {
                commonCheckBox.setSelected(true);
            }
            if (expression.getAdvancedStates().contains(AdvancedState.Free)) {
                freeCheckBox.setSelected(true);
            }
        }
        
        
        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        
        _compareComboBox = new JComboBox<>();
        for (Compare e : Compare.values()) {
            _compareComboBox.addItem(e);
        }
        
        _textField = new JTextField("99");
        
        _percentPiecesComboBox = new JComboBox<>();
        for (PercentPieces e : PercentPieces.values()) {
            _percentPiecesComboBox.addItem(e);
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
        
//        queryPanel.add(is_IsNot_ComboBox);
        
        JComponent[] components = new JComponent[]{
            _has_HasNot_ComboBox,
            tabbedPane,
//            advancedCheckBox.isSelected() ? advancedStateComboBox : simpleStateComboBox,
            _is_IsNot_ComboBox,
            _compareComboBox,
            _textField,
            _percentPiecesComboBox,
            };
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(Bundle.getMessage("ExpressionSlotUsage_Long"), components);
        
        for (JComponent c : componentList) queryPanel.add(c);
        
//        panel.add(stateComboBox);
        
        panel.add(queryPanel);
        
        _findNumSlotsButton = new JButton(Bundle.getMessage("GetNumSlots"));
        panel.add(_findNumSlotsButton);
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
        LocoNetSystemConnectionMemo memo = _systemConnections.get(0);
        
        ExpressionSlotUsage expression = new ExpressionSlotUsage(systemName, userName, memo);
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
