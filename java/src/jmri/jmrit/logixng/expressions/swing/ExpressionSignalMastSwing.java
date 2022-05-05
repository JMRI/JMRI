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
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSignalMast;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionSignalMast object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionSignalMastSwing extends AbstractDigitalExpressionSwing {

    public static final int NUM_COLUMNS_TEXT_FIELDS = 20;

    private LogixNG_SelectNamedBeanSwing<SignalMast> _selectNamedBeanSwing;

    private JTabbedPane _tabbedPaneQueryType;
    private JPanel _panelQueryTypeDirect;
    private JPanel _panelQueryTypeReference;
    private JPanel _panelQueryTypeLocalVariable;
    private JPanel _panelQueryTypeFormula;

    private JComboBox<ExpressionSignalMast.QueryType> _operationComboBox;
    private JTextField _signalMastQueryReferenceTextField;
    private JTextField _signalMastQueryLocalVariableTextField;
    private JTextField _signalMastQueryFormulaTextField;

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


    public ExpressionSignalMastSwing() {
    }

    public ExpressionSignalMastSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionSignalMast expression = (ExpressionSignalMast)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(SignalMastManager.class), getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel _tabbedPaneNamedBean;

        if (expression != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        JPanel examplePanel = new JPanel();
        JPanel innerExamplePanel = new JPanel();
        innerExamplePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        _exampleSignalMastBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(SignalMastManager.class), null);
        innerExamplePanel.add(_exampleSignalMastBeanPanel);

        _exampleSignalMastBeanPanel.getBeanCombo().addActionListener((java.awt.event.ActionEvent e) -> {
            setAspectComboBox(null);
        });


        JPanel expressionPanel = new JPanel();


        _selectNamedBeanSwing.addAddressingListener((ChangeEvent e) -> {
            setGuiEnabledStates();
        });

        _selectNamedBeanSwing.getBeanSelectPanel().getBeanCombo()
                .addActionListener((java.awt.event.ActionEvent e) -> {
            setAspectComboBox(null);
        });

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
        for (ExpressionSignalMast.QueryType e : ExpressionSignalMast.QueryType.values()) {
            _operationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_operationComboBox);

        _operationComboBox.addActionListener(e -> {
            setGuiEnabledStates();
        });

        _panelQueryTypeDirect.add(new JLabel(Bundle.getMessage("ExpressionSignalMast_Query")));
        _panelQueryTypeDirect.add(_operationComboBox);

        _signalMastQueryReferenceTextField = new JTextField();
        _signalMastQueryReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelQueryTypeReference.add(new JLabel(Bundle.getMessage("ExpressionSignalMast_Query")));
        _panelQueryTypeReference.add(_signalMastQueryReferenceTextField);

        _signalMastQueryLocalVariableTextField = new JTextField();
        _signalMastQueryLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelQueryTypeLocalVariable.add(new JLabel(Bundle.getMessage("ExpressionSignalMast_Query")));
        _panelQueryTypeLocalVariable.add(_signalMastQueryLocalVariableTextField);

        _signalMastQueryFormulaTextField = new JTextField();
        _signalMastQueryFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelQueryTypeFormula.add(new JLabel(Bundle.getMessage("ExpressionSignalMast_Query")));
        _panelQueryTypeFormula.add(_signalMastQueryFormulaTextField);


        // Set up the tabbed pane for selecting the aspect
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
        _panelAspectTypeDirect.add(new JLabel(Bundle.getMessage("ExpressionSignalMast_Aspect")));
        _panelAspectTypeDirect.add(_signalMastAspectComboBox);

        _signalMastAspectReferenceTextField = new JTextField();
        _signalMastAspectReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAspectTypeReference.add(new JLabel(Bundle.getMessage("ExpressionSignalMast_Aspect")));
        _panelAspectTypeReference.add(_signalMastAspectReferenceTextField);

        _signalMastAspectLocalVariableTextField = new JTextField();
        _signalMastAspectLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAspectTypeLocalVariable.add(new JLabel(Bundle.getMessage("ExpressionSignalMast_Aspect")));
        _panelAspectTypeLocalVariable.add(_signalMastAspectLocalVariableTextField);

        _signalMastAspectFormulaTextField = new JTextField();
        _signalMastAspectFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelAspectTypeFormula.add(new JLabel(Bundle.getMessage("ExpressionSignalMast_Aspect")));
        _panelAspectTypeFormula.add(_signalMastAspectFormulaTextField);


        JPanel notePanel = new JPanel();
        notePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.white));
        examplePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.white));

        JLabel noteLabel = new JLabel(Bundle.getMessage("SignalExampleText",
                Bundle.getMessage("SignalExampleMast"),
                Bundle.getMessage("SignalExampleAspects")));
        notePanel.add(noteLabel);


        examplePanel.add(new JLabel(Bundle.getMessage("ExpressionSignalMast_ExampleBean")));
        examplePanel.add(innerExamplePanel);


        if (expression != null) {
            if (expression.getSelectExampleNamedBean().getNamedBean() != null) {
                _exampleSignalMastBeanPanel.setDefaultNamedBean(expression.getSelectExampleNamedBean().getNamedBean().getBean());
            }

            switch (expression.getQueryAddressing()) {
                case Direct: _tabbedPaneQueryType.setSelectedComponent(_panelQueryTypeDirect); break;
                case Reference: _tabbedPaneQueryType.setSelectedComponent(_panelQueryTypeReference); break;
                case LocalVariable: _tabbedPaneQueryType.setSelectedComponent(_panelQueryTypeLocalVariable); break;
                case Formula: _tabbedPaneQueryType.setSelectedComponent(_panelQueryTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getQueryAddressing().name());
            }
            _operationComboBox.setSelectedItem(expression.getQueryType());
            _signalMastQueryReferenceTextField.setText(expression.getQueryReference());
            _signalMastQueryLocalVariableTextField.setText(expression.getQueryLocalVariable());
            _signalMastQueryFormulaTextField.setText(expression.getQueryFormula());



            switch (expression.getAspectAddressing()) {
                case Direct: _tabbedPaneAspectType.setSelectedComponent(_panelAspectTypeDirect); break;
                case Reference: _tabbedPaneAspectType.setSelectedComponent(_panelAspectTypeReference); break;
                case LocalVariable: _tabbedPaneAspectType.setSelectedComponent(_panelAspectTypeLocalVariable); break;
                case Formula: _tabbedPaneAspectType.setSelectedComponent(_panelAspectTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAspectAddressing().name());
            }
            _signalMastAspectReferenceTextField.setText(expression.getAspectReference());
            _signalMastAspectLocalVariableTextField.setText(expression.getAspectLocalVariable());
            _signalMastAspectFormulaTextField.setText(expression.getAspectFormula());

            jmri.util.ThreadingUtil.runOnGUIEventually(() -> { setAspectComboBox(expression); });
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneQueryType,
            _tabbedPaneAspectType
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionSignalMast_Components"), components);

        for (JComponent c : componentList) expressionPanel.add(c);

        panel.add(expressionPanel);
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

        if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeDirect &&
                _operationComboBox.getSelectedItem() != ExpressionSignalMast.QueryType.Aspect &&
                _operationComboBox.getSelectedItem() != ExpressionSignalMast.QueryType.NotAspect) {
            return;
        }

        _tabbedPaneAspectType.setEnabled(true);

        if (_selectNamedBeanSwing.getAddressing() != NamedBeanAddressing.Direct &&
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

    private void setAspectComboBox(ExpressionSignalMast expression) {
        SignalMast sm;
        if (_selectNamedBeanSwing.getAddressing() == NamedBeanAddressing.Direct) {
            sm = _selectNamedBeanSwing.getBean();
        } else {
            sm = _exampleSignalMastBeanPanel.getBeanCombo().getSelectedItem();
        }

        if (sm != null) {
            _signalMastAspectComboBox.removeAllItems();
            for (String aspect : sm.getValidAspects()) {
                _signalMastAspectComboBox.addItem(aspect);
                if (expression != null) {
                    if (aspect.equals(expression.getAspect())) _signalMastAspectComboBox.setSelectedItem(aspect);
                }
            }
            JComboBoxUtil.setupComboBoxMaxRows(_signalMastAspectComboBox);
        }
    }


    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionSignalMast expression = new ExpressionSignalMast("IQDE1", null);

        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeReference) {
                expression.setQueryReference(_signalMastQueryReferenceTextField.getText());
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
        ExpressionSignalMast expression = new ExpressionSignalMast(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSignalMast)) {
            throw new IllegalArgumentException("object must be an ExpressionSignalMast but is a: "+object.getClass().getName());
        }
        ExpressionSignalMast expression = (ExpressionSignalMast)object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());

        if (!_exampleSignalMastBeanPanel.isEmpty()
                && (_selectNamedBeanSwing.getAddressing() != NamedBeanAddressing.Direct)
                && (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeDirect)) {

            SignalMast signalMast = _exampleSignalMastBeanPanel.getNamedBean();
            if (signalMast != null) {
                NamedBeanHandle<SignalMast> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(signalMast.getDisplayName(), signalMast);
                expression.getSelectExampleNamedBean().setNamedBean(handle);
            }
        } else {
            expression.getSelectExampleNamedBean().removeNamedBean();
        }

        try {
            if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeDirect) {
                expression.setQueryAddressing(NamedBeanAddressing.Direct);
                expression.setQueryType((ExpressionSignalMast.QueryType)_operationComboBox.getSelectedItem());
            } else if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeReference) {
                expression.setQueryAddressing(NamedBeanAddressing.Reference);
                expression.setQueryReference(_signalMastQueryReferenceTextField.getText());
            } else if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeLocalVariable) {
                expression.setQueryAddressing(NamedBeanAddressing.LocalVariable);
                expression.setQueryLocalVariable(_signalMastQueryLocalVariableTextField.getText());
            } else if (_tabbedPaneQueryType.getSelectedComponent() == _panelQueryTypeFormula) {
                expression.setQueryAddressing(NamedBeanAddressing.Formula);
                expression.setQueryFormula(_signalMastQueryFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneQueryType has unknown selection");
            }

            if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeDirect) {
                expression.setAspectAddressing(NamedBeanAddressing.Direct);

                if (_signalMastAspectComboBox.getItemCount() > 0) {
                    expression.setAspect(_signalMastAspectComboBox
                            .getItemAt(_signalMastAspectComboBox.getSelectedIndex()));
                }
            } else if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeReference) {
                expression.setAspectAddressing(NamedBeanAddressing.Reference);
                expression.setAspectReference(_signalMastAspectReferenceTextField.getText());
            } else if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeLocalVariable) {
                expression.setAspectAddressing(NamedBeanAddressing.LocalVariable);
                expression.setAspectLocalVariable(_signalMastAspectLocalVariableTextField.getText());
            } else if (_tabbedPaneAspectType.getSelectedComponent() == _panelAspectTypeFormula) {
                expression.setAspectAddressing(NamedBeanAddressing.Formula);
                expression.setAspectFormula(_signalMastAspectFormulaTextField.getText());
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


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalMastSwing.class);

}
