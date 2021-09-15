package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.audio.AudioListener;
import jmri.jmrit.audio.AudioSource;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action controls an audio object.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionAudio extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Audio> _audioHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private Operation _operation = Operation.Play;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;

    public ActionAudio(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionAudio copy = new ActionAudio(sysName, userName);
        copy.setComment(getComment());
        if (_audioHandle != null) copy.setAudio(_audioHandle);
        copy.setOperation(_operation);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationFormula(_operationFormula);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationReference(_operationReference);
        return manager.registerAction(copy);
    }

    public void setAudio(@Nonnull String audioName) {
        assertListenersAreNotRegistered(log, "setAudio");
        Audio audio = InstanceManager.getDefault(AudioManager.class).getAudio(audioName);
        if (audio != null) {
            setAudio(audio);
        } else {
            removeAudio();
            log.warn("audio \"{}\" is not found", audioName);
        }
    }

    public void setAudio(@Nonnull NamedBeanHandle<Audio> handle) {
        assertListenersAreNotRegistered(log, "setAudio");
        _audioHandle = handle;
        InstanceManager.getDefault(AudioManager.class).addVetoableChangeListener(this);
    }

    public void setAudio(@Nonnull Audio audio) {
        assertListenersAreNotRegistered(log, "setAudio");
        setAudio(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(audio.getDisplayName(), audio));
    }

    public void removeAudio() {
        assertListenersAreNotRegistered(log, "setAudio");
        if (_audioHandle != null) {
            InstanceManager.getDefault(AudioManager.class).removeVetoableChangeListener(this);
            _audioHandle = null;
        }
    }

    public NamedBeanHandle<Audio> getAudio() {
        return _audioHandle;
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

    public void setOperationAddressing(NamedBeanAddressing addressing) throws ParserException {
        _operationAddressing = addressing;
        parseOperationFormula();
    }

    public NamedBeanAddressing getOperationAddressing() {
        return _operationAddressing;
    }

    public void setOperation(Operation state) {
        _operation = state;
    }

    public Operation getOperation() {
        return _operation;
    }

    public void setOperationReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _operationReference = reference;
    }

    public String getOperationReference() {
        return _operationReference;
    }

    public void setOperationLocalVariable(@Nonnull String localVariable) {
        _operationLocalVariable = localVariable;
    }

    public String getOperationLocalVariable() {
        return _operationLocalVariable;
    }

    public void setOperationFormula(@Nonnull String formula) throws ParserException {
        _operationFormula = formula;
        parseOperationFormula();
    }

    public String getOperationFormula() {
        return _operationFormula;
    }

    private void parseOperationFormula() throws ParserException {
        if (_operationAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _operationExpressionNode = parser.parseExpression(_operationFormula);
        } else {
            _operationExpressionNode = null;
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Audio) {
                if (evt.getOldValue().equals(getAudio().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Audio_AudioInUseAudioActionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Audio) {
                if (evt.getOldValue().equals(getAudio().getBean())) {
                    removeAudio();
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

        switch (_operationAddressing) {
            case Reference:
                return ReferenceUtil.getReference(getConditionalNG().getSymbolTable(), _operationReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_operationLocalVariable), false);

            case Formula:
                return _operationExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _operationExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _operationAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Audio audio;

//        System.out.format("ActionAudio.execute: %s%n", getLongDescription());

        switch (_addressing) {
            case Direct:
                audio = _audioHandle != null ? _audioHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                audio = InstanceManager.getDefault(AudioManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                audio = InstanceManager.getDefault(AudioManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                audio = _expressionNode != null ?
                        InstanceManager.getDefault(AudioManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

//        System.out.format("ActionAudio.execute: audio: %s%n", audio);

        if (audio == null) {
//            log.warn("audio is null");
            return;
        }

        String name = (_operationAddressing != NamedBeanAddressing.Direct)
                ? getNewState() : null;

        Operation operation;
        if ((_operationAddressing == NamedBeanAddressing.Direct)) {
            operation = _operation;
        } else {
            operation = Operation.valueOf(name);
        }

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            if (audio.getSubType() == Audio.SOURCE) {
                AudioSource audioSource = (AudioSource) audio;
                switch (operation) {
                    case Play:
                        audioSource.play();
                        break;
                    case Stop:
                        audioSource.stop();
                        break;
                    case PlayToggle:
                        audioSource.togglePlay();
                        break;
                    case Pause:
                        audioSource.pause();
                        break;
                    case Resume:
                        audioSource.resume();
                        break;
                    case PauseToggle:
                        audioSource.togglePause();
                        break;
                    case Rewind:
                        audioSource.rewind();
                        break;
                    case FadeIn:
                        audioSource.fadeIn();
                        break;
                    case FadeOut:
                        audioSource.fadeOut();
                        break;
                    case ResetPosition:
                        audioSource.resetCurrentPosition();
                        break;
                    default:
                        break;
                }
            } else if (audio.getSubType() == Audio.LISTENER) {
                AudioListener audioListener = (AudioListener) audio;
                switch (operation) {
                    case ResetPosition:
                        audioListener.resetCurrentPosition();
                        break;
                    default:
                        break; // nothing needed for others
                }
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
        return Bundle.getMessage(locale, "ActionAudio_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String operation;

        switch (_addressing) {
            case Direct:
                String audioName;
                if (_audioHandle != null) {
                    audioName = _audioHandle.getBean().getDisplayName();
                } else {
                    audioName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", audioName);
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

        switch (_operationAddressing) {
            case Direct:
                operation = Bundle.getMessage(locale, "AddressByDirect", _operation._text);
                break;

            case Reference:
                operation = Bundle.getMessage(locale, "AddressByReference", _operationReference);
                break;

            case LocalVariable:
                operation = Bundle.getMessage(locale, "AddressByLocalVariable", _operationLocalVariable);
                break;

            case Formula:
                operation = Bundle.getMessage(locale, "AddressByFormula", _operationFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _operationAddressing.name());
        }

        return Bundle.getMessage(locale, "ActionAudio_Long", operation, namedBean);
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


    public enum Operation {
        Play(Bundle.getMessage("ActionAudio_Operation_Play")),
        PlayToggle(Bundle.getMessage("ActionAudio_Operation_PlayToggle")),
        Pause(Bundle.getMessage("ActionAudio_Operation_Pause")),
        PauseToggle(Bundle.getMessage("ActionAudio_Operation_PauseToggle")),
        Resume(Bundle.getMessage("ActionAudio_Operation_Resume")),
        Stop(Bundle.getMessage("ActionAudio_Operation_Stop")),
        FadeIn(Bundle.getMessage("ActionAudio_Operation_FadeIn")),
        FadeOut(Bundle.getMessage("ActionAudio_Operation_FadeOut")),
        Rewind(Bundle.getMessage("ActionAudio_Operation_Rewind")),
        ResetPosition(Bundle.getMessage("ActionAudio_Operation_ResetPosition"));

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
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ActionAudio: bean = {}, report = {}", cdl, report);
        if (getAudio() != null && bean.equals(getAudio().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionAudio.class);

}
