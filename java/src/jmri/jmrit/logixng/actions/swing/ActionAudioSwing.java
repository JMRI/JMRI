package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Audio;
import jmri.AudioManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAudio;
import jmri.jmrit.logixng.actions.ActionAudio.Operation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionAudio object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionAudioSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneAudio;
    private BeanSelectPanel<Audio> audioBeanPanel;
    private JPanel _panelAudioDirect;
    private JPanel _panelAudioReference;
    private JPanel _panelAudioLocalVariable;
    private JPanel _panelAudioFormula;
    private JTextField _audioReferenceTextField;
    private JTextField _audioLocalVariableTextField;
    private JTextField _audioFormulaTextField;
    
    private JTabbedPane _tabbedPaneAudioOperation;
    private JComboBox<Operation> _operationComboBox;
    private JPanel _panelAudioOperationDirect;
    private JPanel _panelAudioOperationReference;
    private JPanel _panelAudioOperationLocalVariable;
    private JPanel _panelAudioOperationFormula;
    private JTextField _audioOperationReferenceTextField;
    private JTextField _audioOperationLocalVariableTextField;
    private JTextField _audioOperationFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionAudio action = (ActionAudio)object;
        
        panel = new JPanel();
        
        _tabbedPaneAudio = new JTabbedPane();
        _panelAudioDirect = new javax.swing.JPanel();
        _panelAudioReference = new javax.swing.JPanel();
        _panelAudioLocalVariable = new javax.swing.JPanel();
        _panelAudioFormula = new javax.swing.JPanel();
        
        _tabbedPaneAudio.addTab(NamedBeanAddressing.Direct.toString(), _panelAudioDirect);
        _tabbedPaneAudio.addTab(NamedBeanAddressing.Reference.toString(), _panelAudioReference);
        _tabbedPaneAudio.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelAudioLocalVariable);
        _tabbedPaneAudio.addTab(NamedBeanAddressing.Formula.toString(), _panelAudioFormula);
        
        audioBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(AudioManager.class), null);
        _panelAudioDirect.add(audioBeanPanel);
        
        _audioReferenceTextField = new JTextField();
        _audioReferenceTextField.setColumns(30);
        _panelAudioReference.add(_audioReferenceTextField);
        
        _audioLocalVariableTextField = new JTextField();
        _audioLocalVariableTextField.setColumns(30);
        _panelAudioLocalVariable.add(_audioLocalVariableTextField);
        
        _audioFormulaTextField = new JTextField();
        _audioFormulaTextField.setColumns(30);
        _panelAudioFormula.add(_audioFormulaTextField);
        
        
        _tabbedPaneAudioOperation = new JTabbedPane();
        _panelAudioOperationDirect = new javax.swing.JPanel();
        _panelAudioOperationReference = new javax.swing.JPanel();
        _panelAudioOperationLocalVariable = new javax.swing.JPanel();
        _panelAudioOperationFormula = new javax.swing.JPanel();
        
        _tabbedPaneAudioOperation.addTab(NamedBeanAddressing.Direct.toString(), _panelAudioOperationDirect);
        _tabbedPaneAudioOperation.addTab(NamedBeanAddressing.Reference.toString(), _panelAudioOperationReference);
        _tabbedPaneAudioOperation.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelAudioOperationLocalVariable);
        _tabbedPaneAudioOperation.addTab(NamedBeanAddressing.Formula.toString(), _panelAudioOperationFormula);
        
        _operationComboBox = new JComboBox<>();
        for (Operation e : Operation.values()) {
            _operationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_operationComboBox);
        
        _panelAudioOperationDirect.add(_operationComboBox);
        
        _audioOperationReferenceTextField = new JTextField();
        _audioOperationReferenceTextField.setColumns(30);
        _panelAudioOperationReference.add(_audioOperationReferenceTextField);
        
        _audioOperationLocalVariableTextField = new JTextField();
        _audioOperationLocalVariableTextField.setColumns(30);
        _panelAudioOperationLocalVariable.add(_audioOperationLocalVariableTextField);
        
        _audioOperationFormulaTextField = new JTextField();
        _audioOperationFormulaTextField.setColumns(30);
        _panelAudioOperationFormula.add(_audioOperationFormulaTextField);
        
        
        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneAudio.setSelectedComponent(_panelAudioDirect); break;
                case Reference: _tabbedPaneAudio.setSelectedComponent(_panelAudioReference); break;
                case LocalVariable: _tabbedPaneAudio.setSelectedComponent(_panelAudioLocalVariable); break;
                case Formula: _tabbedPaneAudio.setSelectedComponent(_panelAudioFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getAudio() != null) {
                audioBeanPanel.setDefaultNamedBean(action.getAudio().getBean());
            }
            _audioReferenceTextField.setText(action.getReference());
            _audioLocalVariableTextField.setText(action.getLocalVariable());
            _audioFormulaTextField.setText(action.getFormula());
            
            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneAudioOperation.setSelectedComponent(_panelAudioOperationDirect); break;
                case Reference: _tabbedPaneAudioOperation.setSelectedComponent(_panelAudioOperationReference); break;
                case LocalVariable: _tabbedPaneAudioOperation.setSelectedComponent(_panelAudioOperationLocalVariable); break;
                case Formula: _tabbedPaneAudioOperation.setSelectedComponent(_panelAudioOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _operationComboBox.setSelectedItem(action.getOperation());
            _audioOperationReferenceTextField.setText(action.getOperationReference());
            _audioOperationLocalVariableTextField.setText(action.getOperationLocalVariable());
            _audioOperationFormulaTextField.setText(action.getOperationFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneAudio,
            _tabbedPaneAudioOperation};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionAudio_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionAudio action = new ActionAudio("IQDA1", null);
        
        try {
            if (_tabbedPaneAudio.getSelectedComponent() == _panelAudioReference) {
                action.setReference(_audioReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneAudioOperation.getSelectedComponent() == _panelAudioOperationReference) {
                action.setOperationReference(_audioOperationReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setFormula(_audioFormulaTextField.getText());
            if (_tabbedPaneAudio.getSelectedComponent() == _panelAudioDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneAudio.getSelectedComponent() == _panelAudioReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneAudio.getSelectedComponent() == _panelAudioLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneAudio.getSelectedComponent() == _panelAudioFormula) {
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
        ActionAudio action = new ActionAudio(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionAudio)) {
            throw new IllegalArgumentException("object must be an ActionAudio but is a: "+object.getClass().getName());
        }
        ActionAudio action = (ActionAudio)object;
        if (_tabbedPaneAudio.getSelectedComponent() == _panelAudioDirect) {
            Audio audio = audioBeanPanel.getNamedBean();
            if (audio != null) {
                NamedBeanHandle<Audio> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(audio.getDisplayName(), audio);
                action.setAudio(handle);
            } else {
                action.removeAudio();
            }
        } else {
            action.removeAudio();
        }
        try {
            if (_tabbedPaneAudio.getSelectedComponent() == _panelAudioDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneAudio.getSelectedComponent() == _panelAudioReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_audioReferenceTextField.getText());
            } else if (_tabbedPaneAudio.getSelectedComponent() == _panelAudioLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_audioLocalVariableTextField.getText());
            } else if (_tabbedPaneAudio.getSelectedComponent() == _panelAudioFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_audioFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneAudio has unknown selection");
            }
            
            if (_tabbedPaneAudioOperation.getSelectedComponent() == _panelAudioOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperation(_operationComboBox.getItemAt(_operationComboBox.getSelectedIndex()));
            } else if (_tabbedPaneAudioOperation.getSelectedComponent() == _panelAudioOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_audioOperationReferenceTextField.getText());
            } else if (_tabbedPaneAudioOperation.getSelectedComponent() == _panelAudioOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_audioOperationLocalVariableTextField.getText());
            } else if (_tabbedPaneAudioOperation.getSelectedComponent() == _panelAudioOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_audioOperationFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneAudioOperation has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionAudio_Short");
    }
    
    @Override
    public void dispose() {
        if (audioBeanPanel != null) {
            audioBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionAudioSwing.class);
    
}
