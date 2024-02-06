package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTable;
import jmri.jmrit.logixng.actions.ActionTable.ConstantType;
import jmri.jmrit.logixng.actions.ActionTable.VariableOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectTableSwing;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionTable object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionTableSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectTableSwing selectTableToSetSwing;

    private JTabbedPane _tabbedPaneVariableOperation;
    private BeanSelectPanel<Memory> _copyMemoryBeanPanel;
    private JCheckBox _listenOnMemory;
    private BeanSelectPanel<Block> _copyBlockBeanPanel;
    private JCheckBox _listenOnBlock;
    private BeanSelectPanel<Reporter> _copyReporterBeanPanel;
    private JCheckBox _listenOnReporter;
    private JPanel _setToNull;
    private JPanel _setToConstant;
    private JPanel _copyMemory;
    private JPanel _copyBlock;
    private JPanel _copyReporter;
    private JPanel _copyVariable;
    private JPanel _copyReference;
    private JPanel _calculateFormula;
    private JPanel _copyTableCell;
    private JComboBox<ConstantType> _setToConstantTypeComboBox;
    private JTextField _setToConstantTextField;
    private JTextField _copyLocalVariableTextField;
    private JTextField _copyReferenceTextField;
    private JTextField _calculateFormulaTextField;
    private LogixNG_SelectTableSwing selectTableSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionTable action = (ActionTable)object;

        selectTableToSetSwing = new LogixNG_SelectTableSwing(getJDialog(), this);
        JPanel selectTableToSetPanel;

        selectTableToSetSwing.addChangeListener((e) -> { getJDialog().pack(); });

        selectTableSwing = new LogixNG_SelectTableSwing(getJDialog(), this);

        panel = new JPanel();

        _tabbedPaneVariableOperation = new JTabbedPane();

        _setToNull = new JPanel();
        _setToConstant = new JPanel();
        _setToConstant.setLayout(new BoxLayout(_setToConstant, BoxLayout.Y_AXIS));
        _copyMemory = new JPanel();
        _copyBlock = new JPanel();
        _copyReporter = new JPanel();
        if (action != null) {
            selectTableToSetPanel = selectTableToSetSwing.createPanel(action.getSelectTableToSet());
            _copyTableCell = selectTableSwing.createPanel(action.getSelectTable());
        } else {
            selectTableToSetPanel = selectTableToSetSwing.createPanel(null);
            _copyTableCell = selectTableSwing.createPanel(null);
        }
        _copyVariable = new JPanel();
        _copyReference = new JPanel();
        _calculateFormula = new JPanel();


        _tabbedPaneVariableOperation.addTab(VariableOperation.SetToNull.toString(), _setToNull);
        _tabbedPaneVariableOperation.addTab(VariableOperation.SetToString.toString(), _setToConstant);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyMemoryToVariable.toString(), _copyMemory);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyBlockToVariable.toString(), _copyBlock);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyReporterToVariable.toString(), _copyReporter);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyVariableToVariable.toString(), _copyVariable);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyReferenceToVariable.toString(), _copyReference);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyTableCellToVariable.toString(), _copyTableCell);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CalculateFormula.toString(), _calculateFormula);

        _setToNull.add(new JLabel("Null"));     // No I18N

        JPanel _setToConstantTypeInnerPanel = new JPanel();
        _setToConstantTypeComboBox = new JComboBox<>();
        for (ConstantType value : ConstantType.values()) {
            _setToConstantTypeComboBox.addItem(value);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_setToConstantTypeComboBox);
        _setToConstantTypeInnerPanel.add(new JLabel(Bundle.getMessage("ActionTable_ConstantType")));
        _setToConstantTypeInnerPanel.add(_setToConstantTypeComboBox);
        _setToConstant.add(_setToConstantTypeInnerPanel);

        JPanel _setToConstantTextFieldInnerPanel = new JPanel();
        _setToConstantTextField = new JTextField(30);
        _setToConstantTextFieldInnerPanel.add(_setToConstantTextField);
        _setToConstant.add(_setToConstantTextFieldInnerPanel);

        _copyMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _listenOnMemory = new JCheckBox(Bundle.getMessage("ActionTable_ListenOnMemory"));
        _copyMemory.add(_copyMemoryBeanPanel);
        _copyMemory.add(_listenOnMemory);

        _copyBlockBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(BlockManager.class), null);
        _listenOnBlock = new JCheckBox(Bundle.getMessage("ActionTable_ListenOnBlock"));
        _copyBlock.add(_copyBlockBeanPanel);
        _copyBlock.add(_listenOnBlock);

        _copyReporterBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(ReporterManager.class), null);
        _listenOnReporter = new JCheckBox(Bundle.getMessage("ActionTable_ListenOnReporter"));
        _copyReporter.add(_copyReporterBeanPanel);
        _copyReporter.add(_listenOnReporter);

        _copyLocalVariableTextField = new JTextField(30);
        _copyVariable.add(_copyLocalVariableTextField);

        _copyReferenceTextField = new JTextField(30);
        _copyReference.add(_copyReferenceTextField);

        _calculateFormulaTextField = new JTextField(30);
        _calculateFormula.add(_calculateFormulaTextField);


        if (action != null) {
            if (action.getSelectMemoryNamedBean().getNamedBean() != null) {
                _copyMemoryBeanPanel.setDefaultNamedBean(action.getSelectMemoryNamedBean().getNamedBean().getBean());
            }
            if (action.getSelectBlockNamedBean().getNamedBean() != null) {
                _copyBlockBeanPanel.setDefaultNamedBean(action.getSelectBlockNamedBean().getNamedBean().getBean());
            }
            if (action.getSelectReporterNamedBean().getNamedBean() != null) {
                _copyReporterBeanPanel.setDefaultNamedBean(action.getSelectReporterNamedBean().getNamedBean().getBean());
            }
            switch (action.getVariableOperation()) {
                case SetToNull: _tabbedPaneVariableOperation.setSelectedComponent(_setToNull); break;
                case SetToString: _tabbedPaneVariableOperation.setSelectedComponent(_setToConstant); break;
                case CopyMemoryToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyMemory); break;
                case CopyTableCellToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyTableCell); break;
                case CopyBlockToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyBlock); break;
                case CopyReporterToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyReporter); break;
                case CopyVariableToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyVariable); break;
                case CopyReferenceToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyReference); break;
                case CalculateFormula: _tabbedPaneVariableOperation.setSelectedComponent(_calculateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getVariableOperation().name());
            }
            _setToConstantTypeComboBox.setSelectedItem(action.getConstantType());
            _setToConstantTextField.setText(action.getConstantValue());
            _copyLocalVariableTextField.setText(action.getOtherLocalVariable());
            _copyReferenceTextField.setText(action.getReference());
            _calculateFormulaTextField.setText(action.getFormula());

            _listenOnMemory.setSelected(action.getListenToMemory());
            _listenOnBlock.setSelected(action.getListenToBlock());
            _listenOnReporter.setSelected(action.getListenToReporter());
        }

        JComponent[] components = new JComponent[]{
            selectTableToSetPanel,
            _tabbedPaneVariableOperation
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionTable_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        ActionTable action = new ActionTable("IQDA1", null);

        selectTableToSetSwing.validate(action.getSelectTableToSet(), errorMessages);

        try {
            action.setVariableOperation(VariableOperation.CalculateFormula);
            action.setFormula(_calculateFormulaTextField.getText());
        } catch (ParserException e) {
            errorMessages.add(e.getMessage());
        }

        // If using the Memory tab, validate the memory variable selection.
        if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyMemory) {
            if (_copyMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionTable_CopyErrorMemory"));
            }
        }

         // If using the Block tab, validate the block variable selection.
        if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyBlock) {
            if (_copyBlockBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionTable_CopyErrorBlock"));
            }
        }

         // If using the Reporter tab, validate the reporter variable selection.
        if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyReporter) {
            if (_copyReporterBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionTable_CopyErrorReporter"));
            }
        }

        selectTableSwing.validate(action.getSelectTable(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionTable action = new ActionTable(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionTable)) {
            throw new IllegalArgumentException("object must be an ActionTable but is a: "+object.getClass().getName());
        }
        ActionTable action = (ActionTable)object;

        selectTableToSetSwing.updateObject(action.getSelectTableToSet());


        if (!_copyMemoryBeanPanel.isEmpty()
                && (_tabbedPaneVariableOperation.getSelectedComponent() == _copyMemory)) {
            Memory memory = _copyMemoryBeanPanel.getNamedBean();
            if (memory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(memory.getDisplayName(), memory);
                action.getSelectMemoryNamedBean().setNamedBean(handle);
            }
        }

        if (!_copyBlockBeanPanel.isEmpty()
                && (_tabbedPaneVariableOperation.getSelectedComponent() == _copyBlock)) {
            Block block = _copyBlockBeanPanel.getNamedBean();
            if (block != null) {
                NamedBeanHandle<Block> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(block.getDisplayName(), block);
                action.getSelectBlockNamedBean().setNamedBean(handle);
            }
        }

        if (!_copyReporterBeanPanel.isEmpty()
                && (_tabbedPaneVariableOperation.getSelectedComponent() == _copyReporter)) {
            Reporter reporter = _copyReporterBeanPanel.getNamedBean();
            if (reporter != null) {
                NamedBeanHandle<Reporter> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(reporter.getDisplayName(), reporter);
                action.getSelectReporterNamedBean().setNamedBean(handle);
            }
        }

        try {
            if (_tabbedPaneVariableOperation.getSelectedComponent() == _setToNull) {
                action.setVariableOperation(VariableOperation.SetToNull);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _setToConstant) {
                action.setVariableOperation(VariableOperation.SetToString);
                action.setConstantType(_setToConstantTypeComboBox.getItemAt(
                        _setToConstantTypeComboBox.getSelectedIndex()));
                action.setConstantValue(_setToConstantTextField.getText());
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyMemory) {
                action.setVariableOperation(VariableOperation.CopyMemoryToVariable);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyBlock) {
                action.setVariableOperation(VariableOperation.CopyBlockToVariable);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyReporter) {
                action.setVariableOperation(VariableOperation.CopyReporterToVariable);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyTableCell) {
                action.setVariableOperation(VariableOperation.CopyTableCellToVariable);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyVariable) {
                action.setVariableOperation(VariableOperation.CopyVariableToVariable);
                action.setOtherLocalVariable(_copyLocalVariableTextField.getText());
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyReference) {
                action.setVariableOperation(VariableOperation.CopyReferenceToVariable);
                action.setReference(_copyReferenceTextField.getText());
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _calculateFormula) {
                action.setVariableOperation(VariableOperation.CalculateFormula);
                action.setFormula(_calculateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneVariableOperation has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        selectTableSwing.updateObject(action.getSelectTable());

        action.setListenToMemory(_listenOnMemory.isSelected());
        action.setListenToBlock(_listenOnBlock.isSelected());
        action.setListenToReporter(_listenOnReporter.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionTable_Short");
    }

    /** {@inheritDoc} */
    @Override
    public boolean canClose() {
        return selectTableToSetSwing.canClose() && selectTableSwing.canClose();
    }

    @Override
    public void dispose() {
        selectTableToSetSwing.dispose();
        selectTableSwing.dispose();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTableSwing.class);

}
