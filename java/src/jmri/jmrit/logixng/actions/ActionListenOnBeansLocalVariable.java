package jmri.jmrit.logixng.actions;

import jmri.jmrit.logixng.NamedBeanType;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;

import net.jcip.annotations.GuardedBy;

/**
 * This action listens on some beans and runs the ConditionalNG on property change.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionListenOnBeansLocalVariable extends AbstractDigitalAction
        implements FemaleSocketListener, PropertyChangeListener, VetoableChangeListener {

    private NamedBeanType _namedBeanType = NamedBeanType.Light;
    private boolean _listenOnAllProperties = false;
    private String _localVariableBeanToListenOn;
    private String _localVariableNamedBean;
    private String _localVariableEvent;
    private String _localVariableNewValue;
    private final Map<NamedBean, String> _namedBeansEntries = new HashMap<>();
    private final InternalFemaleSocket _internalSocket = new InternalFemaleSocket();
    private String _executeSocketSystemName;
    private final FemaleDigitalActionSocket _executeSocket;

    @GuardedBy("this")
    private final Deque<PropertyChangeEvent> _eventQueue = new ArrayDeque<>();


    public ActionListenOnBeansLocalVariable(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _executeSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ShowDialog_SocketExecute"));
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames)
            throws JmriException {

        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionListenOnBeansLocalVariable copy = new ActionListenOnBeansLocalVariable(sysName, userName);
        copy.setComment(getComment());
        copy.setNamedBeanType(_namedBeanType);

        copy.setLocalVariableBeanToListenOn(_localVariableBeanToListenOn);
        copy.setLocalVariableNamedBean(_localVariableNamedBean);
        copy.setLocalVariableEvent(_localVariableEvent);
        copy.setLocalVariableNewValue(_localVariableNewValue);

        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    /**
     * Get the type of the named beans
     * @return the type of named beans
     */
    public NamedBeanType getNamedBeanType() {
        return _namedBeanType;
    }

    /**
     * Set the type of the named beans
     * @param namedBeanType the type of the named beans
     */
    public void setNamedBeanType(@Nonnull NamedBeanType namedBeanType) {
        if (namedBeanType == null) {
            throw new IllegalArgumentException("namedBeanType must not be null");
        }
        _namedBeanType = namedBeanType;
    }

    public boolean getListenOnAllProperties() {
        return _listenOnAllProperties;
    }

    public void setListenOnAllProperties(boolean listenOnAllProperties) {
        _listenOnAllProperties = listenOnAllProperties;
    }

    public void setLocalVariableBeanToListenOn(String localVariableBeanToListenOn) {
        if ((localVariableBeanToListenOn != null) && (!localVariableBeanToListenOn.isEmpty())) {
            this._localVariableBeanToListenOn = localVariableBeanToListenOn;
        } else {
            this._localVariableBeanToListenOn = null;
        }
    }

    public String getLocalVariableBeanToListenOn() {
        return _localVariableBeanToListenOn;
    }

    public void setLocalVariableNamedBean(String localVariableNamedBean) {
        if ((localVariableNamedBean != null) && (!localVariableNamedBean.isEmpty())) {
            this._localVariableNamedBean = localVariableNamedBean;
        } else {
            this._localVariableNamedBean = null;
        }
    }

    public String getLocalVariableNamedBean() {
        return _localVariableNamedBean;
    }

    public void setLocalVariableEvent(String localVariableEvent) {
        if ((localVariableEvent != null) && (!localVariableEvent.isEmpty())) {
            this._localVariableEvent = localVariableEvent;
        } else {
            this._localVariableEvent = null;
        }
    }

    public String getLocalVariableEvent() {
        return _localVariableEvent;
    }

    public void setLocalVariableNewValue(String localVariableNewValue) {
        if ((localVariableNewValue != null) && (!localVariableNewValue.isEmpty())) {
            this._localVariableNewValue = localVariableNewValue;
        } else {
            this._localVariableNewValue = null;
        }
    }

    public String getLocalVariableNewValue() {
        return _localVariableNewValue;
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
    public LogixNG_Category getCategory() {
        return LogixNG_Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (_localVariableBeanToListenOn != null
                && !_localVariableBeanToListenOn.isBlank()) {

            ConditionalNG conditionalNG = getConditionalNG();
            _internalSocket._conditionalNG = conditionalNG;
            _internalSocket._newSymbolTable = new DefaultSymbolTable(conditionalNG.getSymbolTable());

            SymbolTable symbolTable = conditionalNG.getSymbolTable();
            Object value = symbolTable.getValue(_localVariableBeanToListenOn);

            NamedBean namedBean = null;

            if (value instanceof NamedBean) {
                namedBean = (NamedBean) value;
            } else if (value != null) {
                namedBean = _namedBeanType.getManager().getNamedBean(value.toString());
            }

            if (namedBean != null) {
                if (!_namedBeansEntries.containsKey(namedBean)) {
                    _namedBeansEntries.put(namedBean, _namedBeanType.getPropertyName());
                    addPropertyListener(namedBean, _namedBeanType.getPropertyName());
                }
            } else {
                log.warn("The named bean \"{}\" cannot be found in the manager for {}", value, _namedBeanType.toString());
            }
        }
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
        return Bundle.getMessage(locale, "ActionListenOnBeansLocalVariable_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale,
                "ActionListenOnBeansLocalVariable_Long",
                _localVariableBeanToListenOn,
                _namedBeanType.toString());
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

    private void addPropertyListener(NamedBean namedBean, String property) {
        if (!_listenOnAllProperties
                && (property != null)) {
            namedBean.addPropertyChangeListener(property, this);
        } else {
            namedBean.addPropertyChangeListener(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (_listenersAreRegistered) return;

        for (Map.Entry<NamedBean, String> namedBeanEntry : _namedBeansEntries.entrySet()) {
            addPropertyListener(namedBeanEntry.getKey(), namedBeanEntry.getValue());
        }
        _listenersAreRegistered = true;
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (!_listenersAreRegistered) return;

        for (Map.Entry<NamedBean, String> namedBeanEntry : _namedBeansEntries.entrySet()) {
            if (!_listenOnAllProperties
                    && (namedBeanEntry.getValue() != null)) {
                namedBeanEntry.getKey().removePropertyChangeListener(namedBeanEntry.getValue(), this);
            } else {
                namedBeanEntry.getKey().removePropertyChangeListener(this);
            }
            namedBeanEntry.getKey().removePropertyChangeListener(namedBeanEntry.getValue(), this);
        }
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        boolean isQueueEmpty;
        synchronized(this) {
            isQueueEmpty = _eventQueue.isEmpty();
            _eventQueue.add(evt);
        }
        if (isQueueEmpty) {
            getConditionalNG().execute(_internalSocket);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
    }


    private class InternalFemaleSocket extends jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket {

        private ConditionalNG _conditionalNG;
        private SymbolTable _newSymbolTable;

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

                synchronized(this) {
                    SymbolTable oldSymbolTable = _conditionalNG.getSymbolTable();
                    _conditionalNG.setSymbolTable(_newSymbolTable);

                    String namedBean;
                    String event;
                    String newValue;

                    PropertyChangeEvent evt = _eventQueue.poll();
                    if (evt != null) {
                        namedBean = ((NamedBean)evt.getSource()).getDisplayName();
                        event = evt.getPropertyName();
                        newValue = evt.getNewValue() != null ? evt.getNewValue().toString() : null;
                    } else {
                        namedBean = null;
                        event = null;
                        newValue = null;
                    }

                    if (_localVariableNamedBean != null) {
                        _newSymbolTable.setValue(_localVariableNamedBean, namedBean);
                    }
                    if (_localVariableEvent != null) {
                        _newSymbolTable.setValue(_localVariableEvent, event);
                    }
                    if (_localVariableNewValue != null) {
                        _newSymbolTable.setValue(_localVariableNewValue, newValue);
                    }

                    _executeSocket.execute();
                    _conditionalNG.setSymbolTable(oldSymbolTable);

                    if (!_eventQueue.isEmpty()) {
                        _conditionalNG.execute(_internalSocket);
                    }
                }
            }
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeansLocalVariable.class);

}
