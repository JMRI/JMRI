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
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectEnum {

    public static interface InUse {
        public boolean isInUse();
    }

    private final AbstractBase _base;
    private final Enum[] _enumArray;

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private Enum _enum;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;


    public LogixNG_SelectEnum(AbstractBase base, Enum[] enumArray, Enum initialEnum) {
        _base = base;
        _enumArray = enumArray;
        _enum = initialEnum;
    }


    public void copy(LogixNG_SelectEnum copy) throws ParserException {
        copy.setAddressing(_addressing);
        copy.setEnum(_enum);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setFormula(_formula);
    }

    public Enum[] getEnumArray() {
        return _enumArray;
    }

    public void setAddressing(@Nonnull NamedBeanAddressing addressing) throws ParserException {
        this._addressing = addressing;
        parseFormula();
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setEnum(@Nonnull Enum e) {
        _base.assertListenersAreNotRegistered(log, "setEnum");
        _enum = e;
    }

    public Enum getEnum() {
        return _enum;
    }

    public Enum getEnum(String name) {
        for (Enum e : _enumArray) {
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



    public Enum evaluateEnum(ConditionalNG conditionalNG) throws JmriException {

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

            default:
                throw new IllegalArgumentException("invalid _addressing: " + _addressing.name());
        }
        return enumName;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectEnum.class);
}
