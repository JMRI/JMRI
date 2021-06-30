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
 * Evaluates the state of a SignalHead.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionSignalHead extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<SignalHead> _signalHeadHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

    private NamedBeanAddressing _queryAddressing = NamedBeanAddressing.Direct;
    private QueryType _queryType = QueryType.Appearance;
    private String _queryReference = "";
    private String _queryLocalVariable = "";
    private String _queryFormula = "";
    private ExpressionNode _queryExpressionNode;

    private NamedBeanAddressing _appearanceAddressing = NamedBeanAddressing.Direct;
    private int _signalHeadAppearance = SignalHead.DARK;
    private String _appearanceReference = "";
    private String _appearanceLocalVariable = "";
    private String _appearanceFormula = "";
    private ExpressionNode _appearanceExpressionNode;

    private NamedBeanHandle<SignalHead> _exampleSignalHeadHandle;


    public ExpressionSignalHead(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionSignalHead copy = new ExpressionSignalHead(sysName, userName);
        copy.setComment(getComment());
        if (_signalHeadHandle != null) copy.setSignalHead(_signalHeadHandle);
        copy.setAppearance(_signalHeadAppearance);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setQueryAddressing(_queryAddressing);
        copy.setQueryType(_queryType);
        copy.setQueryFormula(_queryFormula);
        copy.setQueryLocalVariable(_queryLocalVariable);
        copy.setQueryReference(_queryReference);
        copy.setAppearanceAddressing(_appearanceAddressing);
        copy.setAppearanceFormula(_appearanceFormula);
        copy.setAppearanceLocalVariable(_appearanceLocalVariable);
        copy.setAppearanceReference(_appearanceReference);
        copy.setExampleSignalHead(_exampleSignalHeadHandle);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public void setSignalHead(@Nonnull String signalHeadName) {
        assertListenersAreNotRegistered(log, "setSignalHead");
        SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalHeadName);
        if (signalHead != null) {
            setSignalHead(signalHead);
        } else {
            removeSignalHead();
            log.error("signalHead \"{}\" is not found", signalHeadName);
        }
    }

    public void setSignalHead(@Nonnull NamedBeanHandle<SignalHead> handle) {
        assertListenersAreNotRegistered(log, "setSignalHead");
        _signalHeadHandle = handle;
        InstanceManager.getDefault(SignalHeadManager.class).addVetoableChangeListener(this);
    }

    public void setSignalHead(@Nonnull SignalHead signalHead) {
        assertListenersAreNotRegistered(log, "setSignalHead");
        setSignalHead(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(signalHead.getDisplayName(), signalHead));
    }

    public void removeSignalHead() {
        assertListenersAreNotRegistered(log, "setSignalHead");
        if (_signalHeadHandle != null) {
            InstanceManager.getDefault(SignalHeadManager.class).removeVetoableChangeListener(this);
            _signalHeadHandle = null;
        }
    }

    public NamedBeanHandle<SignalHead> getSignalHead() {
        return _signalHeadHandle;
    }

    public void setExampleSignalHead(@Nonnull String signalHeadName) {
        assertListenersAreNotRegistered(log, "setExampleSignalHead");
        SignalHead signalHead = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalHeadName);
        if (signalHead != null) {
            setExampleSignalHead(signalHead);
        } else {
            removeExampleSignalHead();
            log.error("signalHead \"{}\" is not found", signalHeadName);
        }
    }

    public void setExampleSignalHead(@Nonnull NamedBeanHandle<SignalHead> handle) {
        assertListenersAreNotRegistered(log, "setExampleSignalHead");
        _exampleSignalHeadHandle = handle;
        InstanceManager.getDefault(SignalHeadManager.class).addVetoableChangeListener(this);
    }

    public void setExampleSignalHead(@Nonnull SignalHead signalHead) {
        assertListenersAreNotRegistered(log, "setExampleSignalHead");
        setExampleSignalHead(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(signalHead.getDisplayName(), signalHead));
    }

    public void removeExampleSignalHead() {
        assertListenersAreNotRegistered(log, "removeExampleSignalHead");
        if (_exampleSignalHeadHandle != null) {
            InstanceManager.getDefault(SignalHeadManager.class).removeVetoableChangeListener(this);
            _exampleSignalHeadHandle = null;
        }
    }

    public NamedBeanHandle<SignalHead> getExampleSignalHead() {
        return _exampleSignalHeadHandle;
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

    public void setAppearanceAddressing(NamedBeanAddressing addressing) throws ParserException {
        _appearanceAddressing = addressing;
        parseAppearanceFormula();
    }

    public NamedBeanAddressing getAppearanceAddressing() {
        return _appearanceAddressing;
    }

    public void setAppearance(int appearance) {
        _signalHeadAppearance = appearance;
    }

    public int getAppearance() {
        return _signalHeadAppearance;
    }

    public void setAppearanceReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _appearanceReference = reference;
    }

    public String getAppearanceReference() {
        return _appearanceReference;
    }

    public void setAppearanceLocalVariable(@Nonnull String localVariable) {
        _appearanceLocalVariable = localVariable;
    }

    public String getAppearanceLocalVariable() {
        return _appearanceLocalVariable;
    }

    public void setAppearanceFormula(@Nonnull String formula) throws ParserException {
        _appearanceFormula = formula;
        parseAppearanceFormula();
    }

    public String getAppearanceFormula() {
        return _appearanceFormula;
    }

    private void parseAppearanceFormula() throws ParserException {
        if (_appearanceAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _appearanceExpressionNode = parser.parseExpression(_appearanceFormula);
        } else {
            _appearanceExpressionNode = null;
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalHead) {
                if ((_signalHeadHandle != null)
                        && (evt.getOldValue().equals(_signalHeadHandle.getBean()))) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("SignalHead_SignalHeadInUseSignalHeadExpressionVeto", getDisplayName()), e); // NOI18N
                }
                if ((_exampleSignalHeadHandle != null)
                        && (evt.getOldValue().equals(_exampleSignalHeadHandle.getBean()))) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("SignalHead_SignalHeadInUseSignalHeadExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof SignalHead) {
                if ((_signalHeadHandle != null)
                        && (evt.getOldValue().equals(_signalHeadHandle.getBean()))) {
                    removeSignalHead();
                }
                if ((_exampleSignalHeadHandle != null)
                        && (evt.getOldValue().equals(_exampleSignalHeadHandle.getBean()))) {
                    removeExampleSignalHead();
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

    private int getAppearanceFromName(String name) {
        if (_signalHeadHandle == null) throw new UnsupportedOperationException("_signalHeadHandle is null");

        SignalHead sh = _signalHeadHandle.getBean();
        String[] keys = sh.getValidStateKeys();
        for (int i=0; i < keys.length; i++) {
            if (name.equals(keys[i])) return sh.getValidStates()[i];
        }

        throw new IllegalArgumentException("Appearance "+name+" is not valid for signal head "+sh.getSystemName());
    }

    private int getNewAppearance() throws JmriException {

        switch (_appearanceAddressing) {
            case Direct:
                return _signalHeadAppearance;

            case Reference:
                return getAppearanceFromName(ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _appearanceReference));

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return getAppearanceFromName(TypeConversionUtil
                        .convertToString(symbolTable.getValue(_appearanceLocalVariable), false));

            case Formula:
                return _appearanceExpressionNode != null
                        ? getAppearanceFromName(TypeConversionUtil.convertToString(
                                _appearanceExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false))
                        : -1;

            default:
                throw new IllegalArgumentException("invalid _aspectAddressing state: " + _appearanceAddressing.name());
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
                    if (_appearanceExpressionNode != null) {
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
        SignalHead signalHead;

        switch (_addressing) {
            case Direct:
                signalHead = _signalHeadHandle != null ? _signalHeadHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                signalHead = InstanceManager.getDefault(SignalHeadManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                signalHead = InstanceManager.getDefault(SignalHeadManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                signalHead = _expressionNode != null ?
                        InstanceManager.getDefault(SignalHeadManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        if (signalHead == null) {
//            log.error("signalHead is null");
            return false;
        }

        QueryType query = getQuery();

        boolean result = false;
        int queryAppearance;

        switch (query) {
            case Appearance:
                queryAppearance = getNewAppearance();
                if (queryAppearance != -1) {
                    result = signalHead.getAppearance() == queryAppearance;
                }
                break;
            case NotAppearance:
                queryAppearance = getNewAppearance();
                if (queryAppearance != -1) {
                    result = ! (signalHead.getAppearance() == queryAppearance);
                }
                break;
            case Lit:
                result = signalHead.getLit();
                break;
            case NotLit:
                result = ! signalHead.getLit();
                break;
            case Held:
                result = signalHead.getHeld();
                break;
            case NotHeld:
                result = ! signalHead.getHeld();
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
        return Bundle.getMessage(locale, "SignalHead_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String query;
        String appearance;

        switch (_addressing) {
            case Direct:
                String sensorName;
                if (_signalHeadHandle != null) {
                    sensorName = _signalHeadHandle.getBean().getDisplayName();
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

        switch (_appearanceAddressing) {
            case Direct:
                String a = "";
                if ((_signalHeadHandle != null) && (_signalHeadHandle.getBean() != null)) {
                    a = _signalHeadHandle.getBean().getAppearanceName(_signalHeadAppearance);
                }
                appearance = Bundle.getMessage(locale, "AddressByDirect", a);
                break;

            case Reference:
                appearance = Bundle.getMessage(locale, "AddressByReference", _appearanceReference);
                break;

            case LocalVariable:
                appearance = Bundle.getMessage(locale, "AddressByLocalVariable", _appearanceLocalVariable);
                break;

            case Formula:
                appearance = Bundle.getMessage(locale, "AddressByFormula", _appearanceFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _appearanceAddressing.name());
        }

        if (_queryAddressing == NamedBeanAddressing.Direct) {
            if (_queryType == QueryType.Appearance) {
                return Bundle.getMessage(locale, "SignalHead_LongAppearance", namedBean, appearance);
            } else if (_queryType == QueryType.NotAppearance) {
                return Bundle.getMessage(locale, "SignalHead_LongNotAppearance", namedBean, appearance);
            } else {
                return Bundle.getMessage(locale, "SignalHead_Long", namedBean, query);
            }
        } else {
            return Bundle.getMessage(locale, "SignalHead_LongUnknownOper", namedBean, query, appearance);
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
        if (!_listenersAreRegistered && (_signalHeadHandle != null)) {

            switch (_queryType) {
                case Appearance:
                case NotAppearance:
                    _signalHeadHandle.getBean().addPropertyChangeListener("Appearance", this);
                    break;

                case Lit:
                case NotLit:
                    _signalHeadHandle.getBean().addPropertyChangeListener("Lit", this);
                    break;

                case Held:
                case NotHeld:
                    _signalHeadHandle.getBean().addPropertyChangeListener("Held", this);
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
                case Appearance:
                case NotAppearance:
                    _signalHeadHandle.getBean().removePropertyChangeListener("Appearance", this);
                    break;

                case Lit:
                case NotLit:
                    _signalHeadHandle.getBean().removePropertyChangeListener("Lit", this);
                    break;

                case Held:
                case NotHeld:
                    _signalHeadHandle.getBean().removePropertyChangeListener("Held", this);
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
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }



    public enum QueryType {
        Appearance(Bundle.getMessage("SignalHeadQueryType_Appearance")),
        NotAppearance(Bundle.getMessage("SignalHeadQueryType_NotAppearance")),
        Lit(Bundle.getMessage("SignalHeadQueryType_Lit")),
        NotLit(Bundle.getMessage("SignalHeadQueryType_NotLit")),
        Held(Bundle.getMessage("SignalHeadQueryType_Held")),
        NotHeld(Bundle.getMessage("SignalHeadQueryType_NotHeld"));

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
        log.debug("getUsageReport :: ExpressionSignalHead: bean = {}, report = {}", cdl, report);
        if (getSignalHead() != null && bean.equals(getSignalHead().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGExpression", cdl, getLongDescription()));
        }
        if (getExampleSignalHead() != null && bean.equals(getExampleSignalHead().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGExpression", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalHead.class);

}
