package jmri.jmrit.logixng.util.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectDouble;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectInteger.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectDoubleSwing {

    private final JDialog _dialog;
    private final LogixNG_SelectTableSwing _selectTableSwing;
    private final FormatterParserValidator _formatterParserValidator;

    private JTabbedPane _tabbedPane;
    private JTextField _valueTextField;
    private JPanel _panelDirect;
    private JPanel _panelReference;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JPanel _panelTable;
    private JTextField _referenceTextField;
    private JTextField _localVariableTextField;
    private JTextField _formulaTextField;


    public LogixNG_SelectDoubleSwing(
            @Nonnull JDialog dialog,
            @Nonnull SwingConfiguratorInterface swi) {
        _dialog = dialog;
        _selectTableSwing = new LogixNG_SelectTableSwing(_dialog, swi);
        _formatterParserValidator = new DefaultFormatterParserValidator();
    }

    public LogixNG_SelectDoubleSwing(
            @Nonnull JDialog dialog,
            @Nonnull SwingConfiguratorInterface swi,
            @Nonnull FormatterParserValidator formatterParserValidator) {
        _dialog = dialog;
        _selectTableSwing = new LogixNG_SelectTableSwing(_dialog, swi);
        _formatterParserValidator = formatterParserValidator;
    }

    public JPanel createPanel(@CheckForNull LogixNG_SelectDouble selectStr) {

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        if (selectStr != null) {
            _panelTable = _selectTableSwing.createPanel(selectStr.getSelectTable());
        } else {
            _panelTable = _selectTableSwing.createPanel(null);
        }

        _tabbedPane.addTab(NamedBeanAddressing.Direct.toString(), _panelDirect);
        _tabbedPane.addTab(NamedBeanAddressing.Reference.toString(), _panelReference);
        _tabbedPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedPane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);
        _tabbedPane.addTab(NamedBeanAddressing.Table.toString(), _panelTable);

        _valueTextField = new JTextField(30);
        _valueTextField.setText(_formatterParserValidator.format(
                _formatterParserValidator.getInitialValue()));
        _panelDirect.add(_valueTextField);

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
                case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
                case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
                case Table: _tabbedPane.setSelectedComponent(_panelTable); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + selectStr.getAddressing().name());
            }
            _valueTextField.setText(_formatterParserValidator.format(selectStr.getValue()));
            _referenceTextField.setText(selectStr.getReference());
            _localVariableTextField.setText(selectStr.getLocalVariable());
            _formulaTextField.setText(selectStr.getFormula());
        } else {
            _valueTextField.setText(_formatterParserValidator.format(
                    _formatterParserValidator.getInitialValue()));
        }

        panel.add(_tabbedPane);
        return panel;
    }

    public boolean validate(
            @Nonnull LogixNG_SelectDouble selectStr,
            @Nonnull List<String> errorMessages) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            String result = _formatterParserValidator.validate(_valueTextField.getText());
            if (result != null) errorMessages.add(result);
        }

        try {
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

    public void updateObject(@Nonnull LogixNG_SelectDouble selectStr) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            selectStr.setValue(_formatterParserValidator.parse(_valueTextField.getText()));
        }

        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectStr.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectStr.setAddressing(NamedBeanAddressing.Reference);
                selectStr.setReference(_referenceTextField.getText());
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


    /**
     * Format, parse and validate.
     */
    public interface FormatterParserValidator {

        /**
         * Get the initial value
         * @return the initial value
         */
        public double getInitialValue();

        /**
         * Format the value
         * @param value the value
         * @return the formatted string
         */
        public String format(double value);

        /**
         * Parse the string
         * @param str the string
         * @return the parsed value
         */
        public double parse(String str);

        /**
         * Validates the string
         * @param str the string
         * @return null if valid. An error message if not valid
         */
        public String validate(String str);
    }


    public static class DefaultFormatterParserValidator
            implements FormatterParserValidator {

        @Override
        public double getInitialValue() {
            return 0;
        }

        @Override
        public String format(double value) {
            return Double.toString(value);
        }

        @Override
        public double parse(String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return getInitialValue();
            }
        }

        @Override
        public String validate(String str) {
            try {
                return null;
            } catch (NumberFormatException e) {
                return Bundle.getMessage("LogixNG_SelectDoubleSwing_MustBeValidInteger");
            }
        }

    }

}
