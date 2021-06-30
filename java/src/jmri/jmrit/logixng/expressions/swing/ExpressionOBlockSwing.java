package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionOBlock;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionOBlock object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionOBlockSwing extends AbstractDigitalExpressionSwing {

    private JTabbedPane _tabbedPaneOBlock;
    private BeanSelectPanel<OBlock> oblockBeanPanel;
    private JPanel _panelOBlockDirect;
    private JPanel _panelOBlockReference;
    private JPanel _panelOBlockLocalVariable;
    private JPanel _panelOBlockFormula;
    private JTextField _oblockReferenceTextField;
    private JTextField _oblockLocalVariableTextField;
    private JTextField _oblockFormulaTextField;
    
    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;
    
    private JTabbedPane _tabbedPaneOBlockState;
    private JComboBox<OBlock.OBlockStatus> _stateComboBox;
    private JPanel _panelOBlockStateDirect;
    private JPanel _panelOBlockStateReference;
    private JPanel _panelOBlockStateLocalVariable;
    private JPanel _panelOBlockStateFormula;
    private JTextField _oblockStateReferenceTextField;
    private JTextField _oblockStateLocalVariableTextField;
    private JTextField _oblockStateFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionOBlock expression = (ExpressionOBlock)object;
        
        panel = new JPanel();
        
        _tabbedPaneOBlock = new JTabbedPane();
        _panelOBlockDirect = new javax.swing.JPanel();
        _panelOBlockReference = new javax.swing.JPanel();
        _panelOBlockLocalVariable = new javax.swing.JPanel();
        _panelOBlockFormula = new javax.swing.JPanel();
        
        _tabbedPaneOBlock.addTab(NamedBeanAddressing.Direct.toString(), _panelOBlockDirect);
        _tabbedPaneOBlock.addTab(NamedBeanAddressing.Reference.toString(), _panelOBlockReference);
        _tabbedPaneOBlock.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOBlockLocalVariable);
        _tabbedPaneOBlock.addTab(NamedBeanAddressing.Formula.toString(), _panelOBlockFormula);
        
        oblockBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(OBlockManager.class), null);
        _panelOBlockDirect.add(oblockBeanPanel);
        
        _oblockReferenceTextField = new JTextField();
        _oblockReferenceTextField.setColumns(30);
        _panelOBlockReference.add(_oblockReferenceTextField);
        
        _oblockLocalVariableTextField = new JTextField();
        _oblockLocalVariableTextField.setColumns(30);
        _panelOBlockLocalVariable.add(_oblockLocalVariableTextField);
        
        _oblockFormulaTextField = new JTextField();
        _oblockFormulaTextField.setColumns(30);
        _panelOBlockFormula.add(_oblockFormulaTextField);
        
        
        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);
        
        
        _tabbedPaneOBlockState = new JTabbedPane();
        _panelOBlockStateDirect = new javax.swing.JPanel();
        _panelOBlockStateReference = new javax.swing.JPanel();
        _panelOBlockStateLocalVariable = new javax.swing.JPanel();
        _panelOBlockStateFormula = new javax.swing.JPanel();
        
        _tabbedPaneOBlockState.addTab(NamedBeanAddressing.Direct.toString(), _panelOBlockStateDirect);
        _tabbedPaneOBlockState.addTab(NamedBeanAddressing.Reference.toString(), _panelOBlockStateReference);
        _tabbedPaneOBlockState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOBlockStateLocalVariable);
        _tabbedPaneOBlockState.addTab(NamedBeanAddressing.Formula.toString(), _panelOBlockStateFormula);
        
        _stateComboBox = new JComboBox<>();
        for (OBlock.OBlockStatus e : OBlock.OBlockStatus.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        
        _panelOBlockStateDirect.add(_stateComboBox);
        
        _oblockStateReferenceTextField = new JTextField();
        _oblockStateReferenceTextField.setColumns(30);
        _panelOBlockStateReference.add(_oblockStateReferenceTextField);
        
        _oblockStateLocalVariableTextField = new JTextField();
        _oblockStateLocalVariableTextField.setColumns(30);
        _panelOBlockStateLocalVariable.add(_oblockStateLocalVariableTextField);
        
        _oblockStateFormulaTextField = new JTextField();
        _oblockStateFormulaTextField.setColumns(30);
        _panelOBlockStateFormula.add(_oblockStateFormulaTextField);
        
        
        if (expression != null) {
            switch (expression.getAddressing()) {
                case Direct: _tabbedPaneOBlock.setSelectedComponent(_panelOBlockDirect); break;
                case Reference: _tabbedPaneOBlock.setSelectedComponent(_panelOBlockReference); break;
                case LocalVariable: _tabbedPaneOBlock.setSelectedComponent(_panelOBlockLocalVariable); break;
                case Formula: _tabbedPaneOBlock.setSelectedComponent(_panelOBlockFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            if (expression.getOBlock() != null) {
                oblockBeanPanel.setDefaultNamedBean(expression.getOBlock().getBean());
            }
            _oblockReferenceTextField.setText(expression.getReference());
            _oblockLocalVariableTextField.setText(expression.getLocalVariable());
            _oblockFormulaTextField.setText(expression.getFormula());
            
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            
            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneOBlockState.setSelectedComponent(_panelOBlockStateDirect); break;
                case Reference: _tabbedPaneOBlockState.setSelectedComponent(_panelOBlockStateReference); break;
                case LocalVariable: _tabbedPaneOBlockState.setSelectedComponent(_panelOBlockStateLocalVariable); break;
                case Formula: _tabbedPaneOBlockState.setSelectedComponent(_panelOBlockStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _oblockStateReferenceTextField.setText(expression.getStateReference());
            _oblockStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _oblockStateFormulaTextField.setText(expression.getStateFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneOBlock,
            _is_IsNot_ComboBox,
            _tabbedPaneOBlockState};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionOBlock_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionOBlock expression = new ExpressionOBlock("IQDE1", null);
        
        try {
            if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockReference) {
                expression.setReference(_oblockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneOBlockState.getSelectedComponent() == _panelOBlockStateReference) {
                expression.setStateReference(_oblockStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            expression.setFormula(_oblockFormulaTextField.getText());
            if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockFormula) {
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
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName();
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionOBlock expression = new ExpressionOBlock(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionOBlock)) {
            throw new IllegalArgumentException("object must be an ExpressionOBlock but is a: "+object.getClass().getName());
        }
        ExpressionOBlock expression = (ExpressionOBlock)object;
        if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockDirect) {
            OBlock oblock = oblockBeanPanel.getNamedBean();
            if (oblock != null) {
                NamedBeanHandle<OBlock> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(oblock.getDisplayName(), oblock);
                expression.setOBlock(handle);
            } else {
                expression.removeOBlock();
            }
        } else {
            expression.removeOBlock();
        }
        try {
            if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
                expression.setReference(_oblockReferenceTextField.getText());
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
                expression.setLocalVariable(_oblockLocalVariableTextField.getText());
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
                expression.setFormula(_oblockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOBlock has unknown selection");
            }
            
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
            
            if (_tabbedPaneOBlockState.getSelectedComponent() == _panelOBlockStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((OBlock.OBlockStatus)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneOBlockState.getSelectedComponent() == _panelOBlockStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_oblockStateReferenceTextField.getText());
            } else if (_tabbedPaneOBlockState.getSelectedComponent() == _panelOBlockStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_oblockStateLocalVariableTextField.getText());
            } else if (_tabbedPaneOBlockState.getSelectedComponent() == _panelOBlockStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_oblockStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOBlockState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("OBlock_Short");
    }
    
    @Override
    public void dispose() {
        if (oblockBeanPanel != null) {
            oblockBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionOBlockSwing.class);
    
}
