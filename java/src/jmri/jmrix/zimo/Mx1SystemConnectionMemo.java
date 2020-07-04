package jmri.jmrix.zimo;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2020
 */
public class Mx1SystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public Mx1SystemConnectionMemo(Mx1TrafficController st) {
        super("Z", "MX-1");
        this.st = st;
        InstanceManager.store(this, Mx1SystemConnectionMemo.class);
        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.zimo.swing.Mx1ComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    public Mx1SystemConnectionMemo() {
        super("Z", "MX-1");
        InstanceManager.store(this, Mx1SystemConnectionMemo.class);
        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.zimo.swing.Mx1ComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    public final static int MX1 = 0x00;
    public final static int MXULF = 0x01;
    public final static int MX10 = 0x02;

    int connectionType = 0x00;

    public void setConnectionType(int connection) {
        connectionType = connection;
    }

    public int getConnectionType() {
        return connectionType;
    }

    /**
     * Provide access to the TrafficController for this particular connection.
     *
     * @return the associated traffic controller
     */
    public Mx1TrafficController getMx1TrafficController() {
        return st;
    }

    public void setMx1TrafficController(Mx1TrafficController st) {
        this.st = st;
    }
    private Mx1TrafficController st;

    /**
     * Configure the "command station" object
     * @deprecated since 4.21.1 without replacement
     */
    @Deprecated
    public void configureCommandStation() {
    }

    @Override
    public void configureManagers() {

        DefaultProgrammerManager programmerManager = getProgrammerManager();
        store(programmerManager,DefaultProgrammerManager.class);
        if (programmerManager.isAddressedModePossible()) {
            store(programmerManager, AddressedProgrammerManager.class);
            InstanceManager.store(programmerManager, AddressedProgrammerManager.class);
        }
        if (programmerManager.isGlobalProgrammerAvailable()) {
            store(programmerManager,GlobalProgrammerManager.class);
            InstanceManager.store(programmerManager, GlobalProgrammerManager.class);
        }

        PowerManager powerManager = new Mx1PowerManager(this);
        store(powerManager, PowerManager.class);
        jmri.InstanceManager.store(powerManager, PowerManager.class);

        ThrottleManager throttleManager = new jmri.jmrix.zimo.Mx1ThrottleManager(this);
        store(throttleManager,ThrottleManager.class);
        InstanceManager.setThrottleManager(throttleManager);
        if (getProtocol() == Mx1Packetizer.BINARY) {
            TurnoutManager turnoutManager = new Mx1TurnoutManager(this);
            store(turnoutManager,TurnoutManager.class);
            InstanceManager.setTurnoutManager(turnoutManager);
        }
        register(); // registers general type
    }

    boolean getProtocol() {
        if (getMx1TrafficController() != null) {
            return getMx1TrafficController().getProtocol();
        }
        return Mx1Packetizer.ASCII;
    }

    public DefaultProgrammerManager getProgrammerManager() {
        return (DefaultProgrammerManager) classObjectMap.computeIfAbsent(DefaultProgrammerManager.class, (Class c) -> {
            return generateDefaultProgrammerManagerForConnection();
        });
    }

    private DefaultProgrammerManager generateDefaultProgrammerManagerForConnection() {
        DefaultProgrammerManager programmerManager;
        if (getProtocol() == Mx1Packetizer.BINARY) {
            programmerManager = new Mx1ProgrammerManager(new Mx1Programmer(getMx1TrafficController()), this);
        } else {
            programmerManager = new DefaultProgrammerManager(new Mx1Programmer(getMx1TrafficController()), this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(DefaultProgrammerManager p) {
        store(p,DefaultProgrammerManager.class);
    }

    public Mx1TurnoutManager getTurnoutManager() {
        return (Mx1TurnoutManager) classObjectMap.get(TurnoutManager.class);
    }

    /**
     * @param cs command station object to set
     * @deprecated since 4.21.1 without replacement
     */
    @Deprecated
    public void setCommandStation(Mx1CommandStation cs) {
    }

    public Mx1PowerManager getPowerManager() {
        return (Mx1PowerManager) classObjectMap.get(PowerManager.class);
    }

    public Mx1ThrottleManager getThrottleManager() {
        return (Mx1ThrottleManager) classObjectMap.get(ThrottleManager.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.zimo.ZimoActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        st = null;
        InstanceManager.deregister(this, Mx1SystemConnectionMemo.class);
        if (componentFactory != null) {
            InstanceManager.deregister(componentFactory, jmri.jmrix.swing.ComponentFactory.class);
        }
        Mx1ThrottleManager throttleManager = (Mx1ThrottleManager) classObjectMap.get(ThrottleManager.class);
        if (throttleManager != null) {
            deregister(throttleManager,ThrottleManager.class);
            InstanceManager.deregister(throttleManager, jmri.jmrix.zimo.Mx1ThrottleManager.class);
        }
        super.dispose();
    }

}
