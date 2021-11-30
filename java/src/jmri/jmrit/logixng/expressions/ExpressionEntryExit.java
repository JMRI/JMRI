package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * This expression sets the state of a DestinationPoints.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionEntryExit extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<DestinationPoints> _entryDestinationPoints;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private EntryExitState _entryExitState = EntryExitState.Active;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;

    public ExpressionEntryExit(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionEntryExit copy = new ExpressionEntryExit(sysName, userName);
        copy.setComment(getComment());
        if (_entryDestinationPoints != null) copy.setDestinationPoints(_entryDestinationPoints);
        copy.setBeanState(_entryExitState);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.set_Is_IsNot(_is_IsNot);
        copy.setStateAddressing(_stateAddressing);
        copy.setStateFormula(_stateFormula);
        copy.setStateLocalVariable(_stateLocalVariable);
        copy.setStateReference(_stateReference);
        return manager.registerExpression(copy);
    }

    public void setDestinationPoints(@Nonnull String entryExitName) {
        assertListenersAreNotRegistered(log, "setEntryExit");
        DestinationPoints destinationPoints =
                InstanceManager.getDefault(EntryExitPairs.class).getNamedBean(entryExitName);
        if (destinationPoints != null) {
            ExpressionEntryExit.this.setDestinationPoints(destinationPoints);
        } else {
            removeDestinationPoints();
            log.warn("destinationPoints \"{}\" is not found", entryExitName);
        }
    }

    public void setDestinationPoints(@Nonnull NamedBeanHandle<DestinationPoints> handle) {
        assertListenersAreNotRegistered(log, "setEntryExit");
        _entryDestinationPoints = handle;
        InstanceManager.getDefault(EntryExitPairs.class).addVetoableChangeListener(this);
    }

    public void setDestinationPoints(@Nonnull DestinationPoints destinationPoints) {
        assertListenersAreNotRegistered(log, "setEntryExit");
        ExpressionEntryExit.this.setDestinationPoints(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(destinationPoints.getDisplayName(), destinationPoints));
    }

    public void removeDestinationPoints() {
        assertListenersAreNotRegistered(log, "setEntryExit");
        if (_entryDestinationPoints != null) {
            InstanceManager.getDefault(EntryExitPairs.class).removeVetoableChangeListener(this);
            _entryDestinationPoints = null;
        }
    }

    public NamedBeanHandle<DestinationPoints> getEntryExit() {
        return _entryDestinationPoints;
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

    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }

    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }

    public void setStateAddressing(NamedBeanAddressing addressing) throws ParserException {
        _stateAddressing = addressing;
        parseStateFormula();
    }

    public NamedBeanAddressing getStateAddressing() {
        return _stateAddressing;
    }

    public void setBeanState(EntryExitState state) {
        _entryExitState = state;
    }

    public EntryExitState getBeanState() {
        return _entryExitState;
    }

    public void setStateReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _stateReference = reference;
    }

    public String getStateReference() {
        return _stateReference;
    }

    public void setStateLocalVariable(@Nonnull String localVariable) {
        _stateLocalVariable = localVariable;
    }

    public String getStateLocalVariable() {
        return _stateLocalVariable;
    }

    public void setStateFormula(@Nonnull String formula) throws ParserException {
        _stateFormula = formula;
        parseStateFormula();
    }

    public String getStateFormula() {
        return _stateFormula;
    }

    private void parseStateFormula() throws ParserException {
        if (_stateAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _stateExpressionNode = parser.parseExpression(_stateFormula);
        } else {
            _stateExpressionNode = null;
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof DestinationPoints) {
                if (evt.getOldValue().equals(getEntryExit().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("EntryExit_EntryExitInUseEntryExitExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof DestinationPoints) {
                if (evt.getOldValue().equals(getEntryExit().getBean())) {
                    removeDestinationPoints();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private String getNewState() throws JmriException {

        switch (_stateAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _stateReference);

            case LocalVariable:
                SymbolTable symbolTable =
                        getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_stateLocalVariable), false);

            case Formula:
                return _stateExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _stateExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _stateAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        DestinationPoints destinationPoints;

//        System.out.format("ExpressionEntryExit.execute: %s%n", getLongDescription());

        switch (_addressing) {
            case Direct:
                destinationPoints = _entryDestinationPoints != null ? _entryDestinationPoints.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                destinationPoints = InstanceManager.getDefault(EntryExitPairs.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable =
                        getConditionalNG().getSymbolTable();
                destinationPoints = InstanceManager.getDefault(EntryExitPairs.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                destinationPoints = _expressionNode != null ?
                        InstanceManager.getDefault(EntryExitPairs.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

//        System.out.format("ExpressionEntryExit.execute: destinationPoints: %s%n", destinationPoints);

        if (destinationPoints == null) {
//            log.warn("destinationPoints is null");
            return false;
        }

        EntryExitState checkEntryExitState;

        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            checkEntryExitState = _entryExitState;
        } else {
            checkEntryExitState = EntryExitState.valueOf(getNewState());
        }

        EntryExitState currentEntryExitState = EntryExitState.get(destinationPoints.getState());
        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return currentEntryExitState == checkEntryExitState;
        } else {
            return currentEntryExitState != checkEntryExitState;
        }
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
        return Bundle.getMessage(locale, "EntryExit_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String state;

        switch (_addressing) {
            case Direct:
                String entryExitName;
                if (_entryDestinationPoints != null) {
                    entryExitName = _entryDestinationPoints.getBean().getDisplayName();
                } else {
                    entryExitName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", entryExitName);
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

        switch (_stateAddressing) {
            case Direct:
                state = Bundle.getMessage(locale, "AddressByDirect", _entryExitState._text);
                break;

            case Reference:
                state = Bundle.getMessage(locale, "AddressByReference", _stateReference);
                break;

            case LocalVariable:
                state = Bundle.getMessage(locale, "AddressByLocalVariable", _stateLocalVariable);
                break;

            case Formula:
                state = Bundle.getMessage(locale, "AddressByFormula", _stateFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _stateAddressing.name());
        }

        return Bundle.getMessage(locale, "EntryExit_Long", namedBean, _is_IsNot.toString(), state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_entryDestinationPoints != null)) {
            _entryDestinationPoints.getBean().addPropertyChangeListener("active", this);
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _entryDestinationPoints.getBean().removePropertyChangeListener("active", this);
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

    public enum EntryExitState {
        Inactive(0x04, Bundle.getMessage("EntryExitStateInactive")),
        Active(0x02, Bundle.getMessage("EntryExitStateActive")),
        Other(-1, Bundle.getMessage("EntryExitOtherStatus"));

        private final int _id;
        private final String _text;

        private EntryExitState(int id, String text) {
            this._id = id;
            this._text = text;
        }

        static public EntryExitState get(int id) {
            switch (id) {
                case 0x04:
                    return Inactive;

                case 0x02:
                    return Active;

                default:
                    return Other;
            }
        }

        public int getID() {
            return _id;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ExpressionEntryExit: bean = {}, report = {}", cdl, report);
        if (getEntryExit() != null && bean.equals(getEntryExit().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGExpression", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionEntryExit.class);

}
