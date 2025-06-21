package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * Evaluates the state of a SignalMast.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionSignalMast extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private final LogixNG_SelectNamedBean<SignalMast> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, SignalMast.class, InstanceManager.getDefault(SignalMastManager.class), this);

    private NamedBeanAddressing _queryAddressing = NamedBeanAddressing.Direct;
    private QueryType _queryType = QueryType.Aspect;
    private String _queryReference = "";
    private String _queryLocalVariable = "";
    private String _queryFormula = "";
    private ExpressionNode _queryExpressionNode;

    private NamedBeanAddressing _aspectAddressing = NamedBeanAddressing.Direct;
    private String _signalMastAspect = "";
    private String _aspectReference = "";
    private String _aspectLocalVariable = "";
    private String _aspectFormula = "";
    private ExpressionNode _aspectExpressionNode;

    private final LogixNG_SelectNamedBean<SignalMast> _selectExampleNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, SignalMast.class, InstanceManager.getDefault(SignalMastManager.class), this);


    public ExpressionSignalMast(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionSignalMast copy = new ExpressionSignalMast(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setAspect(_signalMastAspect);
        copy.setQueryAddressing(_queryAddressing);
        copy.setQueryType(_queryType);
        copy.setQueryFormula(_queryFormula);
        copy.setQueryLocalVariable(_queryLocalVariable);
        copy.setQueryReference(_queryReference);
        copy.setAspectAddressing(_aspectAddressing);
        copy.setAspectFormula(_aspectFormula);
        copy.setAspectLocalVariable(_aspectLocalVariable);
        copy.setAspectReference(_aspectReference);
        _selectExampleNamedBean.copy(copy._selectExampleNamedBean);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<SignalMast> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectNamedBean<SignalMast> getSelectExampleNamedBean() {
        return _selectExampleNamedBean;
    }

    public void setQueryAddressing(NamedBeanAddressing addressing) throws ParserException {
        _queryAddressing = addressing;
        parseQueryFormula();
    }

    public NamedBeanAddressing getQueryAddressing() {
        return _queryAddressing;
    }

    public void setQueryType(QueryType queryType) {
        _queryType = queryType;
    }

    public QueryType getQueryType() {
        return _queryType;
    }

    public void setQueryReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _queryReference = reference;
    }

    public String getQueryReference() {
        return _queryReference;
    }

    public void setQueryLocalVariable(@Nonnull String localVariable) {
        _queryLocalVariable = localVariable;
    }

    public String getQueryLocalVariable() {
        return _queryLocalVariable;
    }

    public void setQueryFormula(@Nonnull String formula) throws ParserException {
        _queryFormula = formula;
        parseQueryFormula();
    }

    public String getQueryFormula() {
        return _queryFormula;
    }

    private void parseQueryFormula() throws ParserException {
        if (_queryAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _queryExpressionNode = parser.parseExpression(_queryFormula);
        } else {
            _queryExpressionNode = null;
        }
    }

    public void setAspectAddressing(NamedBeanAddressing addressing) throws ParserException {
        _aspectAddressing = addressing;
        parseAspectFormula();
    }

    public NamedBeanAddressing getAspectAddressing() {
        return _aspectAddressing;
    }

    public void setAspect(String aspect) {
        if (aspect == null) _signalMastAspect = "";
        else _signalMastAspect = aspect;
    }

    public String getAspect() {
        return _signalMastAspect;
    }

    public void setAspectReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _aspectReference = reference;
    }

    public String getAspectReference() {
        return _aspectReference;
    }

    public void setAspectLocalVariable(@Nonnull String localVariable) {
        _aspectLocalVariable = localVariable;
    }

    public String getAspectLocalVariable() {
        return _aspectLocalVariable;
    }

    public void setAspectFormula(@Nonnull String formula) throws ParserException {
        _aspectFormula = formula;
        parseAspectFormula();
    }

    public String getAspectFormula() {
        return _aspectFormula;
    }

    private void parseAspectFormula() throws ParserException {
        if (_aspectAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _aspectExpressionNode = parser.parseExpression(_aspectFormula);
        } else {
            _aspectExpressionNode = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    private String getNewAspect(ConditionalNG conditionalNG) throws JmriException {

        switch (_aspectAddressing) {
            case Direct:
                return _signalMastAspect;

            case Reference:
                return ReferenceUtil.getReference(
                        conditionalNG.getSymbolTable(), _aspectReference);

            case LocalVariable:
                SymbolTable symbolTable = conditionalNG.getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_aspectLocalVariable), false);

            case Formula:
                return _aspectExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _aspectExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false)
                        : "";

            default:
                throw new IllegalArgumentException("invalid _aspectAddressing state: " + _aspectAddressing.name());
        }
    }

    private QueryType getQuery(ConditionalNG conditionalNG) throws JmriException {

        String oper = "";
        try {
            switch (_queryAddressing) {
                case Direct:
                    return _queryType;

                case Reference:
                    oper = ReferenceUtil.getReference(
                            conditionalNG.getSymbolTable(), _queryReference);
                    return QueryType.valueOf(oper);

                case LocalVariable:
                    SymbolTable symbolTable =
                            conditionalNG.getSymbolTable();
                    oper = TypeConversionUtil
                            .convertToString(symbolTable.getValue(_queryLocalVariable), false);
                    return QueryType.valueOf(oper);

                case Formula:
                    if (_aspectExpressionNode != null) {
                        oper = TypeConversionUtil.convertToString(
                                _queryExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false);
                        return QueryType.valueOf(oper);
                    } else {
                        return null;
                    }
                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _queryAddressing.name());
            }
        } catch (IllegalArgumentException e) {
            throw new JmriException("Unknown query: "+oper, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        final ConditionalNG conditionalNG = getConditionalNG();

        SignalMast signalMast = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (signalMast == null) return false;

        QueryType query = getQuery(conditionalNG);

        boolean result = false;

        switch (query) {
            case Aspect:
                if (signalMast.getAspect() != null) {
                    result = getNewAspect(conditionalNG).equals(signalMast.getAspect());
                }
                break;
            case NotAspect:
                if (signalMast.getAspect() != null) {
                    result = ! getNewAspect(conditionalNG).equals(signalMast.getAspect());
                }
                break;
            case Lit:
                result = signalMast.getLit();
                break;
            case NotLit:
                result = ! signalMast.getLit();
                break;
            case Held:
                result = signalMast.getHeld();
                break;
            case NotHeld:
                result = ! signalMast.getHeld();
                break;
            case IsPermissiveSmlDisabled:
                result = signalMast.isPermissiveSmlDisabled();
                break;
            case IsPermissiveSmlNotDisabled:
                result = ! signalMast.isPermissiveSmlDisabled();
                break;
            default:
                throw new RuntimeException("Unknown enum: "+_queryType.name());
        }

        return result;
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "SignalMast_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String query;
        String aspect;

        switch (_queryAddressing) {
            case Direct:
                query = Bundle.getMessage(locale, "AddressByDirect", _queryType._text);
                break;

            case Reference:
                query = Bundle.getMessage(locale, "AddressByReference", _queryReference);
                break;

            case LocalVariable:
                query = Bundle.getMessage(locale, "AddressByLocalVariable", _queryLocalVariable);
                break;

            case Formula:
                query = Bundle.getMessage(locale, "AddressByFormula", _queryFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _queryAddressing state: " + _queryAddressing.name());
        }

        switch (_aspectAddressing) {
            case Direct:
                aspect = Bundle.getMessage(locale, "AddressByDirect", _signalMastAspect);
                break;

            case Reference:
                aspect = Bundle.getMessage(locale, "AddressByReference", _aspectReference);
                break;

            case LocalVariable:
                aspect = Bundle.getMessage(locale, "AddressByLocalVariable", _aspectLocalVariable);
                break;

            case Formula:
                aspect = Bundle.getMessage(locale, "AddressByFormula", _aspectFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _aspectAddressing.name());
        }

        if (_queryAddressing == NamedBeanAddressing.Direct) {
            if (_queryType == QueryType.Aspect) {
                return Bundle.getMessage(locale, "SignalMast_LongAspect", namedBean, aspect);
            } if (_queryType == QueryType.NotAspect) {
                return Bundle.getMessage(locale, "SignalMast_LongNotAspect", namedBean, aspect);
            } else {
                return Bundle.getMessage(locale, "SignalMast_Long", namedBean, query);
            }
        } else {
            return Bundle.getMessage(locale, "SignalMast_LongUnknownOper", namedBean, query, aspect);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        SignalMast signalMast = _selectNamedBean.getNamedBeanIfDirectAddressing();

        if (!_listenersAreRegistered && (signalMast != null)) {
            switch (_queryType) {
                case Aspect:
                case NotAspect:
                    signalMast.addPropertyChangeListener("Aspect", this);
                    break;

                case Lit:
                case NotLit:
                    signalMast.addPropertyChangeListener("Lit", this);
                    break;

                case Held:
                case NotHeld:
                    signalMast.addPropertyChangeListener("Held", this);
                    break;

                case IsPermissiveSmlDisabled:
                case IsPermissiveSmlNotDisabled:
                    signalMast.addPropertyChangeListener("PermissiveSmlDisabled", this);
                    break;

                default:
                    throw new RuntimeException("Unknown enum: "+_queryType.name());
            }
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        SignalMast signalMast = _selectNamedBean.getNamedBeanIfDirectAddressing();

        if (_listenersAreRegistered && (signalMast != null)) {
            switch (_queryType) {
                case Aspect:
                case NotAspect:
                    signalMast.removePropertyChangeListener("Aspect", this);
                    break;

                case Lit:
                case NotLit:
                    signalMast.removePropertyChangeListener("Lit", this);
                    break;

                case Held:
                case NotHeld:
                    signalMast.removePropertyChangeListener("Held", this);
                    break;

                case IsPermissiveSmlDisabled:
                case IsPermissiveSmlNotDisabled:
                    signalMast.removePropertyChangeListener("PermissiveSmlDisabled", this);
                    break;

                default:
                    throw new RuntimeException("Unknown enum: "+_queryType.name());
            }
            _selectNamedBean.unregisterListeners();
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }



    public enum QueryType {
        Aspect(Bundle.getMessage("SignalMastQueryType_Aspect")),
        NotAspect(Bundle.getMessage("SignalMastQueryType_NotAspect")),
        Lit(Bundle.getMessage("SignalMastQueryType_Lit")),
        NotLit(Bundle.getMessage("SignalMastQueryType_NotLit")),
        Held(Bundle.getMessage("SignalMastQueryType_Held")),
        NotHeld(Bundle.getMessage("SignalMastQueryType_NotHeld")),
        IsPermissiveSmlDisabled(Bundle.getMessage("SignalMastQueryType_IsPermissiveSmlDisabled")),
        IsPermissiveSmlNotDisabled(Bundle.getMessage("SignalMastQueryType_IsPermissiveSmlNotDisabled"));

        private final String _text;

        private QueryType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ExpressionSignalMast: bean = {}, report = {}", cdl, report);
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectExampleNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalMast.class);

}
