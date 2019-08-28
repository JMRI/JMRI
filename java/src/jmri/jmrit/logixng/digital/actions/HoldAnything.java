package jmri.jmrit.logixng.digital.actions;

import java.util.ArrayList;
import java.util.List;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketFactory;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalActionWithEnableExecution;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.implementation.AbstractMaleSocket;

/**
 * An action that can hold everything but doesn't do anything.
 * It is used to hold parts of the LogixNG tree that is not currently in use.
 This allows moving operations in the tree.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class HoldAnything extends AbstractDigitalAction
        implements DigitalActionWithEnableExecution {

    private boolean _enableExecution;
    private final List<MultipleSockets> _multipleSockets = new ArrayList<>();
    
    /**
     * Create a new instance of ActionMany and generate a new system name.
     */
    public HoldAnything() {
        super(InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName());
        init();
    }

    public HoldAnything(String sys)
            throws NamedBean.BadSystemNameException {
        
        super(sys);
        init();
    }

    public HoldAnything(String sys, String user)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException {
        
        super(sys, user);
        init();
    }
    
    private void init() {
        for (FemaleSocketFactory factory :
                InstanceManager.getDefault(LogixNG_Manager.class).getFemaleSocketFactories()) {
            
            _multipleSockets.add(new MultipleSockets(factory));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate(String sys) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsEnableExecution() {
        // This action supports EnableExecution since it never executes anything.
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setEnableExecution(boolean b) {
        _enableExecution = b;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isExecutionEnabled() {
        return _enableExecution;
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    @Override
    public void evaluateOnly() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        // Do nothing
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        int i = index;
        
        for (MultipleSockets multipleSockets : _multipleSockets) {
            if (i < multipleSockets._femaleSockets.size()) {
                return multipleSockets._femaleSockets.get(i);
            }
            i -= multipleSockets._femaleSockets.size();
        }
        
        throw new IllegalArgumentException(String.format("index %d out of range", index));
    }

    @Override
    public int getChildCount() {
        return _multipleSockets.size();
    }
    
    @Override
    public String getShortDescription() {
        return Bundle.getMessage("HoldAnything_Short");
    }

    @Override
    public String getLongDescription() {
        return Bundle.getMessage("HoldAnything_Long");
    }

    public void setInitialSystemNames(String factoryClass, List<String> systemNames) {
        for (MultipleSockets m : _multipleSockets) {
            if (factoryClass.equals(m._femaleSocketFactory.getClass().getName())) {
                m._systemNames = systemNames;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        for (MultipleSockets m : _multipleSockets) {
            if (m._systemNames != null) {
                m._femaleSockets.clear();
                
                for (String systemName : m._systemNames) {
                    FemaleSocket socket = m._femaleSocketFactory.create(m, m);
                    m._femaleSockets.add(socket);
                    try {
                        socket.connect(m._femaleSocketFactory.getBeanBySystemName(systemName));
                    } catch (SocketAlreadyConnectedException ex) {
                        // This shouldn't happen and is a runtime error if it does.
                        throw new RuntimeException("socket is already connected");
                    }
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        // Do nothing.
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        // Do nothing.
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    
    
    // This class can not be static since it needs to access the outer class.
    
    private class MultipleSockets extends AbstractMaleSocket implements FemaleSocketListener {

        private final FemaleSocketFactory _femaleSocketFactory;
        private List<String> _systemNames;
        private final List<FemaleSocket> _femaleSockets = new ArrayList<>();
        private Lock _lock = Lock.NONE;
//        private boolean _enabled = false;
        
        private MultipleSockets(FemaleSocketFactory femaleSocketFactory) {
            _femaleSocketFactory = femaleSocketFactory;
            _femaleSockets.add(femaleSocketFactory.create(this, this));
        }
        
        /** {@inheritDoc} */
        @Override
        public Base getNewObjectBasedOnTemplate(String sys) {
            return null;
        }
        
        @Override
        public Base getParent() {
            return HoldAnything.this;
        }
        
        @Override
        public void setParent(Base parent) {
            // Do nothing. The parent of this class will always be the outer class.
        }
        
        /** {@inheritDoc} */
        @Override
        public String getSystemName() {
            throw new RuntimeException("This method should never be called");
        }
        
        /** {@inheritDoc} */
        @Override
        public String getUserName() {
            throw new RuntimeException("This method should never be called");
        }
        
        /** {@inheritDoc} */
        @Override
        public void setUserName(String name) {
            throw new RuntimeException("This method should never be called");
        }

        /** {@inheritDoc} */
        @Override
        public Lock getLock() {
            return _lock;
        }

        /** {@inheritDoc} */
        @Override
        public void setLock(Lock lock) {
            _lock = lock;
        }

        @Override
        public String getShortDescription() {
            return "Multiple sockets for " + _femaleSocketFactory.getClass().getName();
        }

        @Override
        public String getLongDescription() {
            return "Multiple sockets for " + _femaleSocketFactory.getClass().getName();
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            return _femaleSockets.get(index);
        }

        @Override
        public int getChildCount() {
            return _femaleSockets.size();
        }

        @Override
        public Category getCategory() {
            return Category.OTHER;
        }

        @Override
        public boolean isExternal() {
            return false;
        }

        @Override
        public void connected(FemaleSocket socket) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void disconnected(FemaleSocket socket) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            // Do nothing
        }
        
        /** {@inheritDoc} */
        @Override
        public void registerListenersForThisClass() {
            // Do nothing
        }

        /** {@inheritDoc} */
        @Override
        public void unregisterListenersForThisClass() {
            // Do nothing
        }

        /** {@inheritDoc} */
        @Override
        public void disposeMe() {
            for (FemaleSocket femaleSocket : _femaleSockets) {
                femaleSocket.disconnect();
                femaleSocket.dispose();
            }
        }
        
    }
    
}
