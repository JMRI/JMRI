package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.Audio;
import jmri.AudioManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * This expression evaluates the state of an Audio.
 *
 * @author Daniel Bergqvist Copyright 2023
 */
public class ExpressionAudio extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Audio> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Audio.class, InstanceManager.getDefault(AudioManager.class), this);

    private boolean _hasChangedState = false;

    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;

    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private AudioState _audioState = AudioState.Initial;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;

    private boolean _checkOnlyOnChange;


    public ExpressionAudio(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionAudio copy = new ExpressionAudio(sysName, userName);
        copy.setComment(getComment());

        _selectNamedBean.copy(copy._selectNamedBean);

        copy.set_Is_IsNot(_is_IsNot);

        copy.setStateAddressing(_stateAddressing);
        copy.setBeanState(_audioState);
        copy.setStateReference(_stateReference);
        copy.setStateLocalVariable(_stateLocalVariable);
        copy.setStateFormula(_stateFormula);

        copy.setCheckOnlyOnChange(_checkOnlyOnChange);

        return manager.registerExpression(copy);
    }

    public LogixNG_SelectNamedBean<Audio> getSelectNamedBean() {
        return _selectNamedBean;
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

    public void setBeanState(AudioState state) {
        _audioState = state;
    }

    public AudioState getBeanState() {
        return _audioState;
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


    public void setCheckOnlyOnChange(boolean triggerOnlyOnChange) {
        _checkOnlyOnChange = triggerOnlyOnChange;
    }

    public boolean isCheckOnlyOnChange() {
        return _checkOnlyOnChange;
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
        Audio audio = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (audio == null) return false;

        AudioState checkAudioState;

        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            checkAudioState = _audioState;
        } else {
            checkAudioState = AudioState.valueOf(getNewState());
        }

        int currentState = audio.getState();

        if (_checkOnlyOnChange && !_hasChangedState) {
            return false;
        }

        _hasChangedState = false;

        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return currentState == checkAudioState.getID();
        } else {
            return currentState != checkAudioState.getID();
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
        return Bundle.getMessage(locale, "Audio_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state;

        switch (_stateAddressing) {
            case Direct:
                state = Bundle.getMessage(locale, "AddressByDirect", _audioState._text);
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


        if (_checkOnlyOnChange) {
            return Bundle.getMessage(locale, "Audio_Long4", namedBean, _is_IsNot.toString(), state, Bundle.getMessage(locale, "Audio_CheckOnlyOnChange"));
        } else {
            return Bundle.getMessage(locale, "Audio_Long3", namedBean, _is_IsNot.toString(), state);
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
            _selectNamedBean.addPropertyChangeListener(this);
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _selectNamedBean.removePropertyChangeListener(this);
            _selectNamedBean.unregisterListeners();
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!Objects.equals(evt.getNewValue(), evt.getOldValue())) {
            _hasChangedState = true;
        }
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    public enum AudioState {
        Initial(Audio.STATE_INITIAL, Bundle.getMessage("Audio_StateInitial")),
        Stopped(Audio.STATE_STOPPED, Bundle.getMessage("Audio_StateStopped")),
        Playing(Audio.STATE_PLAYING, Bundle.getMessage("Audio_StatePlaying")),
        Empty(Audio.STATE_EMPTY, Bundle.getMessage("Audio_StateEmpty")),
        Loaded(Audio.STATE_LOADED, Bundle.getMessage("Audio_StateLoaded")),
        Positioned(Audio.STATE_POSITIONED, Bundle.getMessage("Audio_StatePositioned")),
        Moving(Audio.STATE_MOVING, Bundle.getMessage("Audio_StateMoving"));

        private final int _id;
        private final String _text;

        private AudioState(int id, String text) {
            this._id = id;
            this._text = text;
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
        log.debug("getUsageReport :: ExpressionAudio: bean = {}, report = {}", cdl, report);
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Expression);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionAudio.class);

}
