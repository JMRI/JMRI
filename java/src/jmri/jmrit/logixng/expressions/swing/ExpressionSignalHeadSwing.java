package jmri.jmrit.logixng.expressions.swing;

import java.awt.Color;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSignalHead;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionSignalHead object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionSignalHeadSwing extends AbstractDigitalExpressionSwing {

    public static final int NUM_COLUMNS_TEXT_FIELDS = 20;

    private JTabbedPane _tabbedPaneSignalHead;
    private BeanSelectPanel<SignalHead> _signalHeadBeanPanel;
    private JPanel _panelSignalHeadDirect;
    private JPanel _panelSignalHeadReference;
    private JPanel _panelSignalHeadLocalVariable;
    private JPanel _panelSignalHeadFormula;
    private JTextField _signalHeadReferenceTextField;
    private JTextField _signalHeadLocalVariableTextField;
    private JTextField _signalHeadFormulaTextField;

    private JTabbedPane _tabbedPaneQueryType;
    private JPanel _panelQueryTypeDirect;
    private JPanel _panelQueryTypeReference;
    private JPanel _panelQueryTypeLocalVariable;
    private JPanel _panelQueryTypeFormula;

    private JComboBox<ExpressionSignalHead.QueryType> _operationComboBox;
    private JTextField _signalHeadQueryReferenceTextField;
    private JTextField _signalHeadQueryLocalVariableTextField;
    private JTextField _signalHeadQueryFormulaTextField;

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


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionSignalHead expression = (ExpressionSignalHead)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel examplePanel = new JPanel();
        JPanel innerExamplePanel = new JPanel();
        innerExamplePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        _exampleSignalHeadBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(SignalHeadManager.class), null);
        innerExamplePanel.add(_exampleSignalHeadBeanPanel);

        _exampleSignalHeadBeanPanel.getBeanCombo().addActionListener((java.awt.event.ActionEvent e) -> {
            setAppearanceComboBox(null);
        });


        JPanel expressionPanel = new JPanel();


        // Set up tabbed pane for selecting the signal head
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
            setGuiEnabledStates();
        });

        _signalHeadBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(SignalHeadManager.class), null);
        _panelSignalHeadDirect.add(_signalHeadBeanPanel);

        _signalHeadBeanPanel.getBeanCombo().addActionListener((java.awt.event.ActionEvent e) -> {
            setAppearanceComboBox(null);
        });


        _signalHeadReferenceTextField = new JTextField();
        _signalHeadReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelSignalHeadReference.add(_signalHeadReferenceTextField);

        _signalHeadLocalVariableTextField = new JTextField();
        _signalHeadLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelSignalHeadLocalVariable.add(_signalHeadLocalVariableTextField);

        _signalHeadFormulaTextField = new JTextField();
        _signalHeadFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelSignalHeadFormula.add(_signalHeadFormulaTextField);


        // Set up the tabbed pane for selecting the operation
        _tabbedPaneQueryType = new JTabbedPane();
        _panelQueryTypeDirect = new javax.swing.JPanel();
        _panelQueryTypeDirect.setLayout(new BoxLayout(_panelQueryTypeDirect, BoxLayout.Y_AXIS));
        _panelQueryTypeReference = new javax.swing.JPanel();
        _panelQueryTypeReference.setLayout(new BoxLayout(_panelQueryTypeReference, BoxLayout.Y_AXIS));
        _panelQueryTypeLocalVariable = new javax.swing.JPanel();
        _panelQueryTypeLocalVariable.setLayout(new BoxLayout(_panelQueryTypeLocalVariable, BoxLayout.Y_AXIS));
        _panelQueryTypeFormula = new javax.swing.JPanel();
        _panelQueryTypeFormula.setLayout(new BoxLayout(_panelQueryTypeFormula, BoxLayout.Y_AXIS));

        _tabbedPaneQueryType.addTab(NamedBeanAddressing.Direct.toString(), _panelQueryTypeDirect);
        _tabbedPaneQueryType.addTab(NamedBeanAddressing.Reference.toString(), _panelQueryTypeReference);
        _tabbedPaneQueryType.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelQueryTypeLocalVariable);
        _tabbedPaneQueryType.addTab(NamedBeanAddressing.Formula.toString(), _panelQueryTypeFormula);

        _tabbedPaneQueryType.addChangeListener((ChangeEvent e) -> {
            setGuiEnabledStates();
        });

        _operationComboBox = new JComboBox<>();
        for (ExpressionSignalHead.QueryType e : ExpressionSignalHead.QueryType.values()) {
            _operationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_operationComboBox);

        _operationComboBox.addActionListener(e -> {
            setGuiEnabledStates();
        });

        _panelQueryTypeDirect.add(new JLabel(Bundle.getMessage("ExpressionSignalHead_Query")));
        _panelQueryTypeDirect.add(_operationComboBox);

        _signalHeadQueryReferenceTextField = new JTextField();
        _signalHeadQueryReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelQueryTypeReference.add(new JLabel(Bundle.getMessage("ExpressionSignalHead_Query")));
        _panelQueryTypeReference.add(_signalHeadQueryReferenceTextField);

        _signalHeadQueryLocalVariableTextField = new JTextField();
        _signalHeadQueryLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelQueryTypeLocalVariable.add(new JLabel(Bundle.getMessage("ExpressionSignalHead_Query")));
        _panelQueryTypeLocalVariable.add(_signalHeadQueryLocalVariableTextField);

        _signalHeadQueryFormulaTextField = new JTextField();
        _signalHeadQueryFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelQueryTypeFormula.add(new JLabel(Bundle.getMessage("ExpressionSignalHead_Query")));
        _panelQueryTypeFormula.add(_signalHeadQueryFormulaTextField);


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
        _panelAppearanceTypeDirect.add(new JLabel(Bundle.getMessage("ExpressionSignalHead_Appearance")));
        _panelAppearanceTypeDirect.add(_signalHeadAppearanceComboBox);

        _signalHeadAppearanceReferenceTextField = new JTextField();
        _signalHeadAppearanceReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAppearanceTypeReference.add(new JLabel(Bundle.getMessage("ExpressionSignalHead_Appearance")));
        _panelAppearanceTypeReference.add(_signalHeadAppearanceReferenceTextField);

        _signalHeadAppearanceLocalVariableTextField = new JTextField();
        _signalHeadAppearanceLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAppearanceTypeLocalVariable.add(new JLabel(Bundle.getMessage("ExpressionSignalHead_Appearance")));
        _panelAppearanceTypeLocalVariable.add(_signalHeadAppearanceLocalVariableTextField);

        _signalHeadAppearanceFormulaTextField = new JTextField();
        _signalHeadAppearanceFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAppearanceTypeFormula.add(new JLabel(Bundle.getMessage("ExpressionSignalHead_Appearance")));
        _panelAppearanceTypeFormula.add(_signalHeadAppearanceFormulaTextField);


        JPanel notePanel = new JPanel();
        notePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.white));
        examplePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white));

        JLabel noteLabel = new JLabel(Bundle.getMessage("SignalExampleText",
                Bundle.getMessage("SignalExampleHead"),
                Bundle.getMessage("SignalExampleAppearances")));
        notePanel.add(noteLabel);


        examplePanel.add(new JLabel(Bundle.getMessage("ExpressionSignalHead_ExampleBean")));
        examplePanel.add(innerExamplePanel);


        if (expression != null) {
            switch (expression.getAddressing()) {
                case Direct: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadDirect); break;
                case Reference: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadReference); break;
                case LocalVariable: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadLocalVariable); break;
                case Formula: _tabbedPaneSignalHead.setSelectedComponent(_panelSignalHeadFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            if (expression.getSignalHead() != null) {
                _signalHeadBeanPanel.setDefaultNamedBean(expression.getSignalHead().getBean());
            }
            if (expression.getExampleSignalHead() != null) {
                _exampleSignalHeadBeanPanel.setDefaultNamedBean(expression.getExampleSignalHead().getBean());
            }
            _signalHeadReferenceTextField.setText(expression.getReference());
            _signalHeadLocalVariableTextField.setText(expression.getLocalVariable());
            _signalHeadFormulaTextField.setText(expression.getFormula());


            switch (expression.getQueryAddressing()) {
                case Direct: _tabbedPaneQueryType.setSelectedComponent(_panelQueryTypeDirect); break;
                case Reference: _tabbedPaneQueryType.setSelectedComponent(_panelQueryTypeReference); break;
                case LocalVariable: _tabbedPaneQueryType.setSelectedComponent(_panelQueryTypeLocalVariable); break;
                case Formula: _tabbedPaneQueryType.setSelectedComponent(_panelQueryTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getQueryAddressing().name());
            }
            _operationComboBox.setSelectedItem(expression.getQueryType());
            _signalHeadQueryReferenceTextField.setText(expression.getQueryReference());
            _signalHeadQueryLocalVariableTextField.setText(expression.getQueryLocalVariable());
            _signalHeadQueryFormulaTextField.setText(expression.getQueryFormula());


            switch (expression.getAppearanceAddressing()) {
                case Direct: _tabbedPaneAppearanceType.setSelectedComponent(_panelAppearanceTypeDirect); break;
                case Reference: _tabbedPaneAppearanceType.setSelectedComponent(_panelAppearanceTypeReference); break;
                case LocalVariable: _tabbedPaneAppearanceType.setSelectedComponent(_panelAppearanceTypeLocalVariable); break;
                case Formula: _tabbedPaneAppearanceType.setSelectedComponent(_panelAppearanceTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAppearanceAddressing().name());
            }
            _signalHeadAppearanceReferenceTextField.setText(expression.getAppearanceReference());
            _signalHeadAppearanceLocalVariableTextField.setText(expression.getAppearanceLocalVariable());
            _signalHeadAppearanceFormulaTextField.setText(expression.getAppearanceFormula());

            jmri.util.ThreadingUtil.runOnGUIEventually(() -> { setAppearanceComboBox(expression); });
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneSignalHead,
            _tabbedPaneQueryType,
            _tabbedPaneAppearanceType
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionSignalHead_Components"), components);

        for (JComponent c : componentList) expressionPanel.add(c);

        panel.add(expressionPanel);
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

        if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeDirect &&
                _operationComboBox.getSelectedItem() != ExpressionSignalHead.QueryType.Appearance &&
                _operationComboBox.getSelectedItem() != ExpressionSignalHead.QueryType.NotAppearance) {
            return;
        }

        _tabbedPaneAppearanceType.setEnabled(true);

        if (_tabbedPaneSignalHead.getSelectedComponent() != _panelSignalHeadDirect &&
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

    private void setAppearanceComboBox(ExpressionSignalHead expression) {
        SignalHead sh;
        if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadDirect) {
            sh = (SignalHead) _signalHeadBeanPanel.getBeanCombo().getSelectedItem();
        } else {
            sh = (SignalHead) _exampleSignalHeadBeanPanel.getBeanCombo().getSelectedItem();
        }

        if (sh != null) {
            _signalHeadAppearanceComboBox.removeAllItems();
            int[] states = sh.getValidStates();
            for (int s : states) {
                SignalHeadAppearance sha = new SignalHeadAppearance();
                sha._state = s;
                sha._name = sh.getAppearanceName(s);
                _signalHeadAppearanceComboBox.addItem(sha);
                if (expression != null) {
                    if (expression.getAppearance() == s) _signalHeadAppearanceComboBox.setSelectedItem(sha);
                }
            }
            JComboBoxUtil.setupComboBoxMaxRows(_signalHeadAppearanceComboBox);
        }
    }


    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionSignalHead expression = new ExpressionSignalHead("IQDE1", null);

        try {
            if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadReference) {
                expression.setReference(_signalHeadReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeReference) {
                expression.setQueryReference(_signalHeadQueryReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            expression.setFormula(_signalHeadFormulaTextField.getText());
            if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
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
        ExpressionSignalHead expression = new ExpressionSignalHead(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSignalHead)) {
            throw new IllegalArgumentException("object must be an ExpressionSignalHead but is a: "+object.getClass().getName());
        }
        ExpressionSignalHead expression = (ExpressionSignalHead)object;
        if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadDirect) {
            SignalHead signalHead = _signalHeadBeanPanel.getNamedBean();
            if (signalHead != null) {
                NamedBeanHandle<SignalHead> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(signalHead.getDisplayName(), signalHead);
                expression.setSignalHead(handle);
            } else {
                expression.removeSignalHead();
            }
        } else {
            expression.removeSignalHead();
        }

        if (!_exampleSignalHeadBeanPanel.isEmpty()
                && (_tabbedPaneSignalHead.getSelectedComponent() != _panelSignalHeadDirect)
                && (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeDirect)) {

            SignalHead signalHead = _exampleSignalHeadBeanPanel.getNamedBean();
            if (signalHead != null) {
                NamedBeanHandle<SignalHead> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(signalHead.getDisplayName(), signalHead);
                expression.setExampleSignalHead(handle);
            }
        } else {
            expression.removeExampleSignalHead();
        }

        try {
            if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
                expression.setReference(_signalHeadReferenceTextField.getText());
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
                expression.setLocalVariable(_signalHeadLocalVariableTextField.getText());
            } else if (_tabbedPaneSignalHead.getSelectedComponent() == _panelSignalHeadFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
                expression.setFormula(_signalHeadFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneSignalHead has unknown selection");
            }

            if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeDirect) {
                expression.setQueryAddressing(NamedBeanAddressing.Direct);
                expression.setQueryType((ExpressionSignalHead.QueryType)_operationComboBox.getSelectedItem());
            } else if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeReference) {
                expression.setQueryAddressing(NamedBeanAddressing.Reference);
                expression.setQueryReference(_signalHeadQueryReferenceTextField.getText());
            } else if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeLocalVariable) {
                expression.setQueryAddressing(NamedBeanAddressing.LocalVariable);
                expression.setQueryLocalVariable(_signalHeadQueryLocalVariableTextField.getText());
            } else if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeFormula) {
                expression.setQueryAddressing(NamedBeanAddressing.Formula);
                expression.setQueryFormula(_signalHeadQueryFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneQueryType has unknown selection");
            }

            if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeDirect) {
                expression.setAppearanceAddressing(NamedBeanAddressing.Direct);

                if (_signalHeadAppearanceComboBox.getItemCount() > 0) {
                    expression.setAppearance(_signalHeadAppearanceComboBox
                            .getItemAt(_signalHeadAppearanceComboBox.getSelectedIndex())._state);
                }
            } else if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeReference) {
                expression.setAppearanceAddressing(NamedBeanAddressing.Reference);
                expression.setAppearanceReference(_signalHeadAppearanceReferenceTextField.getText());
            } else if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeLocalVariable) {
                expression.setAppearanceAddressing(NamedBeanAddressing.LocalVariable);
                expression.setAppearanceLocalVariable(_signalHeadAppearanceLocalVariableTextField.getText());
            } else if (_tabbedPaneAppearanceType.getSelectedComponent() == _panelAppearanceTypeFormula) {
                expression.setAppearanceAddressing(NamedBeanAddressing.Formula);
                expression.setAppearanceFormula(_signalHeadAppearanceFormulaTextField.getText());
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalHeadSwing.class);

}
