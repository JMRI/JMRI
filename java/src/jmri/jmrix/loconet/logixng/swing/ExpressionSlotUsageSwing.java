package jmri.jmrix.loconet.logixng.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.text.NumberFormatter;

import jmri.*;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.swing.AbstractExpressionSwing;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.Has_HasNot;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.AdvancedState;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.SimpleState;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.Compare;
import jmri.jmrix.loconet.logixng.ExpressionSlotUsage.PercentPieces;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionSlotUsageSwing extends AbstractExpressionSwing {

    private JButton _findNumSlotsButton;
    private JComboBox<LocoNetConnection> _locoNetConnection;
    private JTabbedPane _tabbedPane;
    private JPanel _simplePanel;
    private JPanel _advancedPanel;
    private JComboBox<Has_HasNot> _has_HasNot_ComboBox;
    private JComboBox<SimpleState> _simpleStateComboBox;
    private JCheckBox inUseCheckBox;
    private JCheckBox idleCheckBox;
    private JCheckBox commonCheckBox;
    private JCheckBox freeCheckBox;
    private JComboBox<Compare> _compareComboBox;
    private JTextField _numberField;
    private JComboBox<PercentPieces> _percentPiecesComboBox;
    
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
        
        JPanel locoNetPanel = new JPanel();
        locoNetPanel.add(new JLabel(Bundle.getMessage("LocoNetConnection")));
        
        _locoNetConnection = new JComboBox<>();
        List<LocoNetSystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
        for (LocoNetSystemConnectionMemo connection : systemConnections) {
            _locoNetConnection.addItem(new LocoNetConnection(connection));
        }
        locoNetPanel.add(_locoNetConnection);
        
        _has_HasNot_ComboBox = new JComboBox<>();
        for (Has_HasNot e : Has_HasNot.values()) {
            _has_HasNot_ComboBox.addItem(e);
        }
        
        _simpleStateComboBox = new JComboBox<>();
        for (SimpleState e : SimpleState.values()) {
            _simpleStateComboBox.addItem(e);
        }
        
        _tabbedPane = new JTabbedPane();
        _simplePanel = new javax.swing.JPanel();
        _advancedPanel = new javax.swing.JPanel();
        _advancedPanel.setLayout(new BoxLayout(_advancedPanel, BoxLayout.Y_AXIS));
        
        _tabbedPane.addTab(Bundle.getMessage("TabbedPaneSimple"), _simplePanel); // NOI1aa8N
        _tabbedPane.addTab(Bundle.getMessage("TabbedPaneAdvanced"), _advancedPanel); // NOIaa18N
        
        _simpleStateComboBox = new JComboBox<>();
        for (SimpleState e : SimpleState.values()) {
            _simpleStateComboBox.addItem(e);
        }
        _simplePanel.add(_simpleStateComboBox);
        
        inUseCheckBox = new JCheckBox(Bundle.getMessage("AdvancedStateType_InUse"));
        idleCheckBox = new JCheckBox(Bundle.getMessage("AdvancedStateType_Idle"));
        commonCheckBox = new JCheckBox(Bundle.getMessage("AdvancedStateType_Common"));
        freeCheckBox = new JCheckBox(Bundle.getMessage("AdvancedStateType_Free"));
        _advancedPanel.add(inUseCheckBox);
        _advancedPanel.add(idleCheckBox);
        _advancedPanel.add(commonCheckBox);
        _advancedPanel.add(freeCheckBox);
        
        _compareComboBox = new JComboBox<>();
        for (Compare e : Compare.values()) {
            _compareComboBox.addItem(e);
        }
        
//        _numberField = new JTextField("99");
        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(120);
        formatter.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        formatter.setCommitsOnValidEdit(true);
        _numberField = new JFormattedTextField();
        _numberField.setColumns(3);
        
        _percentPiecesComboBox = new JComboBox<>();
        for (PercentPieces e : PercentPieces.values()) {
            _percentPiecesComboBox.addItem(e);
        }
        
        if (expression != null) {
            _locoNetConnection.setSelectedItem(expression.getMemo());
            
            if (expression.getAdvanced()) {
                _tabbedPane.setSelectedComponent(_advancedPanel);
            }
            
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
            
            _has_HasNot_ComboBox.setSelectedItem(expression.get_Has_HasNot());
            _simpleStateComboBox.setSelectedItem(expression.getSimpleState());
            _compareComboBox.setSelectedItem(expression.getCompare());
            _numberField.setText(Integer.toString(expression.getNumber()));
            _percentPiecesComboBox.setSelectedItem(expression.getPercentPieces());
        }
        
        panel.add(locoNetPanel);
        
        JComponent[] components = new JComponent[]{
            _has_HasNot_ComboBox,
            _tabbedPane,
            _compareComboBox,
            _numberField,
            _percentPiecesComboBox,
            };
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(Bundle.getMessage("ExpressionSlotUsage_Long"), components);
        
        for (JComponent c : componentList) queryPanel.add(c);
        
        panel.add(queryPanel);
        
        _findNumSlotsButton = new JButton(Bundle.getMessage("GetNumSlots"));
        _findNumSlotsButton.addActionListener((ActionEvent e) -> {
            LocoNetSystemConnectionMemo memo =
                    _locoNetConnection.getItemAt(_locoNetConnection.getSelectedIndex())._memo;
            new GetNumSlotsDialog(memo).initComponents();
        });
        
        panel.add(_findNumSlotsButton);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        try {
            Integer.parseInt(_numberField.getText());
        } catch (NumberFormatException e) {
            errorMessages.add("Number is not a valid integer");
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        LocoNetSystemConnectionMemo memo =
                _locoNetConnection.getItemAt(_locoNetConnection.getSelectedIndex())._memo;
        
        ExpressionSlotUsage expression = new ExpressionSlotUsage(systemName, userName, memo);
        updateObject(expression);
        
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSlotUsage)) {
            throw new IllegalArgumentException("object must be an ExpressionTurnout but is a: "+object.getClass().getName());
        }
        
        ExpressionSlotUsage expression = (ExpressionSlotUsage)object;
        
        expression.setMemo(_locoNetConnection.getItemAt(_locoNetConnection.getSelectedIndex())._memo);
        
        expression.setAdvanced(_tabbedPane.getSelectedComponent() == _advancedPanel);
        
        Set<AdvancedState> advancedStates = new HashSet<>();
        if (inUseCheckBox.isSelected()) advancedStates.add(AdvancedState.InUse);
        if (idleCheckBox.isSelected()) advancedStates.add(AdvancedState.Idle);
        if (commonCheckBox.isSelected()) advancedStates.add(AdvancedState.Common);
        if (freeCheckBox.isSelected()) advancedStates.add(AdvancedState.Free);
        expression.setAdvancedStates(advancedStates);
        
        expression.set_Has_HasNot(_has_HasNot_ComboBox.getItemAt(_has_HasNot_ComboBox.getSelectedIndex()));
        expression.setSimpleState(_simpleStateComboBox.getItemAt(_simpleStateComboBox.getSelectedIndex()));
        expression.setCompare(_compareComboBox.getItemAt(_compareComboBox.getSelectedIndex()));
        expression.setNumber(Integer.parseInt(_numberField.getText()));
        expression.setPercentPieces(_percentPiecesComboBox.getItemAt(_percentPiecesComboBox.getSelectedIndex()));
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ExpressionSlotUsage_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    
    private static class LocoNetConnection {
        
        private LocoNetSystemConnectionMemo _memo;
        
        public LocoNetConnection(LocoNetSystemConnectionMemo memo) {
            _memo = memo;
        }
        
        @Override
        public String toString() {
            return _memo.getSystemPrefix();
        }
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSlotUsageSwing.class);

}
