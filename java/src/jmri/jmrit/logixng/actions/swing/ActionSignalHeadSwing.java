package jmri.jmrit.logixng.actions.swing;

import java.awt.Color;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSignalHead;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ActionSignalHead object with a Swing JPanel.
 */
public class ActionSignalHeadSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneSignalHead;
    private BeanSelectCreatePanel<SignalHead> _signalHeadBeanPanel;
    private JPanel _panelSignalHeadDirect;
    private JPanel _panelSignalHeadReference;
    private JPanel _panelSignalHeadLocalVariable;
    private JPanel _panelSignalHeadFormula;
    private JTextField _signalHeadReferenceTextField;
    private JTextField _signalHeadLocalVariableTextField;
    private JTextField _signalHeadFormulaTextField;
    
    private JTabbedPane _tabbedPaneOperationType;
    private JComboBox<ActionSignalHead.OperationType> _operationComboBox;
    private JPanel _panelOperationTypeDirect;
    private JPanel _panelOperationTypeReference;
    private JPanel _panelOperationTypeLocalVariable;
    private JPanel _panelOperationTypeFormula;
    
    private JCheckBox _signalHeadOperationReferenceAppearanceCheckBox;
    private JTextField _signalHeadOperationReferenceTextField;
    private JTextField _signalHeadOperationReferenceAppearanceTextField;
    
    private JCheckBox _signalHeadOperationLocalVariableAppearanceCheckBox;
    private JTextField _signalHeadOperationLocalVariableTextField;
    private JTextField _signalHeadOperationLocalVariableAppearanceTextField;
    
    private JCheckBox _signalHeadOperationFormulaAppearanceCheckBox;
    private JTextField _signalHeadOperationFormulaTextField;
    private JTextField _signalHeadOperationFormulaAppearanceTextField;
    
    private JComboBox<SignalHeadAppearance> _signalHeadAppearanceComboBox;
    
    private BeanSelectCreatePanel<SignalHead> _exampleSignalHeadBeanPanel;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
