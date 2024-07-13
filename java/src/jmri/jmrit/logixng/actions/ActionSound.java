package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.Sound;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * Plays a sound.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionSound extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectEnum<Operation> _selectEnum =
            new LogixNG_SelectEnum<>(this, Operation.values(), Operation.Play, this);

    private NamedBeanAddressing _soundAddressing = NamedBeanAddressing.Direct;
    private String _sound = "";
    private String _soundReference = "";
    private String _soundLocalVariable = "";
    private String _soundFormula = "";
    private ExpressionNode _soundExpressionNode;

    public ActionSound(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSound copy = new ActionSound(sysName, userName);
        copy.setComment(getComment());
        copy.setSound(_sound);
        _selectEnum.copy(copy._selectEnum);
        copy.setSoundAddressing(_soundAddressing);
        copy.setSoundFormula(_soundFormula);
        copy.setSoundLocalVariable(_soundLocalVariable);
        copy.setSoundReference(_soundReference);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectEnum<Operation> getSelectEnum() {
        return _selectEnum;
    }

    public void setSoundAddressing(NamedBeanAddressing addressing) throws ParserException {
        _soundAddressing = addressing;
        parseSoundFormula();
    }

    public NamedBeanAddressing getSoundAddressing() {
        return _soundAddressing;
    }

    public void setSound(String sound) {
        if (sound == null) _sound = "";
        else _sound = sound;
    }

    public String getSound() {
        return _sound;
    }

    public void setSoundReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _soundReference = reference;
    }

    public String getSoundReference() {
        return _soundReference;
    }

    public void setSoundLocalVariable(@Nonnull String localVariable) {
        _soundLocalVariable = localVariable;
    }

    public String getSoundLocalVariable() {
        return _soundLocalVariable;
    }

    public void setSoundFormula(@Nonnull String formula) throws ParserException {
        _soundFormula = formula;
        parseSoundFormula();
    }

    public String getSoundFormula() {
        return _soundFormula;
    }

    private void parseSoundFormula() throws ParserException {
        if (_soundAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _soundExpressionNode = parser.parseExpression(_soundFormula);
        } else {
            _soundExpressionNode = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private String getTheSound() throws JmriException {

        switch (_soundAddressing) {
            case Direct:
                return _sound;

            case Reference:
                return ReferenceUtil.getReference(getConditionalNG().getSymbolTable(), _soundReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_soundLocalVariable), false);

            case Formula:
                return _soundExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _soundExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : "";

            default:
                throw new IllegalArgumentException("invalid _soundAddressing state: " + _soundAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        Operation operation = _selectEnum.evaluateEnum(getConditionalNG());
        String path = getTheSound();

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            switch (operation) {
                case Play:
                    if (!path.equals("")) {
                        try {
                            new Sound(path).play(true);
                        } catch (NullPointerException ex) {
                            throw new JmriException(Bundle.getMessage("ActionSound_Error_SoundNotFound", path));
                        }
                    }
                    break;

                default:
                    throw new IllegalArgumentException("invalid operation: " + operation.name());
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
        return Bundle.getMessage(locale, "ActionSound_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String operation = _selectEnum.getDescription(locale);
        String sound;

        switch (_soundAddressing) {
            case Direct:
                sound = Bundle.getMessage(locale, "AddressByDirect", _sound);
                break;

            case Reference:
                sound = Bundle.getMessage(locale, "AddressByReference", _soundReference);
                break;

            case LocalVariable:
                sound = Bundle.getMessage(locale, "AddressByLocalVariable", _soundLocalVariable);
                break;

            case Formula:
                sound = Bundle.getMessage(locale, "AddressByFormula", _soundFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _soundAddressing.name());
        }

        if (_selectEnum.getAddressing() == NamedBeanAddressing.Direct) {
            if (_selectEnum.getEnum() == Operation.Play) {
                return Bundle.getMessage(locale, "ActionSound_Long_Play", sound);
            } else {
                return Bundle.getMessage(locale, "ActionSound_Long", operation, sound);
            }
        } else {
            return Bundle.getMessage(locale, "ActionSound_LongUnknownOper", operation, sound);
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
        if (!_listenersAreRegistered) {
            _listenersAreRegistered = true;
        }
        _selectEnum.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _listenersAreRegistered = false;
        }
        _selectEnum.unregisterListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void firePropertyChange(String p, Object old, Object n) {
        super.firePropertyChange(p, old, n);
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        // Do nothing
    }


    public enum Operation {
        Play(Bundle.getMessage("ActionSound_Operation_Play"));

        private final String _text;

        private Operation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSound.class);

}
