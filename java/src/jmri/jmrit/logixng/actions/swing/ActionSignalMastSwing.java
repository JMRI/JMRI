package jmri.jmrit.logixng.actions.swing;

import java.awt.Color;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSignalMast;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionSignalMast object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionSignalMastSwing extends AbstractDigitalActionSwing {

    public static final int NUM_COLUMNS_TEXT_FIELDS = 20;

    private JTabbedPane _tabbedPaneSignalMast;
    private BeanSelectPanel<SignalMast> _signalMastBeanPanel;
    private JPanel _panelSignalMastDirect;
    private JPanel _panelSignalMastReference;
    private JPanel _panelSignalMastLocalVariable;
    private JPanel _panelSignalMastFormula;
    private JTextField _signalMastReferenceTextField;
    private JTextField _signalMastLocalVariableTextField;
    private JTextField _signalMastFormulaTextField;

    private JTabbedPane _tabbedPaneOperationType;
    private JPanel _panelOperationTypeDirect;
    private JPanel _panelOperationTypeReference;
    private JPanel _panelOperationTypeLocalVariable;
    private JPanel _panelOperationTypeFormula;

    private JComboBox<ActionSignalMast.OperationType> _operationComboBox;
    private JTextField _signalMastOperationReferenceTextField;
    private JTextField _signalMastOperationLocalVariableTextField;
    private JTextField _signalMastOperationFormulaTextField;

    private JTabbedPane _tabbedPaneAspectType;
    private JPanel _panelAspectTypeDirect;
    private JPanel _panelAspectTypeReference;
    private JPanel _panelAspectTypeLocalVariable;
    private JPanel _panelAspectTypeFormula;

    private JComboBox<String> _signalMastAspectComboBox;
    private JTextField _signalMastAspectReferenceTextField;
    private JTextField _signalMastAspectLocalVariableTextField;
    private JTextField _signalMastAspectFormulaTextField;

    private BeanSelectPanel<SignalMast> _exampleSignalMastBeanPanel;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionSignalMast action = (ActionSignalMast)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel examplePanel = new JPanel();
        JPanel innerExamplePanel = new JPanel();
        innerExamplePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        _exampleSignalMastBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(SignalMastManager.class), null);
        innerExamplePanel.add(_exampleSignalMastBeanPanel);

        _exampleSignalMastBeanPanel.getBeanCombo().addActionListener((java.awt.event.ActionEvent e) -> {
            setAspectComboBox(null);
        });


        JPanel actionPanel = new JPanel();


        // Set up tabbed pane for selecting the signal head
        _tabbedPaneSignalMast = new JTabbedPane();
        _panelSignalMastDirect = new javax.swing.JPanel();
        _panelSignalMastReference = new javax.swing.JPanel();
        _panelSignalMastLocalVariable = new javax.swing.JPanel();
        _panelSignalMastFormula = new javax.swing.JPanel();

        _tabbedPaneSignalMast.addTab(NamedBeanAddressing.Direct.toString(), _panelSignalMastDirect);
        _tabbedPaneSignalMast.addTab(NamedBeanAddressing.Reference.toString(), _panelSignalMastReference);
        _tabbedPaneSignalMast.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelSignalMastLocalVariable);
        _tabbedPaneSignalMast.addTab(NamedBeanAddressing.Formula.toString(), _panelSignalMastFormula);

        _tabbedPaneSignalMast.addChangeListener((ChangeEvent e) -> {
            setGuiEnabledStates();
        });

        _signalMastBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(SignalMastManager.class), null);
        _panelSignalMastDirect.add(_signalMastBeanPanel);

        _signalMastBeanPanel.getBeanCombo().addActionListener((java.awt.event.ActionEvent e) -> {
            setAspectComboBox(null);
        });

        _signalMastReferenceTextField = new JTextField();
        _signalMastReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelSignalMastReference.add(_signalMastReferenceTextField);

        _signalMastLocalVariableTextField = new JTextField();
        _signalMastLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelSignalMastLocalVariable.add(_signalMastLocalVariableTextField);

        _signalMastFormulaTextField = new JTextField();
        _signalMastFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelSignalMastFormula.add(_signalMastFormulaTextField);


        // Set up the tabbed pane for selecting the operation
        _tabbedPaneOperationType = new JTabbedPane();
        _panelOperationTypeDirect = new javax.swing.JPanel();
        _panelOperationTypeDirect.setLayout(new BoxLayout(_panelOperationTypeDirect, BoxLayout.Y_AXIS));
        _panelOperationTypeReference = new javax.swing.JPanel();
        _panelOperationTypeReference.setLayout(new BoxLayout(_panelOperationTypeReference, BoxLayout.Y_AXIS));
        _panelOperationTypeLocalVariable = new javax.swing.JPanel();
        _panelOperationTypeLocalVariable.setLayout(new BoxLayout(_panelOperationTypeLocalVariable, BoxLayout.Y_AXIS));
        _panelOperationTypeFormula = new javax.swing.JPanel();
        _panelOperationTypeFormula.setLayout(new BoxLayout(_panelOperationTypeFormula, BoxLayout.Y_AXIS));

        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Direct.toString(), _panelOperationTypeDirect);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Reference.toString(), _panelOperationTypeReference);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOperationTypeLocalVariable);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Formula.toString(), _panelOperationTypeFormula);

        _tabbedPaneOperationType.addChangeListener((ChangeEvent e) -> {
            setGuiEnabledStates();
        });

        _operationComboBox = new JComboBox<>();
        for (ActionSignalMast.OperationType e : ActionSignalMast.OperationType.values()) {
            _operationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_operationComboBox);

        _operationComboBox.addActionListener(e -> {
            setGuiEnabledStates();
        });

        _panelOperationTypeDirect.add(new JLabel(Bundle.getMessage("ActionSignalMast_Operation")));
        _panelOperationTypeDirect.add(_operationComboBox);

        _signalMastOperationReferenceTextField = new JTextField();
        _signalMastOperationReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeReference.add(new JLabel(Bundle.getMessage("ActionSignalMast_Operation")));
        _panelOperationTypeReference.add(_signalMastOperationReferenceTextField);

        _signalMastOperationLocalVariableTextField = new JTextField();
        _signalMastOperationLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeLocalVariable.add(new JLabel(Bundle.getMessage("ActionSignalMast_Operation")));
        _panelOperationTypeLocalVariable.add(_signalMastOperationLocalVariableTextField);

        _signalMastOperationFormulaTextField = new JTextField();
        _signalMastOperationFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeFormula.add(new JLabel(Bundle.getMessage("ActionSignalMast_Operation")));
        _panelOperationTypeFormula.add(_signalMastOperationFormulaTextField);


        // Set up the tabbed pane for selecting the appearance
        _tabbedPaneAspectType = new JTabbedPane();
        _panelAspectTypeDirect = new javax.swing.JPanel();
        _panelAspectTypeDirect.setLayout(new BoxLayout(_panelAspectTypeDirect, BoxLayout.Y_AXIS));
        _panelAspectTypeReference = new javax.swing.JPanel();
        _panelAspectTypeReference.setLayout(new BoxLayout(_panelAspectTypeReference, BoxLayout.Y_AXIS));
        _panelAspectTypeLocalVariable = new javax.swing.JPanel();
        _panelAspectTypeLocalVariable.setLayout(new BoxLayout(_panelAspectTypeLocalVariable, BoxLayout.Y_AXIS));
        _panelAspectTypeFormula = new javax.swing.JPanel();
        _panelAspectTypeFormula.setLayout(new BoxLayout(_panelAspectTypeFormula, BoxLayout.Y_AXIS));

        _tabbedPaneAspectType.addTab(NamedBeanAddressing.Direct.toString(), _panelAspectTypeDirect);
        _tabbedPaneAspectType.addTab(NamedBeanAddressing.Reference.toString(), _panelAspectTypeReference);
        _tabbedPaneAspectType.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelAspectTypeLocalVariable);
        _tabbedPaneAspectType.addTab(NamedBeanAddressing.Formula.toString(), _panelAspectTypeFormula);

        _tabbedPaneAspectType.addChangeListener((ChangeEvent e) -> {
            setGuiEnabledStates();
        });

        _signalMastAspectComboBox = new JComboBox<>();
        _panelAspectTypeDirect.add(new JLabel(Bundle.getMessage("ActionSignalMast_Aspect")));
        _panelAspectTypeDirect.add(_signalMastAspectComboBox);

        _signalMastAspectReferenceTextField = new JTextField();
        _signalMastAspectReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAspectTypeReference.add(new JLabel(Bundle.getMessage("ActionSignalMast_Aspect")));
        _panelAspectTypeReference.add(_signalMastAspectReferenceTextField);

        _signalMastAspectLocalVariableTextField = new JTextField();
        _signalMastAspectLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAspectTypeLocalVariable.add(new JLabel(Bundle.getMessage("ActionSignalMast_Aspect")));
        _panelAspectTypeLocalVariable.add(_signalMastAspectLocalVariableTextField);

        _signalMastAspectFormulaTextField = new JTextField();
        _signalMastAspectFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAspectTypeFormula.add(new JLabel(Bundle.getMessage("ActionSignalMast_Aspect")));
        _panelAspectTypeFormula.add(_signalMastAspectFormulaTextField);


        JPanel notePanel = new JPanel();
        notePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.white));
        examplePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white));

        JLabel noteLabel = new JLabel(Bundle.getMessage("SignalExampleText",
                Bundle.getMessage("SignalExampleMast"),
                Bundle.getMessage("SignalExampleAspects")));
        notePanel.add(noteLabel);


        examplePanel.add(new JLabel(Bundle.getMessage("ActionSignalMast_ExampleBean")));
        examplePanel.add(innerExamplePanel);


        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneSignalMast.setSelectedComponent(_panelSignalMastDirect); break;
                case Reference: _tabbedPaneSignalMast.setSelectedComponent(_panelSignalMastReference); break;
                case LocalVariable: _tabbedPaneSignalMast.setSelectedComponent(_panelSignalMastLocalVariable); break;
                case Formula: _tabbedPaneSignalMast.setSelectedComponent(_panelSignalMastFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getSignalMast() != null) {
                _signalMastBeanPanel.setDefaultNamedBean(action.getSignalMast().getBean());
            }
            if (action.getExampleSignalMast() != null) {
                _exampleSignalMastBeanPanel.setDefaultNamedBean(action.getExampleSignalMast().getBean());
            }
            _signalMastReferenceTextField.setText(action.getReference());
            _signalMastLocalVariableTextField.setText(action.getLocalVariable());
            _signalMastFormulaTextField.setText(action.getFormula());


            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeDirect); break;
                case Reference: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeReference); break;
                case LocalVariable: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeLocalVariable); break;
                case Formula: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getOperationAddressing().name());
            }
            _operationComboBox.setSelectedItem(action.getOperationType());
            _signalMastOperationReferenceTextField.setText(action.getOperationReference());
            _signalMastOperationLocalVariableTextField.setText(action.getOperationLocalVariable());
            _signalMastOperationFormulaTextField.setText(action.getOperationFormula());


            switch (action.getAspectAddressing()) {
                case Direct: _tabbedPaneAspectType.setSelectedComponent(_panelAspectTypeDirect); break;
                case Reference: _tabbedPaneAspectType.setSelectedComponent(_panelAspectTypeReference); break;
                case LocalVariable: _tabbedPaneAspectType.setSelectedComponent(_panelAspectTypeLocalVariable); break;
                case Formula: _tabbedPaneAspectType.setSelectedComponent(_panelAspectTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAspectAddressing().name());
            }
            _signalMastAspectReferenceTextField.setText(action.getAspectReference());
            _signalMastAspectLocalVariableTextField.setText(action.getAspectLocalVariable());
            _signalMastAspectFormulaTextField.setText(action.getAspectFormula());

            jmri.util.ThreadingUtil.runOnGUIEventually(() -> { setAspectComboBox(action); });
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneSignalMast,
            _tabbedPaneOperationType,
            _tabbedPaneAspectType
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionSignalMast_Components"), components);

        for (JComponent c : componentList) actionPanel.add(c);

        panel.add(actionPanel);
        panel.add(notePanel);
        panel.add(examplePanel);

        setGuiEnabledStates();
    }

    private void setGuiEnabledStates() {
        _tabbedPaneAspectType.setEnabled(false);
        _signalMastAspectComboBox.setEnabled(false);
        _signalMastAspectReferenceTextField.setEnabled(false);
        _signalMastAspectLocalVariableTextField.setEnabled(false);
        _signalMastAspectFormulaTextField.setEnabled(false);
        _exampleSignalMastBeanPanel.getBeanCombo().setEnabled(false);

        if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeDirect &&
                _operationComboBox.getSelectedItem() != ActionSignalMast.OperationType.Aspect) {
            return;
        }

        _tabbedPaneAspectType.setEnabled(true);

        if (_tabbedPaneSignalMast.getSelectedComponent() != _panelSignalMastDirect &&
                _tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeDirect) {
            _exampleSignalMastBeanPanel.getBeanCombo().setEnabled(true);
        }

        if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeDirect) {
            _signalMastAspectComboBox.setEnabled(true);
        }
        if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeReference) {
            _signalMastAspectReferenceTextField.setEnabled(true);
        }
        if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeLocalVariable) {
            _signalMastAspectLocalVariableTextField.setEnabled(true);
        }
        if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeFormula) {
            _signalMastAspectFormulaTextField.setEnabled(true);
        }
    }

    private void setAspectComboBox(ActionSignalMast action) {
        SignalMast sm;
        if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastDirect) {
            sm = _signalMastBeanPanel.getBeanCombo().getSelectedItem();
        } else {
            sm = _exampleSignalMastBeanPanel.getBeanCombo().getSelectedItem();
        }

        if (sm != null) {
            _signalMastAspectComboBox.removeAllItems();
            for (String aspect : sm.getValidAspects()) {
                _signalMastAspectComboBox.addItem(aspect);
                if (action != null) {
                    if (aspect.equals(action.getAspect())) _signalMastAspectComboBox.setSelectedItem(aspect);
                }
            }
            JComboBoxUtil.setupComboBoxMaxRows(_signalMastAspectComboBox);
        }
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
            return false;
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
        if (! (object instanceof ActionSignalMast)) {
            throw new IllegalArgumentException("object must be an ActionSignalMast but is a: "+object.getClass().getName());
        }
        ActionSignalMast action = (ActionSignalMast)object;
        if (_tabbedPaneSignalMast.getSelectedComponent() == _panelSignalMastDirect) {
            SignalMast signalMast = _signalMastBeanPanel.getNamedBean();
            if (signalMast != null) {
                NamedBeanHandle<SignalMast> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(signalMast.getDisplayName(), signalMast);
                action.setSignalMast(handle);
            } else {
                action.removeSignalMast();
            }
        } else {
            action.removeSignalMast();
        }

        if (!_exampleSignalMastBeanPanel.isEmpty()
                && (_tabbedPaneSignalMast.getSelectedComponent() != _panelSignalMastDirect)
                && (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeDirect)) {

            SignalMast signalMast = _exampleSignalMastBeanPanel.getNamedBean();
            if (signalMast != null) {
                NamedBeanHandle<SignalMast> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(signalMast.getDisplayName(), signalMast);
                action.setExampleSignalMast(handle);
            }
        } else {
            action.removeExampleSignalMast();
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
                action.setOperationType((ActionSignalMast.OperationType)_operationComboBox.getSelectedItem());
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

            if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeDirect) {
                action.setAspectAddressing(NamedBeanAddressing.Direct);

                if (_signalMastAspectComboBox.getItemCount() > 0) {
                    action.setAspect(_signalMastAspectComboBox
                            .getItemAt(_signalMastAspectComboBox.getSelectedIndex()));
                }
            } else if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeReference) {
                action.setAspectAddressing(NamedBeanAddressing.Reference);
                action.setAspectReference(_signalMastAspectReferenceTextField.getText());
            } else if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeLocalVariable) {
                action.setAspectAddressing(NamedBeanAddressing.LocalVariable);
                action.setAspectLocalVariable(_signalMastAspectLocalVariableTextField.getText());
            } else if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeFormula) {
                action.setAspectAddressing(NamedBeanAddressing.Formula);
                action.setAspectFormula(_signalMastAspectFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneAspectType has unknown selection");
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


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalMastSwing.class);

}
