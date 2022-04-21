package jmri.jmrit.logixng.actions.swing;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionClock;
import jmri.jmrit.logixng.actions.ActionClock.ClockState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectIntegerSwing;

/**
 * Configures an ActionClock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionClockSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectEnumSwing<ClockState> _selectEnumSwing;
    private LogixNG_SelectIntegerSwing _selectTimeSwing;


    public ActionClockSwing() {
    }

    public ActionClockSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionClock action = (ActionClock) object;

        _selectEnumSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);
        _selectTimeSwing = new LogixNG_SelectIntegerSwing(
                getJDialog(), this, new TimeFormatterParserValidator());

        panel = new JPanel();
        JPanel _tabbedPaneClockState;
        JPanel _tabbedPaneTime;

        if (action != null) {
            _tabbedPaneClockState = _selectEnumSwing.createPanel(action.getSelectEnum(), ClockState.values());
            _tabbedPaneTime = _selectTimeSwing.createPanel(action.getSelectTime());
        } else {
            _tabbedPaneClockState = _selectEnumSwing.createPanel(null, ClockState.values());
            _tabbedPaneTime = _selectTimeSwing.createPanel(null);
        }

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(_tabbedPaneClockState);
        container.add(_tabbedPaneTime);

        JComponent[] components = new JComponent[]{
            container};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionClock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionClock action = new ActionClock("IQDA1", null);

        _selectEnumSwing.validate(action.getSelectEnum(), errorMessages);
        _selectTimeSwing.validate(action.getSelectTime(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionClock action = new ActionClock(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionClock)) {
            throw new IllegalArgumentException("object must be an ActionClock but is a: "+object.getClass().getName());
        }
        ActionClock action = (ActionClock) object;

        _selectEnumSwing.updateObject(action.getSelectEnum());
        _selectTimeSwing.updateObject(action.getSelectTime());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionClock_Short");
    }

    @Override
    public void dispose() {
        _selectEnumSwing.dispose();
        _selectTimeSwing.dispose();
    }


    private static class TimeFormatterParserValidator
            implements LogixNG_SelectIntegerSwing.FormatterParserValidator {

        @Override
        public int getInitialValue() {
            return 0;
        }

        @Override
        public String format(int value) {
            return ActionClock.formatTime(value);
        }

        @Override
        public int parse(String str) {
            int minutes;
            LocalTime newHHMM;
            try {
                newHHMM = LocalTime.parse(str.trim(), DateTimeFormatter.ofPattern("H:mm"));
                minutes = newHHMM.getHour() * 60 + newHHMM.getMinute();
                if (minutes < 0 || minutes > 1439) {
                    return 0;
                }
                return minutes;
            } catch (DateTimeParseException ex) {
                return 0;
            }
        }

        @Override
        public String validate(String str) {
            int minutes;
            LocalTime newHHMM;
            try {
                newHHMM = LocalTime.parse(str.trim(), DateTimeFormatter.ofPattern("H:mm"));
                minutes = newHHMM.getHour() * 60 + newHHMM.getMinute();
                if (minutes < 0 || minutes > 1439) {
                    return Bundle.getMessage("ActionClock_RangeError");
                }
            } catch (DateTimeParseException ex) {
                return Bundle.getMessage("ActionClock_ParseError", ex.getParsedString());
            }
            return null;
        }

    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionClockSwing.class);

}
