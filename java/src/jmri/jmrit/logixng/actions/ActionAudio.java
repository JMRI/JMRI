package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.audio.AudioListener;
import jmri.jmrit.audio.AudioSource;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.ThreadingUtil;

/**
 * This action controls an audio object.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionAudio extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Audio> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Audio.class, InstanceManager.getDefault(AudioManager.class), this);

    private final LogixNG_SelectEnum<Operation> _selectEnum =
            new LogixNG_SelectEnum<>(this, Operation.values(), Operation.Play, this);


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
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnum.copy(copy._selectEnum);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Audio> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<Operation> getSelectEnum() {
        return _selectEnum;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Audio audio = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (audio == null) return;

        Operation operation = _selectEnum.evaluateEnum(getConditionalNG());

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
        String namedBean = _selectNamedBean.getDescription(locale);
        String operation = _selectEnum.getDescription(locale);

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
        _selectNamedBean.registerListeners();
        _selectEnum.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
        _selectEnum.unregisterListeners();
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
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionAudio.class);

}
