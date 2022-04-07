package jmri.jmrit.logixng.actions;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
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
public class ActionTurnoutLock extends AbstractDigitalAction {

    private final LogixNG_SelectNamedBean<Turnout> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Turnout.class, InstanceManager.getDefault(TurnoutManager.class));
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
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setTurnoutLock(_turnoutLock);
        copy.setLockAddressing(_lockAddressing);
        copy.setLockFormula(_stateFormula);
        copy.setLockLocalVariable(_stateLocalVariable);
        copy.setLockReference(_stateReference);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Turnout> getSelectNamedBean() {
        return _selectNamedBean;
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

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
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
        Turnout turnout = _selectNamedBean.evaluateNamedBean(getConditionalNG());

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
        String namedBean = _selectNamedBean.getDescription(locale);
        String state;

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
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnoutLock.class);

}
