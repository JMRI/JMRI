package jmri.jmrit.logixng.util;

import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * Select a string list for LogixNG actions and expressions.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class LogixNG_SelectStringList implements VetoableChangeListener {

    private boolean _onlyDirectAddressingAllowed;

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private final List<String> _list = new ArrayList<>();
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;


    public void setOnlyDirectAddressingAllowed() {
        _onlyDirectAddressingAllowed = true;
    }

    public boolean isOnlyDirectAddressingAllowed() {
        return _onlyDirectAddressingAllowed;
    }

    public void copy(LogixNG_SelectStringList copy) throws ParserException {
        copy.setAddressing(_addressing);
        copy._list.addAll(_list);
        copy.setLocalVariable(_localVariable);
        copy.setFormula(_formula);
    }

    public void setAddressing(@Nonnull NamedBeanAddressing addressing) throws ParserException {
        if (_onlyDirectAddressingAllowed && (addressing != NamedBeanAddressing.Direct)) {
            throw new IllegalArgumentException("Addressing must be Direct");
        }
        this._addressing = addressing;
        parseFormula();
    }

    public boolean isDirectAddressing() {
        return _addressing == NamedBeanAddressing.Direct;
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public List<String> getList() {
        return _list;
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

    public List<String> evaluateValue(ConditionalNG conditionalNG) throws JmriException {

        if (_addressing == NamedBeanAddressing.Direct) {
            return _list;
        } else {
            Object val;

            switch (_addressing) {
                case LocalVariable:
                    SymbolTable symbolNamedBean = conditionalNG.getSymbolTable();
                    val = symbolNamedBean.getValue(_localVariable);
                    break;

                case Formula:
                    val = _expressionNode != null
                            ? _expressionNode.calculate(conditionalNG.getSymbolTable())
                            : null;
                    break;

                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
            }

            if (val instanceof String[]) {
                return Arrays.asList( (String[])val );
            } else if (val instanceof List) {
                List<String> resultList = new ArrayList<>();
                for (Object o : (List<?>)val) {
                    resultList.add(TypeConversionUtil.convertToString(o, false));
                }
                return resultList;
            } else {
                throw new JmriException("value is not an array of String or a List of String. Class name: "
                        + (val != null ? val.getClass().getName() : null));
            }
        }
    }

    public String getDescription(Locale locale) {
        String enumName;

        switch (_addressing) {
            case Direct:
                enumName = Bundle.getMessage(locale, "AddressByDirect", String.join(" ::: ", _list));
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

    /**
     * Register listeners if this object needs that.
     */
    public void registerListeners() {
        // Do nothing
    }

    /**
     * Unregister listeners if this object needs that.
     */
    public void unregisterListeners() {
        // Do nothing
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        // Do nothing
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectStringArray.class);
}
