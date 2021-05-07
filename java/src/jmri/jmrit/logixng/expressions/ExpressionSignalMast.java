package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
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

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<SignalMast> _signalMastHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

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

    private NamedBeanHandle<SignalMast> _exampleSignalMastHandle;


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
        if (_signalMastHandle != null) copy.setSignalMast(_signalMastHandle);
        copy.setAspect(_signalMastAspect);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setQueryAddressing(_queryAddressing);
        copy.setQueryType(_queryType);
        copy.setQueryFormula(_queryFormula);
        copy.setQueryLocalVariable(_queryLocalVariable);
        copy.setQueryReference(_queryReference);
        copy.setAspectAddressing(_aspectAddressing);
        copy.setAspectFormula(_aspectFormula);
        copy.setAspectLocalVariable(_aspectLocalVariable);
        copy.setAspectReference(_aspectReference);
        copy.setExampleSignalMast(_exampleSignalMastHandle);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public void setSignalMast(@Nonnull String signalMastName) {
        assertListenersAreNotRegistered(log, "setSignalMast");
        SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(signalMastName);
        if (signalMast != null) {
            setSignalMast(signalMast);
        } else {
            removeSignalMast();
            log.error("signalMast \"{}\" is not found", signalMastName);
        }
    }

    public void setSignalMast(@Nonnull NamedBeanHandle<SignalMast> handle) {
        assertListenersAreNotRegistered(log, "setSignalMast");
        _signalMastHandle = handle;
        InstanceManager.getDefault(SignalMastManager.class).addVetoableChangeListener(this);
    }

    public void setSignalMast(@Nonnull SignalMast signalMast) {
        assertListenersAreNotRegistered(log, "setSignalMast");
        setSignalMast(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(signalMast.getDisplayName(), signalMast));
    }

    public void removeSignalMast() {
        assertListenersAreNotRegistered(log, "setSignalMast");
        if (_signalMastHandle != null) {
            InstanceManager.getDefault(SignalMastManager.class).removeVetoableChangeListener(this);
            _signalMastHandle = null;
        }
    }

    public NamedBeanHandle<SignalMast> getSignalMast() {
        return _signalMastHandle;
    }

    public void setExampleSignalMast(@Nonnull String signalMastName) {
        assertListenersAreNotRegistered(log, "setExampleSignalMast");
        SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(signalMastName);
        if (signalMast != null) {
            setExampleSignalMast(signalMast);
        } else {
            removeExampleSignalMast();
            log.error("signalMast \"{}\" is not found", signalMastName);
        }
    }

    public void setExampleSignalMast(@Nonnull NamedBeanHandle<SignalMast> handle) {
        assertListenersAreNotRegistered(log, "setExampleSignalMast");
        _exampleSignalMastHandle = handle;
        InstanceManager.getDefault(SignalMastManager.class).addVetoableChangeListener(this);
    }

    public void setExampleSignalMast(@Nonnull SignalMast signalMast) {
        assertListenersAreNotRegistered(log, "setExampleSignalMast");
        setExampleSignalMast(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(signalMast.getDisplayName(), signalMast));
    }

    public void removeExampleSignalMast() {
        assertListenersAreNotRegistered(log, "removeExampleSignalMast");
        if (_exampleSignalMastHandle != null) {
            InstanceManager.getDefault(SignalMastManager.class).removeVetoableChangeListener(this);
            _exampleSignalMastHandle = null;
        }
    }

    public NamedBeanHandle<SignalMast> getExampleSignalMast() {
        return _exampleSignalMastHandle;
    }

    public void setAddressing(NamedBeanAddressing addressing) throws ParserException {
        _addressing = addressing;
        parseFormula();
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
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

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalMast) {
                if ((_signalMastHandle != null)
                        && (evt.getOldValue().equals(_signalMastHandle.getBean()))) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("SignalMast_SignalMastInUseSignalMastExpressionVeto", getDisplayName()), e); // NOI18N
                }
                if ((_exampleSignalMastHandle != null)
                        && (evt.getOldValue().equals(_exampleSignalMastHandle.getBean()))) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("SignalMast_SignalMastInUseSignalMastExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalMast) {
                if ((_signalMastHandle != null)
                        && (evt.getOldValue().equals(_signalMastHandle.getBean()))) {
                    removeSignalMast();
                }
                if ((_exampleSignalMastHandle != null)
                        && (evt.getOldValue().equals(_exampleSignalMastHandle.getBean()))) {
                    removeExampleSignalMast();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }

    private String getNewAspect() throws JmriException {

        switch (_aspectAddressing) {
            case Direct:
                return _signalMastAspect;

            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _aspectReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_aspectLocalVariable), false);

            case Formula:
                return _aspectExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _aspectExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : "";

            default:
                throw new IllegalArgumentException("invalid _aspectAddressing state: " + _aspectAddressing.name());
        }
    }

    private QueryType getQuery() throws JmriException {

        String oper = "";
        try {
            switch (_queryAddressing) {
                case Direct:
                    return _queryType;

                case Reference:
                    oper = ReferenceUtil.getReference(
                            getConditionalNG().getSymbolTable(), _queryReference);
                    return QueryType.valueOf(oper);

                case LocalVariable:
                    SymbolTable symbolTable =
                            getConditionalNG().getSymbolTable();
                    oper = TypeConversionUtil
                            .convertToString(symbolTable.getValue(_queryLocalVariable), false);
                    return QueryType.valueOf(oper);

                case Formula:
                    if (_aspectExpressionNode != null) {
                        oper = TypeConversionUtil.convertToString(
                                _queryExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false);
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
        SignalMast signalMast;

        switch (_addressing) {
            case Direct:
                signalMast = _signalMastHandle != null ? _signalMastHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                signalMast = InstanceManager.getDefault(SignalMastManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                signalMast = InstanceManager.getDefault(SignalMastManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                signalMast = _expressionNode != null ?
                        InstanceManager.getDefault(SignalMastManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        if (signalMast == null) {
//            log.error("signalMast is null");
            return false;
        }

        QueryType query = getQuery();

        boolean result = false;

        switch (query) {
            case Aspect:
                if (_signalMastHandle.getBean().getAspect() != null) {
                    result = getNewAspect().equals(_signalMastHandle.getBean().getAspect());
                }
                break;
            case NotAspect:
                if (_signalMastHandle.getBean().getAspect() != null) {
                    result = ! getNewAspect().equals(_signalMastHandle.getBean().getAspect());
                }
                break;
            case Lit:
                result = _signalMastHandle.getBean().getLit();
                break;
            case NotLit:
                result = ! _signalMastHandle.getBean().getLit();
                break;
            case Held:
                result = _signalMastHandle.getBean().getHeld();
                break;
            case NotHeld:
                result = ! _signalMastHandle.getBean().getHeld();
                break;
            case IsPermissiveSmlDisabled:
                result = _signalMastHandle.getBean().isPermissiveSmlDisabled();
                break;
            case IsPermissiveSmlNotDisabled:
                result = ! _signalMastHandle.getBean().isPermissiveSmlDisabled();
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
        String namedBean;
        String query;
        String aspect;

        switch (_addressing) {
            case Direct:
                String sensorName;
                if (_signalMastHandle != null) {
                    sensorName = _signalMastHandle.getBean().getDisplayName();
                } else {
                    sensorName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", sensorName);
                break;

            case Reference:
                namedBean = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case LocalVariable:
                namedBean = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                namedBean = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

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
        if (!_listenersAreRegistered && (_signalMastHandle != null)) {

            switch (_queryType) {
                case Aspect:
                case NotAspect:
                    _signalMastHandle.getBean().addPropertyChangeListener("Aspect", this);
                    break;

                case Lit:
                case NotLit:
                    _signalMastHandle.getBean().addPropertyChangeListener("Lit", this);
                    break;

                case Held:
                case NotHeld:
                    _signalMastHandle.getBean().addPropertyChangeListener("Held", this);
                    break;

                case IsPermissiveSmlDisabled:
                case IsPermissiveSmlNotDisabled:
                    _signalMastHandle.getBean().removePropertyChangeListener("PermissiveSmlDisabled", this);
                    break;

                default:
                    throw new RuntimeException("Unknown enum: "+_queryType.name());
            }
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {

            switch (_queryType) {
                case Aspect:
                case NotAspect:
                    _signalMastHandle.getBean().removePropertyChangeListener("Aspect", this);
                    break;

                case Lit:
                case NotLit:
                    _signalMastHandle.getBean().removePropertyChangeListener("Lit", this);
                    break;

                case Held:
                case NotHeld:
                    _signalMastHandle.getBean().removePropertyChangeListener("Held", this);
                    break;

                case IsPermissiveSmlDisabled:
                case IsPermissiveSmlNotDisabled:
                    _signalMastHandle.getBean().removePropertyChangeListener("PermissiveSmlDisabled", this);
                    break;

                default:
                    throw new RuntimeException("Unknown enum: "+_queryType.name());
            }
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (getTriggerOnChange()) {
            getConditionalNG().execute();
        }
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
        if (getSignalMast() != null && bean.equals(getSignalMast().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGExpression", cdl, getLongDescription()));
        }
        if (getExampleSignalMast() != null && bean.equals(getExampleSignalMast().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGExpression", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalMast.class);

}