/*        
        if ((object != null) && !(object instanceof ActionSignalHead)) {
            throw new IllegalArgumentException("object must be an ActionSignalHead but is a: "+object.getClass().getName());
        }
        ActionSignalHead action = (ActionSignalHead)object;
        
        panel = new JPanel();
        signalHeadBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SignalHeadManager.class), null);
        
        signalHeadStateComboBox = new JComboBox<>();
        
        queryTypeComboBox = new JComboBox<>();
        for (ActionSignalHead.OperationType e : ActionSignalHead.OperationType.values()) {
            queryTypeComboBox.addItem(e);
        }
        
        if (action != null) {
            if (action.getSignalHead() != null) {
                signalHeadBeanPanel.setDefaultNamedBean(action.getSignalHead().getBean());
            }
            queryTypeComboBox.setSelectedItem(action.getOperationType());
        }
        
        if ((action != null) && (action.getSignalHead() != null)) {
            SignalHead sh = action.getSignalHead().getBean();
            
            int[] states = sh.getValidStates();
            for (int s : states) {
                OperationType shs = new OperationType();
                shs._state = s;
                shs._name = sh.getAppearanceName(s);
                signalHeadStateComboBox.addItem(shs);
                if (action.getAppearance() == s) signalHeadStateComboBox.setSelectedItem(shs);
            }
        }
        panel.add(new JLabel(Bundle.getMessage("BeanNameSignalHead")));
        panel.add(signalHeadBeanPanel);
        panel.add(queryTypeComboBox);
        panel.add(signalHeadStateComboBox);
*/        
        
        
        
        
        ActionSignalHead action = (ActionSignalHead)object;
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JPanel examplePanel = new JPanel();
        JPanel innerExamplePanel = new JPanel();
        innerExamplePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        _exampleSignalHeadBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SignalHeadManager.class), null);
        innerExamplePanel.add(_exampleSignalHeadBeanPanel);
        
        JPanel actionPanel = new JPanel();
        
        _tabbedPaneSignalHead = new JTabbedPane();
        _panelSignalHeadDirect = new javax.swing.JPanel();
        _panelSignalHeadReference = new javax.swing.JPanel();
        _panelSignalHeadLocalVariable = new javax.swing.JPanel();
        _panelSignalHeadFormula = new javax.swing.JPanel();
        
        _tabbedPaneSignalHead.addTab(NamedBeanAddressing.Direct.toString(), _panelSignalHeadDirect);
        _tabbedPaneSignalHead.addTab(NamedBeanAddressing.Reference.toString(), _panelSignalHeadReference);
        _tabbedPaneSignalHead.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelSignalHeadLocalVariable);
        _tabbedPaneSignalHead.addTab(NamedBeanAddressing.Formula.toString(), _panelSignalHeadFormula);
        
        _tabbedPaneSignalHead.addChangeListener((ChangeEvent e) -> {
            enableDisableExampleSignalHeadBeanPanel();
        });
        
        _signalHeadBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SignalHeadManager.class), null);
        _panelSignalHeadDirect.add(_signalHeadBeanPanel);
        
        _signalHeadReferenceTextField = new JTextField();
        _signalHeadReferenceTextField.setColumns(30);
        _panelSignalHeadReference.add(_signalHeadReferenceTextField);
        
        _signalHeadLocalVariableTextField = new JTextField();
        _signalHeadLocalVariableTextField.setColumns(30);
        _panelSignalHeadLocalVariable.add(_signalHeadLocalVariableTextField);
        
        _signalHeadFormulaTextField = new JTextField();
        _signalHeadFormulaTextField.setColumns(30);
        _panelSignalHeadFormula.add(_signalHeadFormulaTextField);
        
        
        _tabbedPaneOperationType = new JTabbedPane();
        _panelOperationTypeDirect = new javax.swing.JPanel();
        _panelOperationTypeReference = new javax.swing.JPanel();
        _panelOperationTypeLocalVariable = new javax.swing.JPanel();
        _panelOperationTypeFormula = new javax.swing.JPanel();
        
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Direct.toString(), _panelOperationTypeDirect);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Reference.toString(), _panelOperationTypeReference);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOperationTypeLocalVariable);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Formula.toString(), _panelOperationTypeFormula);
        
        _tabbedPaneOperationType.addChangeListener((ChangeEvent e) -> {
            enableDisableExampleSignalHeadBeanPanel();
        });
        
        _operationComboBox = new JComboBox<>();
        for (ActionSignalHead.OperationType e : ActionSignalHead.OperationType.values()) {
            _operationComboBox.addItem(e);
        }
        
        _signalHeadAppearanceComboBox = new JComboBox<>();
        
        _panelOperationTypeDirect.add(_operationComboBox);
        _panelOperationTypeDirect.add(_signalHeadAppearanceComboBox);
        
        
        _signalHeadOperationReferenceAppearanceCheckBox = new JCheckBox("Set appearance only");
        _signalHeadOperationReferenceTextField = new JTextField();
        _signalHeadOperationReferenceTextField.setColumns(30);
        _signalHeadOperationReferenceAppearanceTextField = new JTextField();
        _signalHeadOperationReferenceAppearanceTextField.setColumns(30);
        
        _signalHeadOperationReferenceAppearanceCheckBox.addChangeListener((ChangeEvent e) -> {
            if (_signalHeadOperationReferenceAppearanceCheckBox.isSelected()) {
                _signalHeadOperationReferenceTextField.setEnabled(false);
            } else {
                _signalHeadOperationReferenceTextField.setEnabled(true);
            }
        });
        
        if (action != null) {
            _signalHeadOperationReferenceAppearanceCheckBox.setSelected(action.getOnlyAppearanceAddressing());
        }
        if (_signalHeadOperationReferenceAppearanceCheckBox.isSelected()) {
            _signalHeadOperationReferenceTextField.setEnabled(false);
        } else {
            _signalHeadOperationReferenceTextField.setEnabled(true);
        }
        
        setupIndirectAppearancePanel(
            _panelOperationTypeReference,
            _signalHeadOperationReferenceAppearanceCheckBox,
            _signalHeadOperationReferenceTextField,
            _signalHeadOperationReferenceAppearanceTextField);
        
        
        _signalHeadOperationLocalVariableAppearanceCheckBox = new JCheckBox("Set appearance only");
        _signalHeadOperationLocalVariableTextField = new JTextField();
        _signalHeadOperationLocalVariableTextField.setColumns(30);
        _signalHeadOperationLocalVariableAppearanceTextField = new JTextField();
        _signalHeadOperationLocalVariableAppearanceTextField.setColumns(30);
        
        _signalHeadOperationLocalVariableAppearanceCheckBox.addChangeListener((ChangeEvent e) -> {
            if (_signalHeadOperationLocalVariableAppearanceCheckBox.isSelected()) {
                _signalHeadOperationLocalVariableTextField.setEnabled(false);
            } else {
                _signalHeadOperationLocalVariableTextField.setEnabled(true);
            }
        });
        
        if (action != null) {
            _signalHeadOperationLocalVariableAppearanceCheckBox.setSelected(action.getOnlyAppearanceAddressing());
        }
        if (_signalHeadOperationLocalVariableAppearanceCheckBox.isSelected()) {
            _signalHeadOperationLocalVariableTextField.setEnabled(false);
        } else {
            _signalHeadOperationLocalVariableTextField.setEnabled(true);
        }
        
        setupIndirectAppearancePanel(
            _panelOperationTypeLocalVariable,
            _signalHeadOperationLocalVariableAppearanceCheckBox,
            _signalHeadOperationLocalVariableTextField,
            _signalHeadOperationLocalVariableAppearanceTextField);
        
        
        _signalHeadOperationFormulaAppearanceCheckBox = new JCheckBox("Set appearance only");
        _signalHeadOperationFormulaTextField = new JTextField();
        _signalHeadOperationFormulaTextField.setColumns(30);
        _signalHeadOperationFormulaAppearanceTextField = new JTextField();
        _signalHeadOperationFormulaAppearanceTextField.setColumns(30);
        
        _signalHeadOperationFormulaAppearanceCheckBox.addChangeListener((ChangeEvent e) -> {
            if (_signalHeadOperationFormulaAppearanceCheckBox.isSelected()) {
                _signalHeadOperationFormulaTextField.setEnabled(false);
            } else {
                _signalHeadOperationFormulaTextField.setEnabled(true);
            }
        });
        
        if (action != null) {
            _signalHeadOperationFormulaAppearanceCheckBox.setSelected(action.getOnlyAppearanceAddressing());
        }
        if (_signalHeadOperationFormulaAppearanceCheckBox.isSelected()) {
            _signalHeadOperationFormulaTextField.setEnabled(false);
        } else {
            _signalHeadOperationFormulaTextField.setEnabled(true);
        }
        
        setupIndirectAppearancePanel(
            _panelOperationTypeFormula,
            _signalHeadOperationFormulaAppearanceCheckBox,
            _signalHeadOperationFormulaTextField,
            _signalHeadOperationFormulaAppearanceTextField);
        
        
        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadDirect); break;
                case Reference: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadReference); break;
                case LocalVariable: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadLocalVariable); break;
                case Formula: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getSignalHead() != null) {
                _signalHeadBeanPanel.setDefaultNamedBean(action.getSignalHead().getBean());
            }
            _signalHeadReferenceTextField.setText(action.getReference());
            _signalHeadLocalVariableTextField.setText(action.getLocalVariable());
            _signalHeadFormulaTextField.setText(action.getFormula());
            
            switch (action.getOperationAndAppearanceAddressing()) {
                case Direct: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeDirect); break;
                case Reference: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeReference); break;
                case LocalVariable: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeLocalVariable); break;
                case Formula: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _operationComboBox.setSelectedItem(action.getOperationAndAppearanceAddressing());
            
            _signalHeadOperationReferenceTextField.setText(action.getOperationReference());
            _signalHeadOperationReferenceAppearanceTextField.setText(action.getAppearanceReference());
            
            _signalHeadOperationLocalVariableTextField.setText(action.getOperationLocalVariable());
            _signalHeadOperationLocalVariableAppearanceTextField.setText(action.getAppearanceLocalVariable());
            
            _signalHeadOperationFormulaTextField.setText(action.getOperationFormula());
            _signalHeadOperationFormulaAppearanceTextField.setText(action.getAppearanceFormula());
            
            SignalHead sh = null;
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeDirect) {
                if ((_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadDirect)
                        && (action.getSignalHead() != null)) {
                    sh = action.getSignalHead().getBean();
                } else if (action.getExampleSignalHead() != null) {
                    sh = action.getExampleSignalHead().getBean();
                }
            }
            
            if (sh != null) {
                int[] states = sh.getValidStates();
                for (int s : states) {
                    SignalHeadAppearance sha = new SignalHeadAppearance();
                    sha._state = s;
                    sha._name = sh.getAppearanceName(s);
                    _signalHeadAppearanceComboBox.addItem(sha);
                    if (action.getAppearance() == s) _signalHeadAppearanceComboBox.setSelectedItem(sha);
                }
            }
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneSignalHead,
            _tabbedPaneOperationType};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(Bundle.getMessage("SetSignalHead"), components);
        
        for (JComponent c : componentList) actionPanel.add(c);
        panel.add(actionPanel);
        
        
        panel.add(new JLabel("If you use Direct addressing of the signal head and Direct addressing of the appearance,"));
        panel.add(new JLabel(" you need to first select the signal head, then click Create/OK to save the settings, and"));
        panel.add(new JLabel(" then edit the signal head action again and select the appearance."));
        panel.add(new JLabel("If you do not use Direct addressing of the signal head but are using Direct addressing of"));
        panel.add(new JLabel("the appearance, you need to select an example signal head. The example signal head is used"));
        panel.add(new JLabel("to tell JMRI which aspects the indirect addressed signal head may show."));
        
        enableDisableExampleSignalHeadBeanPanel();
        
        examplePanel.add(new JLabel(Bundle.getMessage("SignalHeadExampleBean")));
        examplePanel.add(innerExamplePanel);
        
        panel.add(examplePanel);
    }
    
    private void setupIndirectAppearancePanel(
            JPanel panel,
            JCheckBox setAppearanceCheckBox,
            JTextField operationTextField,
            JTextField appearanceTextField) {
        
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.EAST;
        panel.add(new JLabel("Operation"), c);
        c.gridy = 2;
        c.anchor = java.awt.GridBagConstraints.EAST;
        panel.add(new JLabel("Appearance"), c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        panel.add(setAppearanceCheckBox, c);
        c.gridy = 1;
        panel.add(operationTextField, c);
        c.gridy = 2;
        panel.add(appearanceTextField, c);
    }
    
    private void enableDisableExampleSignalHeadBeanPanel() {
        if ((_tabbedPaneSignalHead.getSelectedComponent() != _panelSignalHeadDirect)
                && (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeDirect)) {
            _exampleSignalHeadBeanPanel.setEnabled(true);
        } else {
            _exampleSignalHeadBeanPanel.setEnabled(false);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionSignalHead action = new ActionSignalHead("IQDA1", null);
        
        try {
            if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadReference) {
                action.setReference(_signalHeadReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                action.setOperationReference(_signalHeadOperationReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setFormula(_signalHeadFormulaTextField.getText());
            if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionSignalHead action = new ActionSignalHead(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
/*        
        if (! (object instanceof ActionSignalHead)) {
            throw new IllegalArgumentException("object must be an ActionSignalHead but is a: "+object.getClass().getName());
        }
        ActionSignalHead action = (ActionSignalHead)object;
        if (!signalHeadBeanPanel.isEmpty()) {
            try {
                SignalHead signalHead = signalHeadBeanPanel.getNamedBean();
                if (signalHead != null) {
                    NamedBeanHandle<SignalHead> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(signalHead.getDisplayName(), signalHead);
                    action.setSignalHead(handle);
                }
            } catch (JmriException ex) {
                log.error("Cannot get NamedBeanHandle for signalHead", ex);
            }
        }
        
        action.setOperationType(queryTypeComboBox.getItemAt(queryTypeComboBox.getSelectedIndex()));
        if (signalHeadOperationComboBox.getItemCount() > 0) {
            action.setAppearance(signalHeadOperationComboBox.getItemAt(signalHeadOperationComboBox.getSelectedIndex())._state);
        }
*/        
        
        
        
        
        if (! (object instanceof ActionSignalHead)) {
            throw new IllegalArgumentException("object must be an ActionSignalHead but is a: "+object.getClass().getName());
        }
        ActionSignalHead action = (ActionSignalHead)object;
        try {
            if (!_signalHeadBeanPanel.isEmpty() && (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadDirect)) {
                SignalHead signalHead = _signalHeadBeanPanel.getNamedBean();
                if (signalHead != null) {
                    NamedBeanHandle<SignalHead> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(signalHead.getDisplayName(), signalHead);
                    action.setSignalHead(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for signalHead", ex);
        }
        try {
            if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_signalHeadReferenceTextField.getText());
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_signalHeadLocalVariableTextField.getText());
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_signalHeadFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneSignalHead has unknown selection");
            }
            
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeDirect) {
                action.setOperationAndAppearanceAddressing(NamedBeanAddressing.Direct);
                action.setOperationType((ActionSignalHead.OperationType)_operationComboBox.getSelectedItem());
                
                if (_signalHeadAppearanceComboBox.getItemCount() > 0) {
                    action.setAppearance(_signalHeadAppearanceComboBox
                            .getItemAt(_signalHeadAppearanceComboBox.getSelectedIndex())._state);
                }
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                action.setOnlyAppearanceAddressing(_signalHeadOperationReferenceAppearanceCheckBox.isSelected());
                action.setOperationAndAppearanceAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_signalHeadOperationReferenceTextField.getText());
                action.setAppearanceReference(_signalHeadOperationReferenceAppearanceTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeLocalVariable) {
                action.setOnlyAppearanceAddressing(_signalHeadOperationLocalVariableAppearanceCheckBox.isSelected());
                action.setOperationAndAppearanceAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_signalHeadOperationLocalVariableTextField.getText());
                action.setAppearanceLocalVariable(_signalHeadOperationLocalVariableAppearanceTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeFormula) {
                action.setOnlyAppearanceAddressing(_signalHeadOperationFormulaAppearanceCheckBox.isSelected());
                action.setOperationAndAppearanceAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_signalHeadOperationFormulaTextField.getText());
                action.setAppearanceFormula(_signalHeadOperationFormulaAppearanceTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOperationType has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
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
    
    
    private static class SignalHeadAppearance {
        
        private int _state;
        private String _name;
        
        @Override
        public String toString() {
            return _name;
        }
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalHeadSwing.class);
    
}
