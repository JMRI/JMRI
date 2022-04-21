package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionClockSpeed;
import jmri.jmrit.logixng.actions.ActionClockSpeed.ClockState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectDoubleSwing;
import static jmri.jmrit.simpleclock.SimpleTimebase.MAXIMUM_RATE;
import static jmri.jmrit.simpleclock.SimpleTimebase.MINIMUM_RATE;

/**
 * Configures an ActionClockSpeed object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionClockSpeedSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectEnumSwing<ClockState> _selectEnumSwing;
    private LogixNG_SelectDoubleSwing _selectSpeedSwing;


    public ActionClockSpeedSwing() {
    }

    public ActionClockSpeedSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionClockSpeed action = (ActionClockSpeed) object;

        _selectEnumSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);
        _selectSpeedSwing = new LogixNG_SelectDoubleSwing(
                getJDialog(), this, new DefaultFormatterParserValidator());

        panel = new JPanel();
        JPanel _tabbedPaneClockState;
        JPanel _tabbedPaneTime;

        if (action != null) {
            _tabbedPaneClockState = _selectEnumSwing.createPanel(action.getSelectEnum(), ClockState.values());
            _tabbedPaneTime = _selectSpeedSwing.createPanel(action.getSelectSpeed());
        } else {
            _tabbedPaneClockState = _selectEnumSwing.createPanel(null, ClockState.values());
            _tabbedPaneTime = _selectSpeedSwing.createPanel(null);
        }

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(_tabbedPaneClockState);
        container.add(_tabbedPaneTime);

        JComponent[] components = new JComponent[]{
            container};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionClockSpeed_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionClockSpeed action = new ActionClockSpeed("IQDA1", null);

        _selectEnumSwing.validate(action.getSelectEnum(), errorMessages);
        _selectSpeedSwing.validate(action.getSelectSpeed(), errorMessages);

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
        ActionClockSpeed action = new ActionClockSpeed(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionClockSpeed)) {
            throw new IllegalArgumentException("object must be an ActionClockSpeed but is a: "+object.getClass().getName());
        }
        ActionClockSpeed action = (ActionClockSpeed) object;

        _selectEnumSwing.updateObject(action.getSelectEnum());
        _selectSpeedSwing.updateObject(action.getSelectSpeed());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionClockSpeed_Short");
    }

    @Override
    public void dispose() {
        _selectEnumSwing.dispose();
        _selectSpeedSwing.dispose();
    }


    private static class DefaultFormatterParserValidator
            extends LogixNG_SelectDoubleSwing.DefaultFormatterParserValidator {

        @Override
        public double getInitialValue() {
            return MINIMUM_RATE;
        }

        @Override
        public double parse(String str) {
            try {
                double value = Double.parseDouble(str);
                if (value < MINIMUM_RATE || value > MAXIMUM_RATE) {
                    return MINIMUM_RATE;
                }
                return value;
            } catch (NumberFormatException ex) {
                return MINIMUM_RATE;
            }
        }

        @Override
        public String validate(String str) {
            try {
                double value = Double.parseDouble(str);
                if (value < MINIMUM_RATE || value > MAXIMUM_RATE) {
                    return Bundle.getMessage("ActionClockSpeed_RangeError");
                }
                return null;
            } catch (NumberFormatException ex) {
                return Bundle.getMessage("ActionClockSpeed_ParseError");
            }
        }

    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionClockSpeedSwing.class);

}
