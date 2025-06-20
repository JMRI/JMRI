package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_SelectComboBox;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.LogixNG_SelectInteger;

/**
 * Program a CV on main.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class ProgramOnMain extends AbstractDigitalAction
        implements FemaleSocketListener, PropertyChangeListener {

    private static final ResourceBundle rbx =
            ResourceBundle.getBundle("jmri.jmrit.logixng.implementation.ImplementationBundle");

    private String _executeSocketSystemName;
    private final FemaleDigitalActionSocket _executeSocket;
    private SystemConnectionMemo _memo;
    private AddressedProgrammerManager _programmerManager;
    private ThrottleManager _throttleManager;
    private final LogixNG_SelectComboBox _selectProgrammingMode;
    private final LogixNG_SelectEnum<LongOrShortAddress> _selectLongOrShortAddress =
            new LogixNG_SelectEnum<>(this, LongOrShortAddress.values(), LongOrShortAddress.Auto, this);
    private final LogixNG_SelectInteger _selectAddress = new LogixNG_SelectInteger(this, this);
    private final LogixNG_SelectInteger _selectCV = new LogixNG_SelectInteger(this, this);
    private final LogixNG_SelectInteger _selectValue = new LogixNG_SelectInteger(this, this);
    private String _localVariableForStatus = "";
    private boolean _wait = true;
    private final InternalFemaleSocket _internalSocket = new InternalFemaleSocket();

    public ProgramOnMain(String sys, String user) {
        super(sys, user);

        // The array is updated with correct values when setMemo() is called
        String[] modes = {""};
        _selectProgrammingMode = new LogixNG_SelectComboBox(this, modes, modes[0], this);

        // Set the _programmerManager and _throttleManager variables
        setMemo(null);

        _executeSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ProgramOnMain_Socket"));
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
        _selectLongOrShortAddress.copy(copy._selectLongOrShortAddress);
        _selectAddress.copy(copy._selectAddress);
        _selectCV.copy(copy._selectCV);
        _selectValue.copy(copy._selectValue);
        copy._wait = _wait;
        copy.setLocalVariableForStatus(_localVariableForStatus);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public final LogixNG_SelectComboBox getSelectProgrammingMode() {
        return _selectProgrammingMode;
    }

    public final LogixNG_SelectInteger getSelectAddress() {
        return _selectAddress;
    }

    public LogixNG_SelectEnum<LongOrShortAddress> getSelectLongOrShortAddress() {
        return _selectLongOrShortAddress;
    }

    public final LogixNG_SelectInteger getSelectCV() {
        return _selectCV;
    }

    public final LogixNG_SelectInteger getSelectValue() {
        return _selectValue;
    }

    public void setLocalVariableForStatus(String localVariable) {
        _localVariableForStatus = localVariable;
    }

    public String getLocalVariableForStatus() {
        return _localVariableForStatus;
    }

    public void setWait(boolean wait) {
        _wait = wait;
    }

    public boolean getWait() {
        return _wait;
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

        String[] modes = modeList.toArray(String[]::new);
        _selectProgrammingMode.setValues(modes);
    }

    public final SystemConnectionMemo getMemo() {
        return _memo;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    private void doProgrammingOnMain(ConditionalNG conditionalNG,
            DefaultSymbolTable newSymbolTable, ProgrammingMode progMode,
            int address, LongOrShortAddress longOrShort, int cv, int value, boolean wait)
            throws JmriException {
        try {
            boolean longAddress;
            
            switch (longOrShort) {
                case Short:
                    longAddress = false;
                    break;
                    
                case Long:
                    longAddress = true;
                    break;
                    
                case Auto:
                    longAddress = !_throttleManager.canBeShortAddress(address);
                    break;
                    
                default:
                    throw new IllegalArgumentException("longOrShort has unknown value");
            }
            
            AddressedProgrammer programmer = _programmerManager.getAddressedProgrammer(
                    new DccLocoAddress(address, longAddress));

            if (programmer != null) {
                programmer.setMode(progMode);
                if (!progMode.equals(programmer.getMode())) {
                    throw new IllegalArgumentException("The addressed programmer doesn't support mode " + progMode.getStandardName());
                }
                AtomicInteger result = new AtomicInteger(-1);
                programmer.writeCV("" + cv, value, (int value1, int status) -> {
                    result.set(status);

                    log.debug("Result of programming cv {} to value {} for address {}: {}", cv, value, address, status);

                    synchronized(ProgramOnMain.this) {
                        _internalSocket.conditionalNG = conditionalNG;
                        _internalSocket.newSymbolTable = newSymbolTable;
                        _internalSocket.status = status;
                        conditionalNG.execute(_internalSocket);
                    }
                });

                if (wait) {
                    try {
                        while (result.get() == -1) {
                            Thread.sleep(10);
                        }
                    } catch (InterruptedException e) {
                        log.warn("Waiting for programmer to complete was aborted");
                    }
                }

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
        DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG.getSymbolTable());

        String progModeStr = _selectProgrammingMode.evaluateValue(conditionalNG);
        ProgrammingMode progMode = new ProgrammingMode(progModeStr);

        int address = _selectAddress.evaluateValue(conditionalNG);
        LongOrShortAddress longOrShort = _selectLongOrShortAddress.evaluateEnum(conditionalNG);
        int cv = _selectCV.evaluateValue(conditionalNG);
        int value = _selectValue.evaluateValue(conditionalNG);

        doProgrammingOnMain(conditionalNG, newSymbolTable, progMode, address, longOrShort, cv, value, _wait);
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _executeSocket;

            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _executeSocket) {
            _executeSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _executeSocket) {
            _executeSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ProgramOnMain_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        if (_memo != null) {
            return Bundle.getMessage(locale, "ProgramOnMain_LongConnection",
                    _selectLongOrShortAddress.getDescription(locale),
                    _selectAddress.getDescription(locale, false),
                    _selectCV.getDescription(locale, false),
                    _selectValue.getDescription(locale, false),
                    _selectProgrammingMode.getDescription(locale),
                    _memo.getUserName());
        } else {
            return Bundle.getMessage(locale, "ProgramOnMain_Long",
                    _selectLongOrShortAddress.getDescription(locale),
                    _selectAddress.getDescription(locale, false),
                    _selectCV.getDescription(locale, false),
                    _selectValue.getDescription(locale, false),
                    _selectProgrammingMode.getDescription(locale));
        }
    }

    public FemaleDigitalActionSocket getExecuteSocket() {
        return _executeSocket;
    }

    public String getExecuteSocketSystemName() {
        return _executeSocketSystemName;
    }

    public void setExecuteSocketSystemName(String systemName) {
        _executeSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_executeSocket.isConnected()
                    || !_executeSocket.getConnectedSocket().getSystemName()
                            .equals(_executeSocketSystemName)) {

                String socketSystemName = _executeSocketSystemName;

                _executeSocket.disconnect();

                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _executeSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action {}", socketSystemName);
                    }
                }
            } else {
                _executeSocket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }


    private class InternalFemaleSocket extends jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket {

        private ConditionalNG conditionalNG;
        private SymbolTable newSymbolTable;
        private int status;

        public InternalFemaleSocket() {
            super(null, new FemaleSocketListener(){
                @Override
                public void connected(FemaleSocket socket) {
                    // Do nothing
                }

                @Override
                public void disconnected(FemaleSocket socket) {
                    // Do nothing
                }
            }, "A");
        }

        @Override
        public void execute() throws JmriException {
            if (_executeSocket != null) {
                MaleSocket maleSocket = (MaleSocket)ProgramOnMain.this.getParent();
                try {
                    SymbolTable oldSymbolTable = conditionalNG.getSymbolTable();
                    conditionalNG.setSymbolTable(newSymbolTable);
                    if (!_localVariableForStatus.isEmpty()) {
                        newSymbolTable.setValue(_localVariableForStatus, status);
                    }
                    _executeSocket.execute();
                    conditionalNG.setSymbolTable(oldSymbolTable);
                } catch (JmriException e) {
                    if (e.getErrors() != null) {
                        maleSocket.handleError(ProgramOnMain.this, rbx.getString("ExceptionExecuteMulti"), e.getErrors(), e, log);
                    } else {
                        maleSocket.handleError(ProgramOnMain.this, Bundle.formatMessage(rbx.getString("ExceptionExecuteAction"), e.getLocalizedMessage()), e, log);
                    }
                } catch (RuntimeException e) {
                    maleSocket.handleError(ProgramOnMain.this, Bundle.formatMessage(rbx.getString("ExceptionExecuteAction"), e.getLocalizedMessage()), e, log);
                }
            }
        }

    }


    public enum LongOrShortAddress {
        Short(Bundle.getMessage("ProgramOnMain_LongOrShortAddress_Short")),
        Long(Bundle.getMessage("ProgramOnMain_LongOrShortAddress_Long")),
        Auto(Bundle.getMessage("ProgramOnMain_LongOrShortAddress_Auto"));

        private final String _text;

        private LongOrShortAddress(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProgramOnMain.class);

}
