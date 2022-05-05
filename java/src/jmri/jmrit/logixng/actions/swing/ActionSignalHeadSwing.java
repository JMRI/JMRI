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
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionSignalHead object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionSignalHeadSwing extends AbstractDigitalActionSwing {

    public static final int NUM_COLUMNS_TEXT_FIELDS = 20;

    private LogixNG_SelectNamedBeanSwing<SignalHead> _selectNamedBeanSwing;

    private JTabbedPane _tabbedPaneOperationType;
    private JPanel _panelOperationTypeDirect;
    private JPanel _panelOperationTypeReference;
    private JPanel _panelOperationTypeLocalVariable;
    private JPanel _panelOperationTypeFormula;

    private JComboBox<ActionSignalHead.OperationType> _operationComboBox;
    private JTextField _signalHeadOperationReferenceTextField;
    private JTextField _signalHeadOperationLocalVariableTextField;
    private JTextField _signalHeadOperationFormulaTextField;

    private JTabbedPane _tabbedPaneAppearanceType;
    private JPanel _panelAppearanceTypeDirect;
    private JPanel _panelAppearanceTypeReference;
    private JPanel _panelAppearanceTypeLocalVariable;
    private JPanel _panelAppearanceTypeFormula;

    private JComboBox<SignalHeadAppearance> _signalHeadAppearanceComboBox;
    private JTextField _signalHeadAppearanceReferenceTextField;
    private JTextField _signalHeadAppearanceLocalVariableTextField;
    private JTextField _signalHeadAppearanceFormulaTextField;

    private BeanSelectPanel<SignalHead> _exampleSignalHeadBeanPanel;


    public ActionSignalHeadSwing() {
    }

    public ActionSignalHeadSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionSignalHead action = (ActionSignalHead)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(SignalHeadManager.class), getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel _tabbedPaneNamedBean;

        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        JPanel examplePanel = new JPanel();
        JPanel innerExamplePanel = new JPanel();
        innerExamplePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        _exampleSignalHeadBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(SignalHeadManager.class), null);
        innerExamplePanel.add(_exampleSignalHeadBeanPanel);

        _exampleSignalHeadBeanPanel.getBeanCombo().addActionListener((java.awt.event.ActionEvent e) -> {
            setAppearanceComboBox(null);
        });


        JPanel actionPanel = new JPanel();

        _selectNamedBeanSwing.addAddressingListener((ChangeEvent e) -> {
            setGuiEnabledStates();
        });

        _selectNamedBeanSwing.getBeanSelectPanel().getBeanCombo()
                .addActionListener((java.awt.event.ActionEvent e) -> {
            setAppearanceComboBox(null);
        });

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
        for (ActionSignalHead.OperationType e : ActionSignalHead.OperationType.values()) {
            _operationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_operationComboBox);
        _operationComboBox.addActionListener(e -> {
            setGuiEnabledStates();
        });

        _panelOperationTypeDirect.add(new JLabel(Bundle.getMessage("ActionSignalHead_Operation")));
        _panelOperationTypeDirect.add(_operationComboBox);

        _signalHeadOperationReferenceTextField = new JTextField();
        _signalHeadOperationReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeReference.add(new JLabel(Bundle.getMessage("ActionSignalHead_Operation")));
        _panelOperationTypeReference.add(_signalHeadOperationReferenceTextField);

        _signalHeadOperationLocalVariableTextField = new JTextField();
        _signalHeadOperationLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeLocalVariable.add(new JLabel(Bundle.getMessage("ActionSignalHead_Operation")));
        _panelOperationTypeLocalVariable.add(_signalHeadOperationLocalVariableTextField);

        _signalHeadOperationFormulaTextField = new JTextField();
        _signalHeadOperationFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeFormula.add(new JLabel(Bundle.getMessage("ActionSignalHead_Operation")));
        _panelOperationTypeFormula.add(_signalHeadOperationFormulaTextField);


        // Set up the tabbed pane for selecting the appearance
        _tabbedPaneAppearanceType = new JTabbedPane();
        _panelAppearanceTypeDirect = new javax.swing.JPanel();
        _panelAppearanceTypeDirect.setLayout(new BoxLayout(_panelAppearanceTypeDirect, BoxLayout.Y_AXIS));
        _panelAppearanceTypeReference = new javax.swing.JPanel();
        _panelAppearanceTypeReference.setLayout(new BoxLayout(_panelAppearanceTypeReference, BoxLayout.Y_AXIS));
        _panelAppearanceTypeLocalVariable = new javax.swing.JPanel();
        _panelAppearanceTypeLocalVariable.setLayout(new BoxLayout(_panelAppearanceTypeLocalVariable, BoxLayout.Y_AXIS));
        _panelAppearanceTypeFormula = new javax.swing.JPanel();
        _panelAppearanceTypeFormula.setLayout(new BoxLayout(_panelAppearanceTypeFormula, BoxLayout.Y_AXIS));

        _tabbedPaneAppearanceType.addTab(NamedBeanAddressing.Direct.toString(), _panelAppearanceTypeDirect);
        _tabbedPaneAppearanceType.addTab(NamedBeanAddressing.Reference.toString(), _panelAppearanceTypeReference);
        _tabbedPaneAppearanceType.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelAppearanceTypeLocalVariable);
        _tabbedPaneAppearanceType.addTab(NamedBeanAddressing.Formula.toString(), _panelAppearanceTypeFormula);

        _tabbedPaneAppearanceType.addChangeListener((ChangeEvent e) -> {
            setGuiEnabledStates();
        });

        _signalHeadAppearanceComboBox = new JComboBox<>();
        _panelAppearanceTypeDirect.add(new JLabel(Bundle.getMessage("ActionSignalHead_Appearance")));
        _panelAppearanceTypeDirect.add(_signalHeadAppearanceComboBox);

        _signalHeadAppearanceReferenceTextField = new JTextField();
        _signalHeadAppearanceReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAppearanceTypeReference.add(new JLabel(Bundle.getMessage("ActionSignalHead_Appearance")));
        _panelAppearanceTypeReference.add(_signalHeadAppearanceReferenceTextField);

        _signalHeadAppearanceLocalVariableTextField = new JTextField();
        _signalHeadAppearanceLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAppearanceTypeLocalVariable.add(new JLabel(Bundle.getMessage("ActionSignalHead_Appearance")));
        _panelAppearanceTypeLocalVariable.add(_signalHeadAppearanceLocalVariableTextField);

        _signalHeadAppearanceFormulaTextField = new JTextField();
        _signalHeadAppearanceFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAppearanceTypeFormula.add(new JLabel(Bundle.getMessage("ActionSignalHead_Appearance")));
        _panelAppearanceTypeFormula.add(_signalHeadAppearanceFormulaTextField);


        JPanel notePanel = new JPanel();
        notePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.white));
        examplePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white));

        JLabel noteLabel = new JLabel(Bundle.getMessage("SignalExampleText",
                Bundle.getMessage("SignalExampleHead"),
                Bundle.getMessage("SignalExampleAppearances")));
        notePanel.add(noteLabel);


        examplePanel.add(new JLabel(Bundle.getMessage("ActionSignalHead_ExampleBean")));
        examplePanel.add(innerExamplePanel);


        if (action != null) {
            if (action.getSelectExampleNamedBean().getNamedBean() != null) {
                _exampleSignalHeadBeanPanel.setDefaultNamedBean(action.getSelectExampleNamedBean().getNamedBean().getBean());
            }

            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeDirect); break;
                case Reference: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeReference); break;
                case LocalVariable: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeLocalVariable); break;
                case Formula: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getOperationAddressing().name());
            }
            _operationComboBox.setSelectedItem(action.getOperationType());
            _signalHeadOperationReferenceTextField.setText(action.getOperationReference());
            _signalHeadOperationLocalVariableTextField.setText(action.getOperationLocalVariable());
            _signalHeadOperationFormulaTextField.setText(action.getOperationFormula());


            switch (action.getAppearanceAddressing()) {
                case Direct: _tabbedPaneAppearanceType.setSelectedComponent(_panelAppearanceTypeDirect); break;
                case Reference: _tabbedPaneAppearanceType.setSelectedComponent(_panelAppearanceTypeReference); break;
                case LocalVariable: _tabbedPaneAppearanceType.setSelectedComponent(_panelAppearanceTypeLocalVariable); break;
                case Formula: _tabbedPaneAppearanceType.setSelectedComponent(_panelAppearanceTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAppearanceAddressing().name());
            }
            _signalHeadAppearanceReferenceTextField.setText(action.getAppearanceReference());
            _signalHeadAppearanceLocalVariableTextField.setText(action.getAppearanceLocalVariable());
            _signalHeadAppearanceFormulaTextField.setText(action.getAppearanceFormula());

            jmri.util.ThreadingUtil.runOnGUIEventually(() -> { setAppearanceComboBox(action); });
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneOperationType,
            _tabbedPaneAppearanceType
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionSignalHead_Components"), components);

        for (JComponent c : componentList) actionPanel.add(c);

        panel.add(actionPanel);
        panel.add(notePanel);
        panel.add(examplePanel);

        setGuiEnabledStates();
    }


    private void setGuiEnabledStates() {
        _tabbedPaneAppearanceType.setEnabled(false);
        _signalHeadAppearanceComboBox.setEnabled(false);
        _signalHeadAppearanceReferenceTextField.setEnabled(false);
        _signalHeadAppearanceLocalVariableTextField.setEnabled(false);
        _signalHeadAppearanceFormulaTextField.setEnabled(false);
        _exampleSignalHeadBeanPanel.getBeanCombo().setEnabled(false);

        if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeDirect &&
                _operationComboBox.getSelectedItem() != ActionSignalHead.OperationType.Appearance) {
            return;
        }

        _tabbedPaneAppearanceType.setEnabled(true);

        if (_selectNamedBeanSwing.getAddressing() != NamedBeanAddressing.Direct &&
                _tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeDirect) {
            _exampleSignalHeadBeanPanel.getBeanCombo().setEnabled(true);
        }

        if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeDirect) {
            _signalHeadAppearanceComboBox.setEnabled(true);
        }
        if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeReference) {
            _signalHeadAppearanceReferenceTextField.setEnabled(true);
        }
        if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeLocalVariable) {
            _signalHeadAppearanceLocalVariableTextField.setEnabled(true);
        }
        if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeFormula) {
            _signalHeadAppearanceFormulaTextField.setEnabled(true);
        }
    }

    private void setAppearanceComboBox(ActionSignalHead action) {
        SignalHead sh;
        if (_selectNamedBeanSwing.getAddressing() == NamedBeanAddressing.Direct) {
            sh = _selectNamedBeanSwing.getBean();
        } else {
            sh = _exampleSignalHeadBeanPanel.getBeanCombo().getSelectedItem();
        }

        if (sh != null) {
            _signalHeadAppearanceComboBox.removeAllItems();
            int[] states = sh.getValidStates();
            for (int s : states) {
                SignalHeadAppearance sha = new SignalHeadAppearance();
                sha._state = s;
                sha._name = sh.getAppearanceName(s);
                _signalHeadAppearanceComboBox.addItem(sha);
                if (action != null) {
                    if (action.getAppearance() == s) _signalHeadAppearanceComboBox.setSelectedItem(sha);
                }
            }
            JComboBoxUtil.setupComboBoxMaxRows(_signalHeadAppearanceComboBox);
        }
    }


    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionSignalHead action = new ActionSignalHead("IQDA1", null);

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                action.setOperationReference(_signalHeadOperationReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        return errorMessages.isEmpty();
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
        if (! (object instanceof ActionSignalHead)) {
            throw new IllegalArgumentException("object must be an ActionSignalHead but is a: "+object.getClass().getName());
        }
        ActionSignalHead action = (ActionSignalHead)object;

        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());

        if (!_exampleSignalHeadBeanPanel.isEmpty()
                && (_selectNamedBeanSwing.getAddressing() != NamedBeanAddressing.Direct)
                && (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeDirect)) {

            SignalHead signalHead = _exampleSignalHeadBeanPanel.getNamedBean();
            if (signalHead != null) {
                NamedBeanHandle<SignalHead> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(signalHead.getDisplayName(), signalHead);
                action.getSelectExampleNamedBean().setNamedBean(handle);
            }
        } else {
            action.getSelectExampleNamedBean().removeNamedBean();
        }

        try {
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationType((ActionSignalHead.OperationType)_operationComboBox.getSelectedItem());
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

            if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeDirect) {
                action.setAppearanceAddressing(NamedBeanAddressing.Direct);

                if (_signalHeadAppearanceComboBox.getItemCount() > 0) {
                    action.setAppearance(_signalHeadAppearanceComboBox
                            .getItemAt(_signalHeadAppearanceComboBox.getSelectedIndex())._state);
                }
            } else if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeReference) {
                action.setAppearanceAddressing(NamedBeanAddressing.Reference);
                action.setAppearanceReference(_signalHeadAppearanceReferenceTextField.getText());
            } else if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeLocalVariable) {
                action.setAppearanceAddressing(NamedBeanAddressing.LocalVariable);
                action.setAppearanceLocalVariable(_signalHeadAppearanceLocalVariableTextField.getText());
            } else if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeFormula) {
                action.setAppearanceAddressing(NamedBeanAddressing.Formula);
                action.setAppearanceFormula(_signalHeadAppearanceFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneAppearanceType has unknown selection");
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalHeadSwing.class);

}
