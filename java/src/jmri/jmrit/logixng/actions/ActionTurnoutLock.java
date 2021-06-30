package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
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
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action sets the lock of a turnout.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionTurnoutLock extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Turnout> _turnoutHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private NamedBeanAddressing _lockAddressing = NamedBeanAddressing.Direct;
    private TurnoutLock _turnoutLock = TurnoutLock.Unlock;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;

    public ActionTurnoutLock(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionTurnoutLock copy = new ActionTurnoutLock(sysName, userName);
        copy.setComment(getComment());
        if (_turnoutHandle != null) copy.setTurnout(_turnoutHandle);
        copy.setTurnoutLock(_turnoutLock);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setLockAddressing(_lockAddressing);
        copy.setLockFormula(_stateFormula);
        copy.setLockLocalVariable(_stateLocalVariable);
        copy.setLockReference(_stateReference);
        return manager.registerAction(copy);
    }

    public void setTurnout(@Nonnull String turnoutName) {
        assertListenersAreNotRegistered(log, "setTurnout");
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout(turnoutName);
        if (turnout != null) {
            setTurnout(turnout);
        } else {
            removeTurnout();
            log.error("turnout \"{}\" is not found", turnoutName);
        }
    }

    public void setTurnout(@Nonnull NamedBeanHandle<Turnout> handle) {
        assertListenersAreNotRegistered(log, "setTurnout");
        _turnoutHandle = handle;
        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    public void setTurnout(@Nonnull Turnout turnout) {
        assertListenersAreNotRegistered(log, "setTurnout");
        setTurnout(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(turnout.getDisplayName(), turnout));
    }

    public void removeTurnout() {
        assertListenersAreNotRegistered(log, "setTurnout");
        if (_turnoutHandle != null) {
            InstanceManager.turnoutManagerInstance().removeVetoableChangeListener(this);
            _turnoutHandle = null;
        }
    }

    public NamedBeanHandle<Turnout> getTurnout() {
        return _turnoutHandle;
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

    public void setLockAddressing(NamedBeanAddressing addressing) throws ParserException {
        _lockAddressing = addressing;
        parseLockFormula();
    }

    public NamedBeanAddressing getLockAddressing() {
        return _lockAddressing;
    }

    public void setTurnoutLock(TurnoutLock state) {
        _turnoutLock = state;
    }

    public TurnoutLock getTurnoutLock() {
        return _turnoutLock;
    }

    public void setLockReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _stateReference = reference;
    }

    public String getLockReference() {
        return _stateReference;
    }

    public void setLockLocalVariable(@Nonnull String localVariable) {
        _stateLocalVariable = localVariable;
    }

    public String getLockLocalVariable() {
        return _stateLocalVariable;
    }

    public void setLockFormula(@Nonnull String formula) throws ParserException {
        _stateFormula = formula;
        parseLockFormula();
    }

    public String getLockFormula() {
        return _stateFormula;
    }

    private void parseLockFormula() throws ParserException {
        if (_lockAddressing == NamedBeanAddressing.Formula) {
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
            if (evt.getOldValue() instanceof Turnout) {
                if (evt.getOldValue().equals(getTurnout().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("TurnoutLock_TurnoutInUseTurnoutActionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Turnout) {
                if (evt.getOldValue().equals(getTurnout().getBean())) {
                    removeTurnout();
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

    private String getNewLock() throws JmriException {

        switch (_lockAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _stateReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_stateLocalVariable), false);

            case Formula:
                return _stateExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _stateExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _lockAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Turnout turnout;

//        System.out.format("ActionTurnoutLock.execute: %s%n", getLongDescription());

        switch (_addressing) {
            case Direct:
                turnout = _turnoutHandle != null ? _turnoutHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                turnout = InstanceManager.getDefault(TurnoutManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                turnout = InstanceManager.getDefault(TurnoutManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                turnout = _expressionNode != null ?
                        InstanceManager.getDefault(TurnoutManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

//        System.out.format("ActionTurnoutLock.execute: turnout: %s%n", turnout);

        if (turnout == null) {
//            log.error("turnout is null");
            return;
        }

        String name = (_lockAddressing != NamedBeanAddressing.Direct)
                ? getNewLock() : null;

        TurnoutLock lock;
        if ((_lockAddressing == NamedBeanAddressing.Direct)) {
            lock = _turnoutLock;
        } else {
            lock = TurnoutLock.valueOf(name);
        }

        if (lock == TurnoutLock.Toggle) {
            if (turnout.getLocked(Turnout.CABLOCKOUT)) {
                lock = TurnoutLock.Unlock;
            } else {
                lock = TurnoutLock.Lock;
            }
        }

        // Variables used in lambda must be effectively final
        TurnoutLock theLock = lock;

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            if (theLock == TurnoutLock.Lock) {
                turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
            } else if (theLock == TurnoutLock.Unlock) {
                turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
            }
        });
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
        return Bundle.getMessage(locale, "TurnoutLock_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String state;

        switch (_addressing) {
            case Direct:
                String turnoutName;
                if (_turnoutHandle != null) {
                    turnoutName = _turnoutHandle.getBean().getDisplayName();
                } else {
                    turnoutName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", turnoutName);
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

        switch (_lockAddressing) {
            case Direct:
                state = Bundle.getMessage(locale, "AddressByDirect", _turnoutLock._text);
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
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _lockAddressing.name());
        }

        return Bundle.getMessage(locale, "TurnoutLock_Long", namedBean, state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public enum TurnoutLock {
        Lock(Bundle.getMessage("TurnoutLock_Lock")),
        Unlock(Bundle.getMessage("TurnoutLock_Unlock")),
        Toggle(Bundle.getMessage("TurnoutLock_Toggle"));

        private final String _text;

        private TurnoutLock(String text) {
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
        log.debug("getUsageReport :: ActionTurnoutLock: bean = {}, report = {}", cdl, report);
        if (getTurnout() != null && bean.equals(getTurnout().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnoutLock.class);

}
