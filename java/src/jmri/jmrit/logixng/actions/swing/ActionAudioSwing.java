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
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionAudio object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionAudioSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Audio> _selectNamedBeanSwing;

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

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(AudioManager.class), getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

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
            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneAudioOperation.setSelectedComponent(_panelAudioOperationDirect); break;
                case Reference: _tabbedPaneAudioOperation.setSelectedComponent(_panelAudioOperationReference); break;
                case LocalVariable: _tabbedPaneAudioOperation.setSelectedComponent(_panelAudioOperationLocalVariable); break;
                case Formula: _tabbedPaneAudioOperation.setSelectedComponent(_panelAudioOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getOperationAddressing().name());
            }
            _operationComboBox.setSelectedItem(action.getOperation());
            _audioOperationReferenceTextField.setText(action.getOperationReference());
            _audioOperationLocalVariableTextField.setText(action.getOperationLocalVariable());
            _audioOperationFormulaTextField.setText(action.getOperationFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
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
            if (_tabbedPaneAudioOperation.getSelectedComponent() == _panelAudioOperationReference) {
                action.setOperationReference(_audioOperationReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        return errorMessages.isEmpty();
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
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        try {
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
        // Do nothing
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionAudioSwing.class);

}
