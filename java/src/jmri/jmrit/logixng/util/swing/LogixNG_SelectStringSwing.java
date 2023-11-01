package jmri.jmrit.logixng.util.swing;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectString;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.FileUtil;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JmriJFileChooser;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectString.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectStringSwing {

    private final JDialog _dialog;
    private final LogixNG_SelectTableSwing _selectTableSwing;

    private JTabbedPane _tabbedPane;
    private JTextField _valueTextField;
    private JPanel _panelDirect;
    private JPanel _panelReference;
    private JPanel _panelMemory;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JPanel _panelTable;
    private JTextField _referenceTextField;
    private BeanSelectPanel<Memory> _memoryPanel;
    private JCheckBox _listenToMemoryCheckBox;
    private JTextField _localVariableTextField;
    private JTextField _formulaTextField;


    public LogixNG_SelectStringSwing(
            @Nonnull JDialog dialog,
            @Nonnull SwingConfiguratorInterface swi) {
        _dialog = dialog;
        _selectTableSwing = new LogixNG_SelectTableSwing(_dialog, swi);
    }

    public JPanel createPanel(@CheckForNull LogixNG_SelectString selectStr) {
        _panelDirect = new javax.swing.JPanel();

        _valueTextField = new JTextField(30);
        _panelDirect.add(_valueTextField);

        return internalCreatePanel(selectStr);
    }

    public JPanel createFilenamePanel(@CheckForNull LogixNG_SelectString selectStr, String path) {
        _panelDirect = new javax.swing.JPanel();

        JButton selectFileButton = new JButton("..."); // "File" replaced by ...
        selectFileButton.setMaximumSize(selectFileButton.getPreferredSize());
        selectFileButton.setToolTipText(Bundle.getMessage("FileButtonHint"));  // NOI18N
        selectFileButton.addActionListener((ActionEvent e) -> {
            JmriJFileChooser fileChooser = new JmriJFileChooser(path);
            fileChooser.rescanCurrentDirectory();
            int retVal = fileChooser.showOpenDialog(null);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                // set selected file location
                try {
                    _valueTextField.setText(FileUtil.getPortableFilename(fileChooser.getSelectedFile().getCanonicalPath()));
                } catch (java.io.IOException ex) {
                    log.error("exception setting file location", ex);  // NOI18N
                    _valueTextField.setText("");
                }
            }
        });
        _valueTextField = new JTextField(30);
        _panelDirect.add(_valueTextField);
        _panelDirect.add(selectFileButton);

        return internalCreatePanel(selectStr);
    }

    private JPanel internalCreatePanel(@CheckForNull LogixNG_SelectString selectStr) {

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelReference = new javax.swing.JPanel();
        _panelMemory = new JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        if (selectStr != null) {
            _panelTable = _selectTableSwing.createPanel(selectStr.getSelectTable());
            if (selectStr.isOnlyDirectAddressingAllowed()) {
                _tabbedPane.setEnabled(false);
                _panelReference.setEnabled(false);
                _panelMemory.setEnabled(false);
                _panelLocalVariable.setEnabled(false);
                _panelFormula.setEnabled(false);
            }
        } else {
            _panelTable = _selectTableSwing.createPanel(null);
        }

        _memoryPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _listenToMemoryCheckBox = new JCheckBox(Bundle.getMessage("ListenToMemory"));

        _panelMemory.setLayout(new BoxLayout(_panelMemory, BoxLayout.Y_AXIS));
        _panelMemory.add(_memoryPanel);
        _panelMemory.add(_listenToMemoryCheckBox);

        _tabbedPane.addTab(NamedBeanAddressing.Direct.toString(), _panelDirect);
        _tabbedPane.addTab(NamedBeanAddressing.Reference.toString(), _panelReference);
        _tabbedPane.addTab(NamedBeanAddressing.Memory.toString(), _panelMemory);
        _tabbedPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedPane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);
        _tabbedPane.addTab(NamedBeanAddressing.Table.toString(), _panelTable);

        _referenceTextField = new JTextField();
        _referenceTextField.setColumns(30);
        _panelReference.add(_referenceTextField);

        _localVariableTextField = new JTextField();
        _localVariableTextField.setColumns(30);
        _panelLocalVariable.add(_localVariableTextField);

        _formulaTextField = new JTextField();
        _formulaTextField.setColumns(30);
        _panelFormula.add(_formulaTextField);


        if (selectStr != null) {
            switch (selectStr.getAddressing()) {
                case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
                case Reference: _tabbedPane.setSelectedComponent(_panelReference); break;
                case Memory: _tabbedPane.setSelectedComponent(_panelMemory); break;
                case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
                case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
                case Table: _tabbedPane.setSelectedComponent(_panelTable); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + selectStr.getAddressing().name());
            }
            if (selectStr.getValue() != null) {
                _valueTextField.setText(selectStr.getValue());
            }
            _referenceTextField.setText(selectStr.getReference());
            _memoryPanel.setDefaultNamedBean(selectStr.getMemory());
            _listenToMemoryCheckBox.setSelected(selectStr.getListenToMemory());
            _localVariableTextField.setText(selectStr.getLocalVariable());
            _formulaTextField.setText(selectStr.getFormula());
        }

        panel.add(_tabbedPane);
        return panel;
    }

    public boolean validate(
            @Nonnull LogixNG_SelectString selectStr,
            @Nonnull List<String> errorMessages) {
        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectStr.setValue(_valueTextField.getText());
            }
            if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectStr.setReference(_referenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            selectStr.setFormula(_formulaTextField.getText());
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectStr.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectStr.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPane.getSelectedComponent() == _panelMemory) {
                selectStr.setAddressing(NamedBeanAddressing.Memory);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectStr.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectStr.setAddressing(NamedBeanAddressing.Formula);
            } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
                selectStr.setAddressing(NamedBeanAddressing.Table);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }

        _selectTableSwing.validate(selectStr.getSelectTable(), errorMessages);

        return errorMessages.isEmpty();
    }

    public void setEnabled(boolean value) {
        _tabbedPane.setEnabled(value);
        _valueTextField.setEnabled(value);
        _referenceTextField.setEnabled(value);
        _localVariableTextField.setEnabled(value);
        _formulaTextField.setEnabled(value);
    }

    public void updateObject(@Nonnull LogixNG_SelectString selectStr) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            selectStr.setValue(_valueTextField.getText());
        }

        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectStr.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectStr.setAddressing(NamedBeanAddressing.Reference);
                selectStr.setReference(_referenceTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelMemory) {
                selectStr.setAddressing(NamedBeanAddressing.Memory);
                selectStr.setMemory(_memoryPanel.getNamedBean());
                selectStr.setListenToMemory(_listenToMemoryCheckBox.isSelected());
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectStr.setAddressing(NamedBeanAddressing.LocalVariable);
                selectStr.setLocalVariable(_localVariableTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectStr.setAddressing(NamedBeanAddressing.Formula);
                selectStr.setFormula(_formulaTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
                selectStr.setAddressing(NamedBeanAddressing.Table);
            } else {
                throw new IllegalArgumentException("_tabbedPaneEnum has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        _selectTableSwing.updateObject(selectStr.getSelectTable());
    }

    public void dispose() {
        _selectTableSwing.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectStringSwing.class);
}
