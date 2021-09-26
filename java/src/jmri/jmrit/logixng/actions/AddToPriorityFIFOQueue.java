package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Adds to a PriorityFIFOQueue
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class AddToPriorityFIFOQueue
        extends AbstractDigitalAction
        implements FemaleSocketListener, VetoableChangeListener {

    private NamedBeanHandle<PriorityFIFOQueue> _priorityFIFOQueueHandle;
    private int _priority;
    private String _actionSocketSystemName;
    private final FemaleDigitalActionSocket _actionSocket;
    
    public AddToPriorityFIFOQueue(String sys, String user) {
        super(sys, user);
        _actionSocket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        AddToPriorityFIFOQueue copy = new AddToPriorityFIFOQueue(sysName, userName);
        copy.setComment(getComment());
        if (_priorityFIFOQueueHandle != null) copy.setPriorityFIFOQueue(_priorityFIFOQueueHandle);
        copy.setPriority(_priority);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    public void setPriorityFIFOQueue(@Nonnull String priorityFIFOQueueName) {
        assertListenersAreNotRegistered(log, "setPriorityFIFOQueue");
        Base digitalAction =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .getNamedBean(priorityFIFOQueueName);
        if (digitalAction != null) {
            while (digitalAction instanceof MaleSocket) {
                digitalAction = ((MaleSocket)digitalAction).getObject();
            }
            if (digitalAction instanceof PriorityFIFOQueue) {
                setPriorityFIFOQueue((PriorityFIFOQueue)digitalAction);
            } else {
                removePriorityFIFOQueue();
                log.error("\"{}\" is not a PriorityFIFOQueue", priorityFIFOQueueName);
            }
        } else {
            removePriorityFIFOQueue();
            log.error("PriorityFIFOQueue \"{}\" is not found", priorityFIFOQueueName);
        }
    }
    
    public void setPriorityFIFOQueue(@Nonnull NamedBeanHandle<PriorityFIFOQueue> handle) {
        assertListenersAreNotRegistered(log, "setPriorityFIFOQueue");
        _priorityFIFOQueueHandle = handle;
        InstanceManager.getDefault(DigitalExpressionManager.class).addVetoableChangeListener(this);
    }
    
    public void setPriorityFIFOQueue(@Nonnull PriorityFIFOQueue priorityFIFOQueue) {
        assertListenersAreNotRegistered(log, "setPriorityFIFOQueue");
        setPriorityFIFOQueue(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(priorityFIFOQueue.getDisplayName(), priorityFIFOQueue));
    }
    
    public void removePriorityFIFOQueue() {
        assertListenersAreNotRegistered(log, "removePriorityFIFOQueue");
        if (_priorityFIFOQueueHandle != null) {
            InstanceManager.getDefault(DigitalExpressionManager.class).removeVetoableChangeListener(this);
            _priorityFIFOQueueHandle = null;
        }
    }
    
    public NamedBeanHandle<PriorityFIFOQueue> getPriorityFIFOQueue() {
        return _priorityFIFOQueueHandle;
    }
    
    public int getPriority() {
        return _priority;
    }

    public void setPriority(int priority) {
        assertListenersAreNotRegistered(log, "setPriority");
        _priority = priority;
    }

    public FemaleDigitalActionSocket getDigitalActionSocket() {
        return _actionSocket;
    }

    public String getDigitalActionSocketSystemName() {
        return _actionSocketSystemName;
    }

    public void setDigitalActionSocketSystemName(String systemName) {
        assertListenersAreNotRegistered(log, "setDigitalActionSocketSystemName");
        _actionSocketSystemName = systemName;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof DigitalExpression) {
                if (evt.getOldValue().equals(getPriorityFIFOQueue().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("AddToPriorityFIFOQueue_BeanInUseVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof DigitalExpression) {
                if (evt.getOldValue().equals(getPriorityFIFOQueue().getBean())) {
                    removePriorityFIFOQueue();
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        if (_priorityFIFOQueueHandle != null) {
            _priorityFIFOQueueHandle.getBean().addAction(_priority, _actionSocket);
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _actionSocket;
                
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
        if (socket == _actionSocket) {
            _actionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _actionSocket) {
            _actionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "AddToPriorityFIFOQueue_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String priorityFIFOQueueName;
        if (_priorityFIFOQueueHandle != null) {
            priorityFIFOQueueName = _priorityFIFOQueueHandle.getBean().getDisplayName();
        } else {
            priorityFIFOQueueName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "AddToPriorityFIFOQueue_Long", _actionSocket.getName(), _priority, priorityFIFOQueueName);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_actionSocket.isConnected()
                    || !_actionSocket.getConnectedSocket().getSystemName()
                            .equals(_actionSocketSystemName)) {
                
                String socketSystemName = _actionSocketSystemName;
                
                _actionSocket.disconnect();
                
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _actionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action " + socketSystemName);
                    }
                }
            } else {
                _actionSocket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
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
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddToPriorityFIFOQueue.class);
    
}
