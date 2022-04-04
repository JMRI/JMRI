package jmri.jmrit.logixng.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * Select a string for LogixNG actions and expressions.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectString {

    private final AbstractBase _base;
    private final InUse _inUse;
    private final LogixNG_SelectTable _selectTable;

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private String _value;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;


    public LogixNG_SelectString(AbstractBase base) {
        _base = base;
        _inUse = () -> true;
        _selectTable = new LogixNG_SelectTable(_base, _inUse);
    }


    public void copy(LogixNG_SelectString copy) throws ParserException {
        copy.setAddressing(_addressing);
        copy.setValue(_value);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setFormula(_formula);
        _selectTable.copy(copy._selectTable);
    }

    public void setAddressing(@Nonnull NamedBeanAddressing addressing) throws ParserException {
        this._addressing = addressing;
        parseFormula();
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setValue(@Nonnull String value) {
        _base.assertListenersAreNotRegistered(log, "setEnum");
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    public void setReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _reference = reference;
    }

    public String getReference() {
        return _reference;
    }

    public void setLocalVariable(@Nonnull String localVariable) {
        _localVariable = localVariable;
    }

    public String getLocalVariable() {
        return _localVariable;
    }

    public void setFormula(@Nonnull String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }

    public String getFormula() {
        return _formula;
    }

    private void parseFormula() throws ParserException {
        if (_addressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
        }
    }

    public LogixNG_SelectTable getSelectTable() {
        return _selectTable;
    }

    public String evaluateValue(ConditionalNG conditionalNG) throws JmriException {

        if (_addressing == NamedBeanAddressing.Direct) {
            return _value;
        } else {
            Object val;

            switch (_addressing) {
                case Reference:
                    val = ReferenceUtil.getReference(
                            conditionalNG.getSymbolTable(), _reference);
                    break;

                case LocalVariable:
                    SymbolTable symbolNamedBean = conditionalNG.getSymbolTable();
                    val = TypeConversionUtil
                            .convertToString(symbolNamedBean.getValue(_localVariable), false);
                    break;

                case Formula:
                    val = _expressionNode != null
                            ? _expressionNode.calculate(conditionalNG.getSymbolTable())
                            : null;
                    break;

                case Table:
                    val = _selectTable.evaluateTableData(conditionalNG);
                    break;

                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
            }

            return TypeConversionUtil.convertToString(val, false);
        }
    }

    public String getDescription(Locale locale) {
        String enumName;
        switch (_addressing) {
            case Direct:
                enumName = Bundle.getMessage(locale, "AddressByDirect", _value);
                break;

            case Reference:
                enumName = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case LocalVariable:
                enumName = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                enumName = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            case Table:
                enumName = Bundle.getMessage(
                        locale,
                        "AddressByTable",
                        _selectTable.getTableNameDescription(locale),
                        _selectTable.getTableRowDescription(locale),
                        _selectTable.getTableColumnDescription(locale));
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing: " + _addressing.name());
        }
        return enumName;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectString.class);
}
