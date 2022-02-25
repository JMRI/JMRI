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
 * Select namedBean for LogixNG actions and expressions.
 *
 * @param <E> the type of enum
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectEnum<E extends Enum<?>> {

    private final AbstractBase _base;
    private final InUse _inUse;
    private final E[] _enumArray;
    private final LogixNG_SelectTable _selectTable;

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private E _enum;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;


    public LogixNG_SelectEnum(AbstractBase base, E[] enumArray, E initialEnum) {
        _base = base;
        _inUse = () -> true;
        _enumArray = enumArray;
        _enum = initialEnum;
        _selectTable = new LogixNG_SelectTable(_base, _inUse);
    }


    public void copy(LogixNG_SelectEnum<E> copy) throws ParserException {
        copy.setAddressing(_addressing);
        copy.setEnum(_enum);
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

    public void setEnum(@Nonnull E e) {
        _base.assertListenersAreNotRegistered(log, "setEnum");
        _enum = e;
    }

    public E getEnum() {
        return _enum;
    }

    public E getEnum(String name) {
        for (E e : _enumArray) {
            if (e.name().equals(name)) return e;
        }
        return null;
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

    public E evaluateEnum(ConditionalNG conditionalNG) throws JmriException {

        if (_addressing == NamedBeanAddressing.Direct) {
            return _enum;
        } else {
            String name;

            switch (_addressing) {
                case Reference:
                    name = ReferenceUtil.getReference(
                            conditionalNG.getSymbolTable(), _reference);
                    break;

                case LocalVariable:
                    SymbolTable symbolNamedBean = conditionalNG.getSymbolTable();
                    name = TypeConversionUtil
                            .convertToString(symbolNamedBean.getValue(_localVariable), false);
                    break;

                case Formula:
                    name = _expressionNode  != null
                            ? TypeConversionUtil.convertToString(
                                    _expressionNode.calculate(
                                            conditionalNG.getSymbolTable()), false)
                            : null;
                    break;

                case Table:
                    name = TypeConversionUtil.convertToString(
                            _selectTable.evaluateTableData(conditionalNG), false);
                    break;

                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
            }

            return getEnum(name);
        }
    }

    public String getDescription(Locale locale) {
        String enumName;
        switch (_addressing) {
            case Direct:
                enumName = Bundle.getMessage(locale, "AddressByDirect", _enum.toString());
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectEnum.class);
}
