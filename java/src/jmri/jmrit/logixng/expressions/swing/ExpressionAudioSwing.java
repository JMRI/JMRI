package jmri.jmrit.logixng.expressions.swing;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionAudio;
import jmri.jmrit.logixng.expressions.ExpressionAudio.AudioState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionAudio object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2023
 */
public class ExpressionAudioSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Audio> _selectNamedBeanSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;

    private JTabbedPane _tabbedPaneAudioState;
    private JComboBox<AudioState> _stateComboBox;
    private JPanel _panelAudioStateDirect;
    private JPanel _panelAudioStateReference;
    private JPanel _panelAudioStateLocalVariable;
    private JPanel _panelAudioStateFormula;
    private JTextField _turnoutStateReferenceTextField;
    private JTextField _turnoutStateLocalVariableTextField;
    private JTextField _turnoutStateFormulaTextField;

    private JCheckBox _checkOnlyOnChangeCheckBox;


    public ExpressionAudioSwing() {
    }

    public ExpressionAudioSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionAudio expression = (ExpressionAudio)object;

        Predicate<Audio> filter = (bean) -> { return bean.getSubType() != Audio.BUFFER; };

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel innerPanel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(AudioManager.class), getJDialog(), this);

        JPanel _tabbedPaneNamedBean;
        if (expression != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean(), filter);
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null, filter);
        }


        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);


        _tabbedPaneAudioState = new JTabbedPane();
        _panelAudioStateDirect = new javax.swing.JPanel();
        _panelAudioStateReference = new javax.swing.JPanel();
        _panelAudioStateLocalVariable = new javax.swing.JPanel();
        _panelAudioStateFormula = new javax.swing.JPanel();

        _tabbedPaneAudioState.addTab(NamedBeanAddressing.Direct.toString(), _panelAudioStateDirect);
        _tabbedPaneAudioState.addTab(NamedBeanAddressing.Reference.toString(), _panelAudioStateReference);
        _tabbedPaneAudioState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelAudioStateLocalVariable);
        _tabbedPaneAudioState.addTab(NamedBeanAddressing.Formula.toString(), _panelAudioStateFormula);

        _stateComboBox = new JComboBox<>();
        for (AudioState e : AudioState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _panelAudioStateDirect.add(_stateComboBox);

        _turnoutStateReferenceTextField = new JTextField();
        _turnoutStateReferenceTextField.setColumns(30);
        _panelAudioStateReference.add(_turnoutStateReferenceTextField);

        _turnoutStateLocalVariableTextField = new JTextField();
        _turnoutStateLocalVariableTextField.setColumns(30);
        _panelAudioStateLocalVariable.add(_turnoutStateLocalVariableTextField);

        _turnoutStateFormulaTextField = new JTextField();
        _turnoutStateFormulaTextField.setColumns(30);
        _panelAudioStateFormula.add(_turnoutStateFormulaTextField);


        _checkOnlyOnChangeCheckBox = new JCheckBox(Bundle.getMessage("Audio_CheckOnlyOnChange"));


        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());

            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneAudioState.setSelectedComponent(_panelAudioStateDirect); break;
                case Reference: _tabbedPaneAudioState.setSelectedComponent(_panelAudioStateReference); break;
                case LocalVariable: _tabbedPaneAudioState.setSelectedComponent(_panelAudioStateLocalVariable); break;
                case Formula: _tabbedPaneAudioState.setSelectedComponent(_panelAudioStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _turnoutStateReferenceTextField.setText(expression.getStateReference());
            _turnoutStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _turnoutStateFormulaTextField.setText(expression.getStateFormula());
            _checkOnlyOnChangeCheckBox.setSelected(expression.isCheckOnlyOnChange());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            _tabbedPaneAudioState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionAudio_Components"), components);

        for (JComponent c : componentList) innerPanel.add(c);

        panel.add(innerPanel);
        panel.add(_checkOnlyOnChangeCheckBox);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionAudio expression = new ExpressionAudio("IQDE1", null);

        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedPaneAudioState.getSelectedComponent() == _panelAudioStateReference) {
                expression.setStateReference(_turnoutStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionAudio expression = new ExpressionAudio(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionAudio)) {
            throw new IllegalArgumentException("object must be an ExpressionAudio but is a: "+object.getClass().getName());
        }
        ExpressionAudio expression = (ExpressionAudio)object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());

        try {
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());

            if (_tabbedPaneAudioState.getSelectedComponent() == _panelAudioStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((AudioState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneAudioState.getSelectedComponent() == _panelAudioStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_turnoutStateReferenceTextField.getText());
            } else if (_tabbedPaneAudioState.getSelectedComponent() == _panelAudioStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_turnoutStateLocalVariableTextField.getText());
            } else if (_tabbedPaneAudioState.getSelectedComponent() == _panelAudioStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_turnoutStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneAudioState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        expression.setCheckOnlyOnChange(_checkOnlyOnChangeCheckBox.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Audio_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionAudioSwing.class);

}
