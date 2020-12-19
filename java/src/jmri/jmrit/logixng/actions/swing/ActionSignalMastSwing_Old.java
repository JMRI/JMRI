package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSignalMast;
import jmri.jmrit.logixng.actions.ActionSignalMast.OperationType;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ActionSignalMast object with a Swing JPanel.
 */
public class ActionSignalMastSwing_Old extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneSignalMast;
    private BeanSelectCreatePanel<SignalMast> signalMastBeanPanel;
    private JPanel _panelSignalMastDirect;
    private JPanel _panelSignalMastReference;
    private JPanel _panelSignalMastLocalVariable;
    private JPanel _panelSignalMastFormula;
    private JTextField _signalMastReferenceTextField;
    private JTextField _signalMastLocalVariableTextField;
    private JTextField _signalMastFormulaTextField;
    
    private JTabbedPane _tabbedPaneOperationType;
    private JComboBox<ActionSignalMast.OperationType> operationComboBox;
    private JPanel _panelOperationTypeDirect;
    private JPanel _panelOperationTypeReference;
    private JPanel _panelOperationTypeLocalVariable;
    private JPanel _panelOperationTypeFormula;
    private JTextField _signalMastOperationReferenceTextField;
    private JTextField _signalMastOperationLocalVariableTextField;
    private JTextField _signalMastOperationFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
