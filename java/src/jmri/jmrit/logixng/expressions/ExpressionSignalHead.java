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
 * Evaluates the state of a SignalHead.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionSignalHead extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private final LogixNG_SelectNamedBean<SignalHead> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, SignalHead.class, InstanceManager.getDefault(SignalHeadManager.class), this);

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

    private final LogixNG_SelectNamedBean<SignalHead> _selectExampleNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, SignalHead.class, InstanceManager.getDefault(SignalHeadManager.class), this);


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
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setAppearance(_signalHeadAppearance);
        copy.setQueryAddressing(_queryAddressing);
        copy.setQueryType(_queryType);
        copy.setQueryFormula(_queryFormula);
        copy.setQueryLocalVariable(_queryLocalVariable);
        copy.setQueryReference(_queryReference);
        copy.setAppearanceAddressing(_appearanceAddressing);
        copy.setAppearanceFormula(_appearanceFormula);
        copy.setAppearanceLocalVariable(_appearanceLocalVariable);
        copy.setAppearanceReference(_appearanceReference);
        _selectExampleNamedBean.copy(copy._selectExampleNamedBean);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<SignalHead> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectNamedBean<SignalHead> getSelectExampleNamedBean() {
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

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    private int getAppearanceFromName(String name, SignalHead signalHead) {
        if (signalHead == null) throw new UnsupportedOperationException("_signalHeadHandle is null");

        String[] keys = signalHead.getValidStateKeys();
        for (int i=0; i < keys.length; i++) {
            if (name.equals(keys[i])) return signalHead.getValidStates()[i];
        }

        throw new IllegalArgumentException("Appearance "+name+" is not valid for signal head "+signalHead.getSystemName());
    }

    private int getNewAppearance(ConditionalNG conditionalNG, SignalHead signalHead)
            throws JmriException {

        switch (_appearanceAddressing) {
            case Direct:
                return _signalHeadAppearance;

            case Reference:
                return getAppearanceFromName(ReferenceUtil.getReference(
                        conditionalNG.getSymbolTable(), _appearanceReference), signalHead);

            case LocalVariable:
                SymbolTable symbolTable = conditionalNG.getSymbolTable();
                return getAppearanceFromName(TypeConversionUtil
                        .convertToString(symbolTable.getValue(_appearanceLocalVariable), false), signalHead);

            case Formula:
                return _appearanceExpressionNode != null
                        ? getAppearanceFromName(TypeConversionUtil.convertToString(
                                _appearanceExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false), signalHead)
                        : -1;

            default:
                throw new IllegalArgumentException("invalid _aspectAddressing state: " + _appearanceAddressing.name());
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
                    if (_appearanceExpressionNode != null) {
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

        SignalHead signalHead = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (signalHead == null) return false;

        QueryType query = getQuery(conditionalNG);

        boolean result = false;
        int queryAppearance;

        switch (query) {
            case Appearance:
                queryAppearance = getNewAppearance(conditionalNG, signalHead);
                if (queryAppearance != -1) {
                    result = signalHead.getAppearance() == queryAppearance;
                }
                break;
            case NotAppearance:
                queryAppearance = getNewAppearance(conditionalNG, signalHead);
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
        String namedBean = _selectNamedBean.getDescription(locale);
        String query;
        String appearance;

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
                SignalHead signalHead = null;
                if (_selectNamedBean.getAddressing() == NamedBeanAddressing.Direct) {
                    if (_selectNamedBean.getNamedBean() != null) {
                        signalHead = _selectNamedBean.getNamedBeanIfDirectAddressing();
                    }
                } else {
                    if (_selectExampleNamedBean.getNamedBean() != null) {
                        signalHead = _selectExampleNamedBean.getNamedBeanIfDirectAddressing();
                    }
                }
                String a = "";
                if (signalHead != null) {
                    a = signalHead.getAppearanceName(_signalHeadAppearance);
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
        SignalHead signalHead = _selectNamedBean.getNamedBeanIfDirectAddressing();

       if (!_listenersAreRegistered && (signalHead != null)) {
            switch (_queryType) {
                case Appearance:
                case NotAppearance:
                    signalHead.addPropertyChangeListener("Appearance", this);
                    break;

                case Lit:
                case NotLit:
                    signalHead.addPropertyChangeListener("Lit", this);
                    break;

                case Held:
                case NotHeld:
                    signalHead.addPropertyChangeListener("Held", this);
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
        SignalHead signalHead = _selectNamedBean.getNamedBeanIfDirectAddressing();

       if (_listenersAreRegistered && (signalHead != null)) {
            switch (_queryType) {
                case Appearance:
                case NotAppearance:
                    signalHead.removePropertyChangeListener("Appearance", this);
                    break;

                case Lit:
                case NotLit:
                    signalHead.removePropertyChangeListener("Lit", this);
                    break;

                case Held:
                case NotHeld:
                    signalHead.removePropertyChangeListener("Held", this);
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
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectExampleNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSignalHead.class);

}
