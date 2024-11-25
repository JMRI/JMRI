package jmri.jmrix.powerline;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.SerialPort;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010 copied from NCE into Powerline for
 * multiple connections by
 * @author Ken Cameron Copyright (C) 2011
 */
public class SerialSystemConnectionMemo extends jmri.jmrix.DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public SerialSystemConnectionMemo() {
        super("P", "Powerline");
        InstanceManager.store(this, SerialSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.powerline.swing.PowerlineComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     *
     * @return tc
     */
    public SerialTrafficController getTrafficController() {
        return serialTrafficController;
    }
    private SerialTrafficController serialTrafficController;

    public void setTrafficController(SerialTrafficController tc) {
        serialTrafficController = tc;
    }

    /**
     * Provide access to the serial port for this connection
     * @return SerialPort
     */
    public SerialPort getActiveSerialPort() {
        return serialPort;
    }
    private SerialPort serialPort;
    public void setActiveSerialPort(SerialPort sp) {
        serialPort = sp;
    }
    /**
     * Provide access to a serialAddress for this particular connection
     *
     * @return serialAddress
     */
    public SerialAddress getSerialAddress() {
        return serialAddress;
    }
    private SerialAddress serialAddress;

    public void setSerialAddress(SerialAddress sa) {
        serialAddress = sa;
    }

    /**
     * Configure the common managers for Powerline connections. This puts the
     * common manager config in one place.
     */
    @Override
    public void configureManagers() {
        // now does nothing here, it's done by the specific class
        register(); // registers general type
    }

    // menu support parts
    // subclasses can override to change menu items

    public static class MenuItem {
        MenuItem(String name, String load) {
            this.name = name;
            this.load = load;
        }
        public String name;
        public String load;
    }
    private final MenuItem[] panelItems = new MenuItem[]{
        new MenuItem("MenuItemCommandMonitor", "jmri.jmrix.powerline.swing.serialmon.SerialMonPane"),
        new MenuItem("MenuItemSendCommand", "jmri.jmrix.powerline.swing.packetgen.SerialPacketGenPane")
    };

    public MenuItem[] provideMenuItemList() {
        return panelItems;
    }

    public SerialTurnoutManager getTurnoutManager() {
        return (SerialTurnoutManager)get(TurnoutManager.class);
    }

    public SerialLightManager getLightManager() {
        return (SerialLightManager)get(LightManager.class);
    }

    public SerialSensorManager getSensorManager() {
        return (SerialSensorManager)get(SensorManager.class);
    }

    public void setTurnoutManager(SerialTurnoutManager m) {
        store(m,TurnoutManager.class);
    }

    public void setLightManager(SerialLightManager m) {
        store(m,LightManager.class);
    }

    public void setSensorManager(SerialSensorManager m) {
        store(m,SensorManager.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.powerline.PowerlineActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        serialTrafficController = null;
        InstanceManager.deregister(this, SerialSystemConnectionMemo.class);
        super.dispose();
    }

}