/*        
        if ((object != null) && !(object instanceof ActionSignalMast)) {
            throw new IllegalArgumentException("object must be an ActionSignalMast but is a: "+object.getClass().getName());
        }
        ActionSignalMast action = (ActionSignalMast)object;
        
        panel = new JPanel();
        signalMastBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SignalMastManager.class), null);
        
        signalMastAspectComboBox = new JComboBox<>();
        
        queryTypeComboBox = new JComboBox<>();
        for (ActionSignalMast.OperationType e : ActionSignalMast.OperationType.values()) {
            queryTypeComboBox.addItem(e);
        }
        
        if (action != null) {
            if (action.getSignalMast() != null) {
                signalMastBeanPanel.setDefaultNamedBean(action.getSignalMast().getBean());
            }
            queryTypeComboBox.setSelectedItem(action.getOperationType());
        }
        
        if ((action != null) && (action.getSignalMast() != null)) {
            SignalMast sm = action.getSignalMast().getBean();
            
            for (String aspect : sm.getValidAspects()) {
                signalMastAspectComboBox.addItem(aspect);
                if (aspect.equals(action.getAspect())) signalMastAspectComboBox.setSelectedItem(aspect);
            }
        }
        panel.add(new JLabel(Bundle.getMessage("BeanNameSignalMast")));
        panel.add(signalMastBeanPanel);
        panel.add(queryTypeComboBox);
        panel.add(signalMastAspectComboBox);
*/        
        
        
        
        
        ActionSignalMast action = (ActionSignalMast)object;
        
        panel = new JPanel();
        
        _tabbedPaneSignalMast = new JTabbedPane();
        _panelSignalMastDirect = new javax.swing.JPanel();
        _panelSignalMastReference = new javax.swing.JPanel();
        _panelSignalMastLocalVariable = new javax.swing.JPanel();
        _panelSignalMastFormula = new javax.swing.JPanel();
        
        _tabbedPaneSignalMast.addTab(NamedBeanAddressing.Direct.toString(), _panelSignalMastDirect);
        _tabbedPaneSignalMast.addTab(NamedBeanAddressing.Reference.toString(), _panelSignalMastReference);
        _tabbedPaneSignalMast.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelSignalMastLocalVariable);
        _tabbedPaneSignalMast.addTab(NamedBeanAddressing.Formula.toString(), _panelSignalMastFormula);
        
        signalMastBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SignalMastManager.class), null);
        _panelSignalMastDirect.add(signalMastBeanPanel);
        
        _signalMastReferenceTextField = new JTextField();
        _signalMastReferenceTextField.setColumns(30);
        _panelSignalMastReference.add(_signalMastReferenceTextField);
        
        _signalMastLocalVariableTextField = new JTextField();
        _signalMastLocalVariableTextField.setColumns(30);
        _panelSignalMastLocalVariable.add(_signalMastLocalVariableTextField);
        
        _signalMastFormulaTextField = new JTextField();
        _signalMastFormulaTextField.setColumns(30);
        _panelSignalMastFormula.add(_signalMastFormulaTextField);
        
        
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
        for (ActionSignalMast.OperationType e : ActionSignalMast.OperationType.values()) {
            operationComboBox.addItem(e);
        }
        
        _panelOperationTypeDirect.add(operationComboBox);
        
        _signalMastOperationReferenceTextField = new JTextField();
        _signalMastOperationReferenceTextField.setColumns(30);
        _panelOperationTypeReference.add(_signalMastOperationReferenceTextField);
        
        _signalMastOperationLocalVariableTextField = new JTextField();
        _signalMastOperationLocalVariableTextField.setColumns(30);
        _panelOperationTypeLocalVariable.add(_signalMastOperationLocalVariableTextField);
        
        _signalMastOperationFormulaTextField = new JTextField();
        _signalMastOperationFormulaTextField.setColumns(30);
        _panelOperationTypeFormula.add(_signalMastOperationFormulaTextField);
        
        
        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneSignalMast.setSelectedComponent(_panelSignalMastDirect); break;
                case Reference: _tabbedPaneSignalMast.setSelectedComponent(_panelSignalMastReference); break;
                case LocalVariable: _tabbedPaneSignalMast.setSelectedComponent(_panelSignalMastLocalVariable); break;
                case Formula: _tabbedPaneSignalMast.setSelectedComponent(_panelSignalMastFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getSignalMast() != null) {
                signalMastBeanPanel.setDefaultNamedBean(action.getSignalMast().getBean());
            }
            _signalMastReferenceTextField.setText(action.getReference());
            _signalMastLocalVariableTextField.setText(action.getLocalVariable());
            _signalMastFormulaTextField.setText(action.getFormula());
            
            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeDirect); break;
                case Reference: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeReference); break;
                case LocalVariable: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeLocalVariable); break;
                case Formula: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            operationComboBox.setSelectedItem(action.getOperationAddressing());
            _signalMastOperationReferenceTextField.setText(action.getOperationReference());
            _signalMastOperationLocalVariableTextField.setText(action.getOperationLocalVariable());
            _signalMastOperationFormulaTextField.setText(action.getOperationFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneSignalMast,
            _tabbedPaneOperationType};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(Bundle.getMessage("SetSignalMast"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionSignalMast action = new ActionSignalMast("IQDA1", null);
        
        try {
            if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastReference) {
                action.setReference(_signalMastReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                action.setOperationReference(_signalMastOperationReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setFormula(_signalMastFormulaTextField.getText());
            if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastFormula) {
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
        ActionSignalMast action = new ActionSignalMast(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
/*        
        if (! (object instanceof ActionSignalMast)) {
            throw new IllegalArgumentException("object must be an ActionSignalMast but is a: "+object.getClass().getName());
        }
        ActionSignalMast action = (ActionSignalMast)object;
        if (!signalMastBeanPanel.isEmpty()) {
            try {
                SignalMast signalMast = signalMastBeanPanel.getNamedBean();
                if (signalMast != null) {
                    NamedBeanHandle<SignalMast> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(signalMast.getDisplayName(), signalMast);
                    action.setSignalMast(handle);
                }
            } catch (JmriException ex) {
                log.error("Cannot get NamedBeanHandle for signalMast", ex);
            }
        }
        
        action.setOperationType(queryTypeComboBox.getItemAt(queryTypeComboBox.getSelectedIndex()));
        if (signalMastAspectComboBox.getItemCount() > 0) {
            action.setAspect(signalMastAspectComboBox.getItemAt(signalMastAspectComboBox.getSelectedIndex()));
        }
*/        
        
        
        
        if (! (object instanceof ActionSignalMast)) {
            throw new IllegalArgumentException("object must be an ActionSignalMast but is a: "+object.getClass().getName());
        }
        ActionSignalMast action = (ActionSignalMast)object;
        try {
            if (!signalMastBeanPanel.isEmpty() && (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastDirect)) {
                SignalMast signalMast = signalMastBeanPanel.getNamedBean();
                if (signalMast != null) {
                    NamedBeanHandle<SignalMast> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(signalMast.getDisplayName(), signalMast);
                    action.setSignalMast(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for signalMast", ex);
        }
        try {
            if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_signalMastReferenceTextField.getText());
            } else if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_signalMastLocalVariableTextField.getText());
            } else if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_signalMastFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneSignalMast has unknown selection");
            }
            
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationType((ActionSignalMast.OperationType)operationComboBox.getSelectedItem());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_signalMastOperationReferenceTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_signalMastOperationLocalVariableTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_signalMastOperationFormulaTextField.getText());
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
        return Bundle.getMessage("SignalMast_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalMastSwing_Old.class);
    
}
