package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSignalHead;
import jmri.jmrit.logixng.actions.ActionSignalHead.OperationType;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ActionSignalHead object with a Swing JPanel.
 */
public class ActionSignalHeadSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneSignalHead;
    private BeanSelectCreatePanel<SignalHead> signalHeadBeanPanel;
    private JPanel _panelSignalHeadDirect;
    private JPanel _panelSignalHeadReference;
    private JPanel _panelSignalHeadLocalVariable;
    private JPanel _panelSignalHeadFormula;
    private JTextField _signalHeadReferenceTextField;
    private JTextField _signalHeadLocalVariableTextField;
    private JTextField _signalHeadFormulaTextField;
    
    private JTabbedPane _tabbedPaneOperationType;
    private JComboBox<ActionSignalHead.OperationType> operationComboBox;
    private JPanel _panelOperationTypeDirect;
    private JPanel _panelOperationTypeReference;
    private JPanel _panelOperationTypeLocalVariable;
    private JPanel _panelOperationTypeFormula;
    private JTextField _signalHeadOperationReferenceTextField;
    private JTextField _signalHeadOperationLocalVariableTextField;
    private JTextField _signalHeadOperationFormulaTextField;
    
    
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
        
        _tabbedPaneSignalHead = new JTabbedPane();
        _panelSignalHeadDirect = new javax.swing.JPanel();
        _panelSignalHeadReference = new javax.swing.JPanel();
        _panelSignalHeadLocalVariable = new javax.swing.JPanel();
        _panelSignalHeadFormula = new javax.swing.JPanel();
        
        _tabbedPaneSignalHead.addTab(NamedBeanAddressing.Direct.toString(), _panelSignalHeadDirect);
        _tabbedPaneSignalHead.addTab(NamedBeanAddressing.Reference.toString(), _panelSignalHeadReference);
        _tabbedPaneSignalHead.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelSignalHeadLocalVariable);
        _tabbedPaneSignalHead.addTab(NamedBeanAddressing.Formula.toString(), _panelSignalHeadFormula);
        
        signalHeadBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SignalHeadManager.class), null);
        _panelSignalHeadDirect.add(signalHeadBeanPanel);
        
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
        
        operationComboBox = new JComboBox<>();
        for (ActionSignalHead.OperationType e : ActionSignalHead.OperationType.values()) {
            operationComboBox.addItem(e);
        }
        
        _panelOperationTypeDirect.add(operationComboBox);
        
        _signalHeadOperationReferenceTextField = new JTextField();
        _signalHeadOperationReferenceTextField.setColumns(30);
        _panelOperationTypeReference.add(_signalHeadOperationReferenceTextField);
        
        _signalHeadOperationLocalVariableTextField = new JTextField();
        _signalHeadOperationLocalVariableTextField.setColumns(30);
        _panelOperationTypeLocalVariable.add(_signalHeadOperationLocalVariableTextField);
        
        _signalHeadOperationFormulaTextField = new JTextField();
        _signalHeadOperationFormulaTextField.setColumns(30);
        _panelOperationTypeFormula.add(_signalHeadOperationFormulaTextField);
        
        
        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadDirect); break;
                case Reference: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadReference); break;
                case LocalVariable: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadLocalVariable); break;
                case Formula: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getSignalHead() != null) {
                signalHeadBeanPanel.setDefaultNamedBean(action.getSignalHead().getBean());
            }
            _signalHeadReferenceTextField.setText(action.getReference());
            _signalHeadLocalVariableTextField.setText(action.getLocalVariable());
            _signalHeadFormulaTextField.setText(action.getFormula());
            
            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeDirect); break;
                case Reference: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeReference); break;
                case LocalVariable: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeLocalVariable); break;
                case Formula: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
//DANIEL FEL!!!            operationComboBox.setSelectedItem(action.getOperation());
            _signalHeadOperationReferenceTextField.setText(action.getOperationReference());
            _signalHeadOperationLocalVariableTextField.setText(action.getOperationLocalVariable());
            _signalHeadOperationFormulaTextField.setText(action.getOperationFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneSignalHead,
            _tabbedPaneOperationType};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(Bundle.getMessage("SetSignalHead"), components);
        
        for (JComponent c : componentList) panel.add(c);
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
            if (!signalHeadBeanPanel.isEmpty() && (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadDirect)) {
                SignalHead signalHead = signalHeadBeanPanel.getNamedBean();
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
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationType((ActionSignalHead.OperationType)operationComboBox.getSelectedItem());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_signalHeadOperationReferenceTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_signalHeadOperationLocalVariableTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_signalHeadOperationFormulaTextField.getText());
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
    
    
    private static class OperationType {
        
        private int _state;
        private String _name;
        
        @Override
        public String toString() {
            return _name;
        }
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalHeadSwing.class);
    
}
