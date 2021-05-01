package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionWarrant;
import jmri.jmrit.logixng.actions.ActionWarrant.DirectOperation;
import jmri.jmrit.logixng.actions.ActionWarrant.ControlAutoTrain;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionWarrant object with a Swing JPanel.
 *
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ActionWarrantSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneWarrant;
    private BeanSelectPanel<Warrant> _warrantBeanPanel;
    private JPanel _panelWarrantDirect;
    private JPanel _panelWarrantReference;
    private JPanel _panelWarrantLocalVariable;
    private JPanel _panelWarrantFormula;
    private JTextField _warrantReferenceTextField;
    private JTextField _warrantLocalVariableTextField;
    private JTextField _warrantFormulaTextField;

    private JTabbedPane _tabbedPaneOperation;
    private JComboBox<DirectOperation> _stateComboBox;
    private JPanel _panelOperationDirect;
    private JPanel _panelOperationReference;
    private JPanel _panelOperationLocalVariable;
    private JPanel _panelOperationFormula;
    private JTextField _warrantLockReferenceTextField;
    private JTextField _warrantLockLocalVariableTextField;
    private JTextField _warrantLockFormulaTextField;

    private JPanel _panelStateCombo;
    private JPanel _panelTrainIdName;
    private JPanel _panelControlTrainCombo;
    private JTextField _trainIdNameTextField;
    private JComboBox<ControlAutoTrain> _controlTrainComboBox;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionWarrant action = (ActionWarrant)object;

        panel = new JPanel();

        _tabbedPaneWarrant = new JTabbedPane();
        _panelWarrantDirect = new javax.swing.JPanel();
        _panelWarrantReference = new javax.swing.JPanel();
        _panelWarrantLocalVariable = new javax.swing.JPanel();
        _panelWarrantFormula = new javax.swing.JPanel();

        _tabbedPaneWarrant.addTab(NamedBeanAddressing.Direct.toString(), _panelWarrantDirect);
        _tabbedPaneWarrant.addTab(NamedBeanAddressing.Reference.toString(), _panelWarrantReference);
        _tabbedPaneWarrant.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelWarrantLocalVariable);
        _tabbedPaneWarrant.addTab(NamedBeanAddressing.Formula.toString(), _panelWarrantFormula);

        _warrantBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(WarrantManager.class), null);
        _panelWarrantDirect.add(_warrantBeanPanel);

        _warrantReferenceTextField = new JTextField();
        _warrantReferenceTextField.setColumns(30);
        _panelWarrantReference.add(_warrantReferenceTextField);

        _warrantLocalVariableTextField = new JTextField();
        _warrantLocalVariableTextField.setColumns(30);
        _panelWarrantLocalVariable.add(_warrantLocalVariableTextField);

        _warrantFormulaTextField = new JTextField();
        _warrantFormulaTextField.setColumns(30);
        _panelWarrantFormula.add(_warrantFormulaTextField);

        _tabbedPaneOperation = new JTabbedPane();
        _panelOperationDirect = new javax.swing.JPanel();

        _panelOperationReference = new javax.swing.JPanel();
        _panelOperationLocalVariable = new javax.swing.JPanel();
        _panelOperationFormula = new javax.swing.JPanel();

        _tabbedPaneOperation.addTab(NamedBeanAddressing.Direct.toString(), _panelOperationDirect);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Reference.toString(), _panelOperationReference);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOperationLocalVariable);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Formula.toString(), _panelOperationFormula);

        // Create an empty strut panel that sits to the left of the container
        JPanel directSpacer = new JPanel();
        directSpacer.setLayout(new BoxLayout(directSpacer, BoxLayout.Y_AXIS));
        directSpacer.add(Box.createVerticalStrut(100));

        // The container holds 3 panels, two of which are based on selections
        JPanel directContainer = new JPanel();
        directContainer.setLayout(new BoxLayout(directContainer, BoxLayout.Y_AXIS));

        _panelStateCombo = new JPanel();
        _panelTrainIdName = new JPanel();
        _panelControlTrainCombo = new JPanel();


        _panelTrainIdName.setVisible(false);
        _panelControlTrainCombo.setVisible(false);

        // Populate the container sub-panels
        _stateComboBox = new JComboBox<>();
        for (DirectOperation e : DirectOperation.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        _stateComboBox.addActionListener((java.awt.event.ActionEvent e) -> {
            setPanelDetailVisibility();
        });
        _panelStateCombo.add(_stateComboBox);

        _trainIdNameTextField = new JTextField();
        _trainIdNameTextField.setColumns(25);
        _panelTrainIdName.add(_trainIdNameTextField);

        _controlTrainComboBox = new JComboBox<>();
        for (ControlAutoTrain e : ControlAutoTrain.values()) {
            _controlTrainComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_controlTrainComboBox);
        _panelControlTrainCombo.add(_controlTrainComboBox);

        directContainer.add(_panelStateCombo);
        directContainer.add(_panelTrainIdName);
        directContainer.add(_panelControlTrainCombo);

        _panelOperationDirect.add(directSpacer);
        _panelOperationDirect.add(directContainer);
        // direct container done

        _warrantLockReferenceTextField = new JTextField();
        _warrantLockReferenceTextField.setColumns(30);
        _panelOperationReference.add(_warrantLockReferenceTextField);

        _warrantLockLocalVariableTextField = new JTextField();
        _warrantLockLocalVariableTextField.setColumns(30);
        _panelOperationLocalVariable.add(_warrantLockLocalVariableTextField);

        _warrantLockFormulaTextField = new JTextField();
        _warrantLockFormulaTextField.setColumns(30);
        _panelOperationFormula.add(_warrantLockFormulaTextField);

        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneWarrant.setSelectedComponent(_panelWarrantDirect); break;
                case Reference: _tabbedPaneWarrant.setSelectedComponent(_panelWarrantReference); break;
                case LocalVariable: _tabbedPaneWarrant.setSelectedComponent(_panelWarrantLocalVariable); break;
                case Formula: _tabbedPaneWarrant.setSelectedComponent(_panelWarrantFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getWarrant() != null) {
                _warrantBeanPanel.setDefaultNamedBean(action.getWarrant().getBean());
            }
            _warrantReferenceTextField.setText(action.getReference());
            _warrantLocalVariableTextField.setText(action.getLocalVariable());
            _warrantFormulaTextField.setText(action.getFormula());

            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperation.setSelectedComponent(_panelOperationDirect); break;
                case Reference: _tabbedPaneOperation.setSelectedComponent(_panelOperationReference); break;
                case LocalVariable: _tabbedPaneOperation.setSelectedComponent(_panelOperationLocalVariable); break;
                case Formula: _tabbedPaneOperation.setSelectedComponent(_panelOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getOperationDirect());
            setPanelDetailVisibility();
            _warrantLockReferenceTextField.setText(action.getOperationReference());
            _warrantLockLocalVariableTextField.setText(action.getOperationLocalVariable());
            _warrantLockFormulaTextField.setText(action.getLockFormula());

            _trainIdNameTextField.setText(action.getTrainIdName());
            _controlTrainComboBox.setSelectedItem(action.getControlAutoTrain());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneWarrant,
            _tabbedPaneOperation};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionWarrant_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setPanelDetailVisibility() {
        _panelTrainIdName.setVisible(false);
        _panelControlTrainCombo.setVisible(false);
        if (_stateComboBox.getSelectedItem() == DirectOperation.ControlAutoTrain) {
            _panelControlTrainCombo.setVisible(true);
        } else if (_stateComboBox.getSelectedItem() == DirectOperation.SetTrainId
                || _stateComboBox.getSelectedItem() == DirectOperation.SetTrainName) {
            _panelTrainIdName.setVisible(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionWarrant action = new ActionWarrant("IQDA1", null);

        try {
            if (_tabbedPaneWarrant.getSelectedComponent() == _panelWarrantReference) {
                action.setReference(_warrantReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationReference(_warrantLockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setFormula(_warrantFormulaTextField.getText());
            if (_tabbedPaneWarrant.getSelectedComponent() == _panelWarrantDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneWarrant.getSelectedComponent() == _panelWarrantReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneWarrant.getSelectedComponent() == _panelWarrantLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneWarrant.getSelectedComponent() == _panelWarrantFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        if (_warrantBeanPanel == null || _warrantBeanPanel.getNamedBean() == null) {
            errorMessages.add(Bundle.getMessage("ActionWarrant_ErrorWarrant"));
        }

        DirectOperation oper = _stateComboBox.getItemAt(_stateComboBox.getSelectedIndex());
        if (oper == DirectOperation.None) {
            errorMessages.add(Bundle.getMessage("ActionWarrant_ErrorOperation"));
        } else if (oper == DirectOperation.SetTrainId || oper == DirectOperation.SetTrainName) {
            if (_trainIdNameTextField.getText().isEmpty()) {
                errorMessages.add(Bundle.getMessage("ActionWarrant_ErrorConstant"));
            }
        }

        if (!errorMessages.isEmpty()) return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionWarrant action = new ActionWarrant(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionWarrant)) {
            throw new IllegalArgumentException("object must be an ActionBLock but is a: "+object.getClass().getName());
        }
        ActionWarrant action = (ActionWarrant) object;

        if (_warrantBeanPanel != null && !_warrantBeanPanel.isEmpty() && (_tabbedPaneWarrant.getSelectedComponent() == _panelWarrantDirect)) {
            Warrant warrant = _warrantBeanPanel.getNamedBean();
            if (warrant != null) {
                NamedBeanHandle<Warrant> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(warrant.getDisplayName(), warrant);
                action.setWarrant(handle);
            }
        }

        try {
            // Left panel
            if (_tabbedPaneWarrant.getSelectedComponent() == _panelWarrantDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneWarrant.getSelectedComponent() == _panelWarrantReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_warrantReferenceTextField.getText());
            } else if (_tabbedPaneWarrant.getSelectedComponent() == _panelWarrantLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_warrantLocalVariableTextField.getText());
            } else if (_tabbedPaneWarrant.getSelectedComponent() == _panelWarrantFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_warrantFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneWarrant has unknown selection");
            }

            // Right panel
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationDirect(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));

                // Handle optional data fields
                if (action.getOperationDirect() == DirectOperation.SetTrainId
                        || action.getOperationDirect() == DirectOperation.SetTrainName) {
                    action.setTrainIdName(_trainIdNameTextField.getText());
                } else if (action.getOperationDirect() == DirectOperation.ControlAutoTrain) {
                    action.setControlAutoTrain((ControlAutoTrain) _controlTrainComboBox.getSelectedItem());
                }

            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_warrantLockReferenceTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_warrantLockLocalVariableTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_warrantLockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneWarrant has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionWarrant_Short");
    }

    @Override
    public void dispose() {
        if (_warrantBeanPanel != null) {
            _warrantBeanPanel.dispose();
        }
    }


//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionWarrantSwing.class);

}
