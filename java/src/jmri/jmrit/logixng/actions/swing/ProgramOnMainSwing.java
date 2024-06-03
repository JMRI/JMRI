package jmri.jmrit.logixng.actions.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ProgramOnMain;
import jmri.jmrit.logixng.actions.ProgramOnMain.LongOrShortAddress;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectComboBoxSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectIntegerSwing;

/**
 * Configures an ProgramOnMain object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class ProgramOnMainSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectComboBoxSwing _selectProgrammingModeSwing;
    private LogixNG_SelectEnumSwing<LongOrShortAddress> _selectLongOrShortAddressSwing;
    private LogixNG_SelectIntegerSwing _selectAddressSwing;
    private LogixNG_SelectIntegerSwing _selectCVSwing;
    private LogixNG_SelectIntegerSwing _selectValueSwing;
    private JComboBox<Connection> _connection;
    private JTextField _localVariableForStatus;
    private JCheckBox _wait;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ProgramOnMain action = (ProgramOnMain)object;
        if (action == null) {
            // Create a temporary action
            action = new ProgramOnMain("IQDA1", null);
        }

        JLabel longOrShortLabel = new JLabel(Bundle.getMessage("ProgramOnMainSwing_LongOrShortAddress"));
        JLabel addressLabel = new JLabel(Bundle.getMessage("ProgramOnMainSwing_Address"));
        JLabel cvLabel = new JLabel(Bundle.getMessage("ProgramOnMainSwing_CV"));
        JLabel valueLabel = new JLabel(Bundle.getMessage("ProgramOnMainSwing_Value"));
        JLabel connectionLabel = new JLabel(Bundle.getMessage("ProgramOnMainSwing_Connection"));
        JLabel programmingModeLabel = new JLabel(Bundle.getMessage("ProgramOnMainSwing_ProgrammingMode"));
        JLabel localVariableForStatusLabel = new JLabel(Bundle.getMessage("ProgramOnMainSwing_LocalVariableStatus"));
        JLabel waitLabel = new JLabel(Bundle.getMessage("ProgramOnMainSwing_Wait"));
        JLabel waitInfoLabel = new JLabel(Bundle.getMessage("ProgramOnMainSwing_WaitInfo"));

        _selectProgrammingModeSwing = new LogixNG_SelectComboBoxSwing(getJDialog(), this);
        _selectLongOrShortAddressSwing = new LogixNG_SelectEnumSwing<LongOrShortAddress>(getJDialog(), this);
        _selectAddressSwing = new LogixNG_SelectIntegerSwing(getJDialog(), this);
        _selectCVSwing = new LogixNG_SelectIntegerSwing(getJDialog(), this);
        _selectValueSwing = new LogixNG_SelectIntegerSwing(getJDialog(), this);
        _wait = new JCheckBox();

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel panelProgrammingMode;
        JPanel panelLongOrShortAddress;
        JPanel panelAddress;
        JPanel panelCV;
        JPanel panelValue;

        panelProgrammingMode = _selectProgrammingModeSwing.createPanel(
                action.getSelectProgrammingMode());
        panelLongOrShortAddress = _selectLongOrShortAddressSwing.createPanel(
                action.getSelectLongOrShortAddress(), LongOrShortAddress.values());
        panelAddress = _selectAddressSwing.createPanel(action.getSelectAddress());
        panelCV = _selectCVSwing.createPanel(action.getSelectCV());
        panelValue = _selectValueSwing.createPanel(action.getSelectValue());


        _connection = new JComboBox<>();
        _connection.addItem(new Connection(null));
        List<SystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(SystemConnectionMemo.class);
        for (SystemConnectionMemo connection : systemConnections) {
            if (!connection.provides(ThrottleManager.class)) continue;
            Connection c = new Connection(connection);
            _connection.addItem(c);
            if ((object != null) && (action.getMemo() == connection)) {
                _connection.setSelectedItem(c);
            }
        }

        _connection.addActionListener((e) -> { updateProgrammingModes(); });


        _localVariableForStatus = new JTextField(20);
        _localVariableForStatus.setText(action.getLocalVariableForStatus());
        
        _wait.setSelected(action.getWait());


        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.gridwidth = 1;
        constraint.gridheight = 1;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.EAST;
        panel.add(longOrShortLabel, constraint);
        longOrShortLabel.setLabelFor(panelLongOrShortAddress);
        constraint.gridy = 1;
        panel.add(cvLabel, constraint);
        cvLabel.setLabelFor(panelCV);
        constraint.anchor = GridBagConstraints.CENTER;
        constraint.gridwidth = 7;
        constraint.gridy = 6;
        panel.add(new JLabel(" "), constraint);     // Add space
        constraint.gridy = 7;
        panel.add(waitInfoLabel, constraint);

        // Add some space
        constraint.gridwidth = 1;
        constraint.gridx = 1;
        constraint.gridy = 0;
        panel.add(new JLabel(" "), constraint);

        constraint.gridx = 2;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.WEST;
        panel.add(panelLongOrShortAddress, constraint);
        constraint.gridy = 1;
        panel.add(panelCV, constraint);
        constraint.anchor = GridBagConstraints.EAST;
        constraint.gridwidth = 3;
        constraint.gridy = 2;
        panel.add(programmingModeLabel, constraint);
        programmingModeLabel.setLabelFor(panelProgrammingMode);
        constraint.gridy = 3;
        panel.add(connectionLabel, constraint);
        connectionLabel.setLabelFor(_connection);
        constraint.gridy = 4;
        panel.add(localVariableForStatusLabel, constraint);
        localVariableForStatusLabel.setLabelFor(_localVariableForStatus);
        constraint.gridy = 5;
        panel.add(waitLabel, constraint);
        waitLabel.setLabelFor(_wait);
        constraint.gridwidth = 1;

        // Add some space
        constraint.gridx = 3;
        constraint.gridy = 0;
        panel.add(new JLabel(" "), constraint);

        constraint.gridx = 4;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.EAST;
        panel.add(addressLabel, constraint);
        addressLabel.setLabelFor(panelAddress);
        constraint.gridy = 1;
        panel.add(valueLabel, constraint);
        valueLabel.setLabelFor(panelValue);

        // Add some space
        constraint.gridx = 5;
        constraint.gridy = 0;
        panel.add(new JLabel(" "), constraint);

        constraint.gridx = 6;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.WEST;
        panel.add(panelAddress, constraint);
        constraint.gridy = 1;
        panel.add(panelValue, constraint);
        constraint.gridy = 2;
        panel.add(panelProgrammingMode, constraint);
        constraint.gridy = 3;
        panel.add(_connection, constraint);
        constraint.gridy = 4;
        panel.add(_localVariableForStatus, constraint);
        constraint.gridy = 5;
        panel.add(_wait, constraint);
    }

    private void updateProgrammingModes() {
        // Create a temporary action to get programming modes
        ProgramOnMain action = new ProgramOnMain("IQDA1", null);
        action.setMemo(_connection.getItemAt(_connection.getSelectedIndex())._memo);
        _selectProgrammingModeSwing.setValues(action.getSelectProgrammingMode().getValues());
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ProgramOnMain action = new ProgramOnMain("IQDA1", null);

        _selectProgrammingModeSwing.validate(action.getSelectProgrammingMode(), errorMessages);
        _selectAddressSwing.validate(action.getSelectAddress(), errorMessages);
        _selectCVSwing.validate(action.getSelectCV(), errorMessages);
        _selectValueSwing.validate(action.getSelectValue(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ProgramOnMain action = new ProgramOnMain(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ProgramOnMain)) {
            throw new IllegalArgumentException("object must be an ProgramOnMain but is a: "+object.getClass().getName());
        }
        ProgramOnMain action = (ProgramOnMain)object;
        _selectProgrammingModeSwing.updateObject(action.getSelectProgrammingMode());
        _selectLongOrShortAddressSwing.updateObject(action.getSelectLongOrShortAddress());
        _selectAddressSwing.updateObject(action.getSelectAddress());
        _selectCVSwing.updateObject(action.getSelectCV());
        _selectValueSwing.updateObject(action.getSelectValue());

        action.setMemo(_connection.getItemAt(_connection.getSelectedIndex())._memo);

        action.setLocalVariableForStatus(_localVariableForStatus.getText());
        action.setWait(_wait.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ProgramOnMain_Short");
    }

    @Override
    public void dispose() {
        _selectProgrammingModeSwing.dispose();
        _selectAddressSwing.dispose();
        _selectCVSwing.dispose();
        _selectValueSwing.dispose();
    }



    private static class Connection {

        private SystemConnectionMemo _memo;

        public Connection(SystemConnectionMemo memo) {
            _memo = memo;
        }

        @Override
        public String toString() {
            if (_memo == null) {
                return Bundle.getMessage("ProgramOnMainSwing_DefaultConnection");
            }
            return _memo.getUserName();
        }
    }

}
