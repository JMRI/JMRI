package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectComboBox;
import jmri.jmrit.logixng.util.LogixNG_SelectInteger;

/**
 * Program a CV on main.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class ProgramOnMain extends AbstractDigitalAction implements PropertyChangeListener {

    private SystemConnectionMemo _memo;
    private AddressedProgrammerManager _programmerManager;
    private ThrottleManager _throttleManager;
    private final LogixNG_SelectComboBox _selectProgrammingMode;
    private final LogixNG_SelectInteger _selectAddress = new LogixNG_SelectInteger(this, this);
    private final LogixNG_SelectInteger _selectCV = new LogixNG_SelectInteger(this, this);
    private final LogixNG_SelectInteger _selectValue = new LogixNG_SelectInteger(this, this);

    public ProgramOnMain(String sys, String user) {
        super(sys, user);

        // The array is updated with correct values when setMemo() is called
        String[] modes = {""};
        _selectProgrammingMode = new LogixNG_SelectComboBox(this, modes, modes[0], this);

        // Set the _programmerManager and _throttleManager variables
        setMemo(null);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ProgramOnMain copy = new ProgramOnMain(sysName, userName);
        copy.setComment(getComment());
        copy.setMemo(_memo);
        _selectProgrammingMode.copy(copy._selectProgrammingMode);
        _selectAddress.copy(copy._selectAddress);
        _selectCV.copy(copy._selectCV);
        _selectValue.copy(copy._selectValue);
        return manager.registerAction(copy);
    }

    public final LogixNG_SelectComboBox getSelectProgrammingMode() {
        return _selectProgrammingMode;
    }

    public final LogixNG_SelectInteger getSelectAddress() {
        return _selectAddress;
    }

    public final LogixNG_SelectInteger getSelectCV() {
        return _selectCV;
    }

    public final LogixNG_SelectInteger getSelectValue() {
        return _selectValue;
    }

    public final void setMemo(SystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setMemo");

        _memo = memo;
        if (_memo != null) {
            _programmerManager = _memo.get(AddressedProgrammerManager.class);
            _throttleManager = _memo.get(ThrottleManager.class);
            if (_throttleManager == null) {
                throw new IllegalArgumentException("Memo "+memo.getUserName()+" doesn't have a ThrottleManager");
            }

            // LocoNet memo doesn't have a programmer during tests
            if (_programmerManager == null) {
                _programmerManager = InstanceManager.getDefault(AddressedProgrammerManager.class);
            }
        } else {
            _programmerManager = InstanceManager.getDefault(AddressedProgrammerManager.class);
            _throttleManager = InstanceManager.getDefault(ThrottleManager.class);
        }

        List<String> modeList = new ArrayList<>();
        for (ProgrammingMode mode : _programmerManager.getDefaultModes()) {
            log.debug("Available programming mode: {}", mode);
            modeList.add(mode.getStandardName());
        }

        // Add OPSBYTEMODE in case we don't have any mode,
        // for example if we are running a simulator.
        if (modeList.isEmpty()) {
            modeList.add(ProgrammingMode.OPSBYTEMODE.getStandardName());
        }

        // Add OPSBYTEMODE in case we don't have any mode,
        // for example if we are running a simulator.
        if (modeList.isEmpty()) {
            modeList.add(ProgrammingMode.OPSBYTEMODE.getStandardName());
        }

        String[] modes = modeList.toArray(String[]::new);
        _selectProgrammingMode.setValues(modes);
    }

    public final SystemConnectionMemo getMemo() {
        return _memo;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private void doProgrammingOnMain(ProgrammingMode progMode, int address,
            int cv, int value)
            throws JmriException {
        try {
            AddressedProgrammer programmer = _programmerManager.getAddressedProgrammer(
                    new DccLocoAddress(address, !_throttleManager.canBeShortAddress(address)));

            if (programmer != null) {
                programmer.setMode(progMode);
                if (!progMode.equals(programmer.getMode())) {
                    throw new IllegalArgumentException("The addressed programmer doesn't support mode " + progMode.getStandardName());
                }
                programmer.writeCV("" + cv, value, null);
            } else {
                throw new IllegalArgumentException("An addressed programmer isn't available for address " + address);
            }
        } catch (ProgrammerException e) {
            throw new JmriException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        ConditionalNG conditionalNG = this.getConditionalNG();

        String progModeStr = _selectProgrammingMode.evaluateValue(conditionalNG);
        ProgrammingMode progMode = new ProgrammingMode(progModeStr);

        int address = _selectAddress.evaluateValue(conditionalNG);
        int cv = _selectCV.evaluateValue(conditionalNG);
        int value = _selectValue.evaluateValue(conditionalNG);

        doProgrammingOnMain(progMode, address, cv, value);
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ProgramOnMain_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        if (_memo != null) {
            return Bundle.getMessage(locale, "ProgramOnMain_LongConnection",
                    _selectAddress.getDescription(locale),
                    _selectCV.getDescription(locale),
                    _selectValue.getDescription(locale),
                    _selectProgrammingMode.getDescription(locale),
                    _memo.getUserName());
        } else {
            return Bundle.getMessage(locale, "ProgramOnMain_Long",
                    _selectAddress.getDescription(locale),
                    _selectCV.getDescription(locale),
                    _selectValue.getDescription(locale),
                    _selectProgrammingMode.getDescription(locale));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProgramOnMain.class);

}
