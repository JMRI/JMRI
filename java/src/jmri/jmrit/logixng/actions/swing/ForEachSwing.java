package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ForEach;
import jmri.jmrit.logixng.actions.ForEach.CommonManager;
import jmri.jmrit.logixng.actions.ForEach.UserSpecifiedSource;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ForEach object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ForEachSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _commonOrUserSpecifiedPane;
    private JPanel _commonPanel;
    private JComboBox<CommonManager> _commonManagersComboBox;

    private JTabbedPane _tabbedPaneUserSpecifiedSource;
    private BeanSelectPanel<Memory> _copyMemoryBeanPanel;
    private JCheckBox _listenOnMemory;
    private JPanel _copyMemory;
    private JPanel _copyVariable;
    private JPanel _calculateFormula;
    private JTextField _copyLocalVariableTextField;
    private JTextField _calculateFormulaTextField;

    private JTextField _localVariable;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ForEach)) {
            throw new IllegalArgumentException("object must be an ForEach but is a: "+object.getClass().getName());
        }

        ForEach action = (ForEach)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


        _commonOrUserSpecifiedPane = new JTabbedPane();

        _commonPanel = new JPanel();
        _commonOrUserSpecifiedPane.addTab(Bundle.getMessage("ForEachSwing_Common"), _commonPanel);
        _commonManagersComboBox = new JComboBox<>();
        for (CommonManager commonManager : CommonManager.values()) {
            _commonManagersComboBox.addItem(commonManager);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_commonManagersComboBox);

        _commonPanel.add(_commonManagersComboBox);


        _tabbedPaneUserSpecifiedSource = new JTabbedPane();
        _commonOrUserSpecifiedPane.addTab(Bundle.getMessage("ForEachSwing_UserSpecified"), _tabbedPaneUserSpecifiedSource);

        _copyMemory = new JPanel();
        _copyVariable = new JPanel();
        _calculateFormula = new JPanel();

        _tabbedPaneUserSpecifiedSource.addTab(UserSpecifiedSource.Memory.toString(), _copyMemory);
        _tabbedPaneUserSpecifiedSource.addTab(UserSpecifiedSource.Variable.toString(), _copyVariable);
        _tabbedPaneUserSpecifiedSource.addTab(UserSpecifiedSource.Formula.toString(), _calculateFormula);

        _copyMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _listenOnMemory = new JCheckBox(Bundle.getMessage("ActionLocalVariable_ListenOnMemory"));
        _copyMemory.add(_copyMemoryBeanPanel);
        _copyMemory.add(_listenOnMemory);

        _copyLocalVariableTextField = new JTextField(30);
        _copyVariable.add(_copyLocalVariableTextField);

        _calculateFormulaTextField = new JTextField(30);
        _calculateFormula.add(_calculateFormulaTextField);

        JPanel localVariablePanel = new JPanel();
        localVariablePanel.add(new JLabel(Bundle.getMessage("ForEachSwing_LocalVariable")));
        _localVariable = new JTextField(20);
        localVariablePanel.add(_localVariable);
        panel.add(localVariablePanel);


        if (action != null) {
            if (!action.isUseCommonSource()) {
                _commonOrUserSpecifiedPane.setSelectedComponent(_tabbedPaneUserSpecifiedSource);
            }
            _commonManagersComboBox.setSelectedItem(action.getCommonManager());

            if (action.getSelectMemoryNamedBean().getNamedBean() != null) {
                _copyMemoryBeanPanel.setDefaultNamedBean(action.getSelectMemoryNamedBean().getNamedBean().getBean());
            }
            switch (action.getUserSpecifiedSource()) {
                case Memory: _tabbedPaneUserSpecifiedSource.setSelectedComponent(_copyMemory); break;
                case Variable: _tabbedPaneUserSpecifiedSource.setSelectedComponent(_copyVariable); break;
                case Formula: _tabbedPaneUserSpecifiedSource.setSelectedComponent(_calculateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getUserSpecifiedSource().name());
            }
            _copyLocalVariableTextField.setText(action.getOtherLocalVariable());
            _calculateFormulaTextField.setText(action.getFormula());

            _listenOnMemory.setSelected(action.isListenToMemory());

            _localVariable.setText(action.getLocalVariableName());
        }


        panel.add(_commonOrUserSpecifiedPane);
        panel.add(localVariablePanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ForEach action = new ForEach("IQDA1", null);

        try {
            action.setUserSpecifiedSource(UserSpecifiedSource.Formula);
            action.setFormula(_calculateFormulaTextField.getText());
        } catch (ParserException e) {
            errorMessages.add(e.getMessage());
        }

        // If using the Memory tab, validate the memory variable selection.
        if (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _copyMemory) {
            if (_copyMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ForEach_ErrorMemory"));
            }
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ForEach action = new ForEach(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof ForEach)) {
            throw new IllegalArgumentException("object must be an ForEach but is a: "+object.getClass().getName());
        }

        ForEach action = (ForEach)object;

        try {
            if (_commonOrUserSpecifiedPane.getSelectedComponent() == _commonPanel) {

                action.setUseCommonSource(true);
                action.setCommonManager(_commonManagersComboBox.getItemAt(
                        _commonManagersComboBox.getSelectedIndex()));

            } else if (_commonOrUserSpecifiedPane.getSelectedComponent() == _tabbedPaneUserSpecifiedSource) {

                action.setUseCommonSource(false);

                if (!_copyMemoryBeanPanel.isEmpty()
                        && (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _copyMemory)) {
                    Memory memory = _copyMemoryBeanPanel.getNamedBean();
                    if (memory != null) {
                        NamedBeanHandle<Memory> handle
                                = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                        .getNamedBeanHandle(memory.getDisplayName(), memory);
                        action.getSelectMemoryNamedBean().setNamedBean(handle);
                    }
                }
                action.setListenToMemory(_listenOnMemory.isSelected());

                try {
                    if (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _copyMemory) {
                        action.setUserSpecifiedSource(UserSpecifiedSource.Memory);
                    } else if (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _copyVariable) {
                        action.setUserSpecifiedSource(UserSpecifiedSource.Variable);
                        action.setOtherLocalVariable(_copyLocalVariableTextField.getText());
                    } else if (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _calculateFormula) {
                        action.setUserSpecifiedSource(UserSpecifiedSource.Formula);
                        action.setFormula(_calculateFormulaTextField.getText());
                    } else {
                        throw new IllegalArgumentException("_tabbedPaneUserSpecifiedSource has unknown selection");
                    }
                } catch (ParserException e) {
                    throw new RuntimeException("ParserException: "+e.getMessage(), e);
                }

            } else {
                throw new IllegalArgumentException("_tabbedPaneUserSpecifiedSource has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        action.setLocalVariableName(_localVariable.getText());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ForEach_Short");
    }

    @Override
    public void dispose() {
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ForEachSwing.class);

}
