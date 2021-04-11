package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.TriggerRoute;
import jmri.jmrit.logixng.actions.TriggerRoute.Operation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an TriggerRoute object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class TriggerRouteSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneRoute;
    private BeanSelectPanel<Route> routeBeanPanel;
    private JPanel _panelRouteDirect;
    private JPanel _panelRouteReference;
    private JPanel _panelRouteLocalVariable;
    private JPanel _panelRouteFormula;
    private JTextField _routeReferenceTextField;
    private JTextField _routeLocalVariableTextField;
    private JTextField _routeFormulaTextField;
    
    private JTabbedPane _tabbedPaneOperation;
    private JComboBox<Operation> _stateComboBox;
    private JPanel _panelOperationDirect;
    private JPanel _panelOperationReference;
    private JPanel _panelOperationLocalVariable;
    private JPanel _panelOperationFormula;
    private JTextField _routeLockReferenceTextField;
    private JTextField _routeLockLocalVariableTextField;
    private JTextField _routeLockFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        TriggerRoute action = (TriggerRoute)object;
        
        panel = new JPanel();
        
        _tabbedPaneRoute = new JTabbedPane();
        _panelRouteDirect = new javax.swing.JPanel();
        _panelRouteReference = new javax.swing.JPanel();
        _panelRouteLocalVariable = new javax.swing.JPanel();
        _panelRouteFormula = new javax.swing.JPanel();
        
        _tabbedPaneRoute.addTab(NamedBeanAddressing.Direct.toString(), _panelRouteDirect);
        _tabbedPaneRoute.addTab(NamedBeanAddressing.Reference.toString(), _panelRouteReference);
        _tabbedPaneRoute.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelRouteLocalVariable);
        _tabbedPaneRoute.addTab(NamedBeanAddressing.Formula.toString(), _panelRouteFormula);
        
        routeBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(RouteManager.class), null);
        _panelRouteDirect.add(routeBeanPanel);
        
        _routeReferenceTextField = new JTextField();
        _routeReferenceTextField.setColumns(30);
        _panelRouteReference.add(_routeReferenceTextField);
        
        _routeLocalVariableTextField = new JTextField();
        _routeLocalVariableTextField.setColumns(30);
        _panelRouteLocalVariable.add(_routeLocalVariableTextField);
        
        _routeFormulaTextField = new JTextField();
        _routeFormulaTextField.setColumns(30);
        _panelRouteFormula.add(_routeFormulaTextField);
        
        
        _tabbedPaneOperation = new JTabbedPane();
        _panelOperationDirect = new javax.swing.JPanel();
        _panelOperationReference = new javax.swing.JPanel();
        _panelOperationLocalVariable = new javax.swing.JPanel();
        _panelOperationFormula = new javax.swing.JPanel();
        
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Direct.toString(), _panelOperationDirect);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Reference.toString(), _panelOperationReference);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOperationLocalVariable);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Formula.toString(), _panelOperationFormula);
        
        _stateComboBox = new JComboBox<>();
        for (Operation e : Operation.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        
        _panelOperationDirect.add(_stateComboBox);
        
        _routeLockReferenceTextField = new JTextField();
        _routeLockReferenceTextField.setColumns(30);
        _panelOperationReference.add(_routeLockReferenceTextField);
        
        _routeLockLocalVariableTextField = new JTextField();
        _routeLockLocalVariableTextField.setColumns(30);
        _panelOperationLocalVariable.add(_routeLockLocalVariableTextField);
        
        _routeLockFormulaTextField = new JTextField();
        _routeLockFormulaTextField.setColumns(30);
        _panelOperationFormula.add(_routeLockFormulaTextField);
        
        
        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneRoute.setSelectedComponent(_panelRouteDirect); break;
                case Reference: _tabbedPaneRoute.setSelectedComponent(_panelRouteReference); break;
                case LocalVariable: _tabbedPaneRoute.setSelectedComponent(_panelRouteLocalVariable); break;
                case Formula: _tabbedPaneRoute.setSelectedComponent(_panelRouteFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getRoute() != null) {
                routeBeanPanel.setDefaultNamedBean(action.getRoute().getBean());
            }
            _routeReferenceTextField.setText(action.getReference());
            _routeLocalVariableTextField.setText(action.getLocalVariable());
            _routeFormulaTextField.setText(action.getFormula());
            
            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperation.setSelectedComponent(_panelOperationDirect); break;
                case Reference: _tabbedPaneOperation.setSelectedComponent(_panelOperationReference); break;
                case LocalVariable: _tabbedPaneOperation.setSelectedComponent(_panelOperationLocalVariable); break;
                case Formula: _tabbedPaneOperation.setSelectedComponent(_panelOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getOperationDirect());
            _routeLockReferenceTextField.setText(action.getOperationReference());
            _routeLockLocalVariableTextField.setText(action.getOperationLocalVariable());
            _routeLockFormulaTextField.setText(action.getLockFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneRoute,
            _tabbedPaneOperation};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("TriggerRoute_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        TriggerRoute action = new TriggerRoute("IQDA1", null);
        
        try {
            if (_tabbedPaneRoute.getSelectedComponent() == _panelRouteReference) {
                action.setReference(_routeReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationReference(_routeLockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setFormula(_routeFormulaTextField.getText());
            if (_tabbedPaneRoute.getSelectedComponent() == _panelRouteDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneRoute.getSelectedComponent() == _panelRouteReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneRoute.getSelectedComponent() == _panelRouteLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneRoute.getSelectedComponent() == _panelRouteFormula) {
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
        TriggerRoute action = new TriggerRoute(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof TriggerRoute)) {
            throw new IllegalArgumentException("object must be an TriggerRoute but is a: "+object.getClass().getName());
        }
        TriggerRoute action = (TriggerRoute)object;
        if (!routeBeanPanel.isEmpty() && (_tabbedPaneRoute.getSelectedComponent() == _panelRouteDirect)) {
            Route route = routeBeanPanel.getNamedBean();
            if (route != null) {
                NamedBeanHandle<Route> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(route.getDisplayName(), route);
                action.setRoute(handle);
            }
        }
        try {
            if (_tabbedPaneRoute.getSelectedComponent() == _panelRouteDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneRoute.getSelectedComponent() == _panelRouteReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_routeReferenceTextField.getText());
            } else if (_tabbedPaneRoute.getSelectedComponent() == _panelRouteLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_routeLocalVariableTextField.getText());
            } else if (_tabbedPaneRoute.getSelectedComponent() == _panelRouteFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_routeFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneRoute has unknown selection");
            }
            
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationDirect(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_routeLockReferenceTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_routeLockLocalVariableTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_routeLockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneRoute has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("TriggerRoute_Short");
    }
    
    @Override
    public void dispose() {
        if (routeBeanPanel != null) {
            routeBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TriggerRouteSwing.class);
    
}
