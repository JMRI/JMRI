package jmri.jmrit.logixng.actions.swing;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionClock;
import jmri.jmrit.logixng.actions.ActionClock.ClockState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionClock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionClockSwing extends AbstractDigitalActionSwing {

    private JComboBox<ClockState> _stateComboBox;
    private JTextField _timeTextField;
    private int _minutes = 0;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionClock action = (ActionClock) object;

        panel = new JPanel();

        _stateComboBox = new JComboBox<>();
        for (ClockState e : ClockState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        _stateComboBox.addActionListener((java.awt.event.ActionEvent e) -> {
            setTimeField(action == null ? 0 : action.getClockTime());
        });

        _timeTextField = new JTextField(4);
        setTimeField(0);

        if (action != null) {
            _stateComboBox.setSelectedItem(action.getBeanState());
            setTimeField(action.getClockTime());
        }

        JPanel timeField = new JPanel();
        JLabel timelabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ActionClock_TimeLabel")));
        timeField.add(timelabel);
        timeField.add(_timeTextField);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(_stateComboBox);
        container.add(timeField);

        JComponent[] components = new JComponent[]{
            container};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionClock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setTimeField(int time) {
        if (_stateComboBox.getSelectedItem() == ClockState.SetClock) {
            _timeTextField.setEnabled(true);
            _timeTextField.setText(ActionClock.formatTime(time));
        } else {
            _timeTextField.setEnabled(false);
            _timeTextField.setText("");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        log.debug("Validate time: {}", _timeTextField.getText());

        if (_stateComboBox.getSelectedItem() == ClockState.SetClock) {
            LocalTime newHHMM;

            try {
                newHHMM = LocalTime.parse(_timeTextField.getText().trim(), DateTimeFormatter.ofPattern("H:mm"));
                log.debug("time: hh = {}, mm = {}", newHHMM.getHour(), newHHMM.getMinute());
                _minutes = newHHMM.getHour() * 60 + newHHMM.getMinute();
                if (_minutes < 0 || _minutes > 1439) {
                    errorMessages.add(Bundle.getMessage("ActionClock_RangeError"));
                }
                log.debug("new time: {}", _minutes);
            } catch (java.time.format.DateTimeParseException ex) {
                errorMessages.add(Bundle.getMessage("ActionClock_ParseError", ex.getParsedString()));
            }
        }

        if (!errorMessages.isEmpty()) return false;
        return true;
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
        action.setBeanState(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));

        if (_stateComboBox.getSelectedItem() == ClockState.SetClock) {
                action.setClockTime(_minutes);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionClock_Short");
    }

    @Override
    public void dispose() {
    }


   private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionClockSwing.class);

}
