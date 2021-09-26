package jmri.jmrit.logixng.actions;

import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Priority First in, First out Queue
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class PriorityFIFOQueue
        extends AbstractDigitalAction
        implements FemaleSocketListener {

    private int _numPriorities;
    private final List<Deque<DigitalAction>> _queues = new ArrayList<>();
    private boolean _isCurrentlyRunning;
    
    
    public PriorityFIFOQueue(String sys, String user) {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        PriorityFIFOQueue copy = new PriorityFIFOQueue(sysName, userName);
        copy.setComment(getComment());
        copy.setNumPriorities(_numPriorities);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    private DigitalAction getNextAction() {
        for (Deque<DigitalAction> queue : _queues) {
            if (!queue.isEmpty()) {
                return queue.removeFirst();
            }
        }
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        DigitalAction action;
        
        synchronized (this) {
            _isCurrentlyRunning = true;
            action = getNextAction();
        }
        
        while (action != null) {
            Base oldParent = action.getParent();
            action.setParent(this);
            action.execute();
            action.setParent(oldParent);
            
            synchronized (this) {
                action = getNextAction();
                if (action == null) {
                    _isCurrentlyRunning = false;
                    return;
                }
            }
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new IllegalArgumentException(
                String.format("index has invalid value: %d", index));
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public void connected(FemaleSocket socket) {
        throw new IllegalArgumentException("unkown socket");
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        throw new IllegalArgumentException("unkown socket");
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "PriorityFIFOQueue_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "PriorityFIFOQueue_Long", _numPriorities);
    }

    public int getNumPriorities() {
        return _numPriorities;
    }

    public void setNumPriorities(int numPriorities) {
        _numPriorities = numPriorities;
        synchronized(this) {
            _queues.clear();
            for (int i=0; i < _numPriorities; i++) {
                _queues.add(new ArrayDeque<>());
            }
        }
    }

    public synchronized void addAction(int priority, DigitalAction action) {
        if ((priority < 0) || (priority >= _numPriorities)) {
            throw new IllegalArgumentException("priority is out of bound");
        }
        _queues.get(priority).add(action);
        if (!_isCurrentlyRunning) getConditionalNG().execute();
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
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PriorityFIFOQueue.class);
    
}
