package jmri.jmrit.z21server;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.*;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the mapping from a Z21 turnout number to a JMRI Named Bean. This is not
 * restricted to just JMRI turnouts, the mapping works for:
 * - Turnouts
 * - Lights (thrown = ON, closed = OFF
 * - Routes (thrown sets the route, closed does nothing)
 * - Signal Masts and Signal Heads (closed = HELD, thrown = not HELD, the aspect/appearance itself can not be modified)
 * - Sensors (thrown = ACTIVE, closed = INACTIVE). Used to triggers all actions bound to the sensor
 * 
 * If a component should be used by a Z21 turnout number, the number will be stored
 * in a property of the bean.
 * 
 * @author Eckart Meyer Copyright (C) 2025
 */

public class TurnoutNumberMapHandler implements PropertyChangeListener {
    
    private static TurnoutNumberMapHandler instance;
    public final static String beanProperty = "Z21TurnoutMap";

    // NOTE: This list should match the classes used in NumberMapFrame.java
    private final static Class<?>[] mgrList = {
        TurnoutManager.class,
        RouteManager.class,
        LightManager.class,
        SignalMastManager.class,
        SignalHeadManager.class,
        SensorManager.class
    };

    private final Map<Integer, NamedBean> turnoutNumberList = new HashMap<>(); //cache for faster access
    
    private TurnoutNumberMapHandler() {
    }
    
    synchronized public static TurnoutNumberMapHandler getInstance() {
        if (instance == null) {
            instance = new TurnoutNumberMapHandler();
            instance.loadNumberList();
            instance.addPropertyChangeListeners();
        }
        return instance;
    }
    
    public static Class<?>[] getManagerClassList() {
        return mgrList;
    }

/**
 * Get the state of a component given by the Z21 Turnout Number. Get the the component from
 * the hash map. The state is either THROWN, CLOSED or UNKNOWN. For other components than
 * turnouts, their state will be converted into a turnout state.
 * 
 * @param turnoutNumber - the Z21 Turnout Number
 * @return the current state converted to a turnout state
 */    
    public int getStateForNumber(int turnoutNumber) {
        int state = -1;
        NamedBean b = turnoutNumberList.get(turnoutNumber);
        if (b != null) {
            log.debug("Turnout number {} is {} - {}", turnoutNumber, b.getSystemName(), b.getUserName());
            log.trace("  class: {}", b.getClass().getName());
            if (b instanceof Route) {
                state = Turnout.CLOSED;
            }
            else if (b instanceof Light) {
                Light l = (Light)b;
                state = (l.getState() == Light.ON) ? Turnout.THROWN : Turnout.CLOSED;
            }
            else if (b instanceof SignalMast) {
                SignalMast s = (SignalMast)b;
                state = (s.getHeld()) ? Turnout.CLOSED : Turnout.THROWN;
            }
            else if (b instanceof SignalHead) {
                SignalHead s = (SignalHead)b;
                state = (s.getHeld()) ? Turnout.CLOSED : Turnout.THROWN;
            }
            else if (b instanceof Sensor) {
                Sensor s = (Sensor)b;
                state = (s.getKnownState() == Sensor.ACTIVE) ? Turnout.THROWN : Turnout.CLOSED;
            }
            else {
                state = b.getState();
            }
        }
        log.debug("state for number {} is {}", turnoutNumber, state);
        return state;
    }
    
/**
 * Set the state of a component identified by the mapped number from a turnout state (THROWN or CLOSED)
 * 
 * @param turnoutNumber - the Z21 Turnout Number
 * @param state - a turnout state
 */
    public void setStateForNumber(int turnoutNumber, int state) {
        NamedBean b = turnoutNumberList.get(turnoutNumber);
        if (b != null) {
            log.debug("Turnout number {} is {} - {}", turnoutNumber, b.getSystemName(), b.getUserName());
            if (b instanceof Turnout) {
                Turnout t = (Turnout)b;
                t.setCommandedState(state);
            }
            else if (b instanceof Route) {
                Route r = (Route)b;
                if (state == Turnout.THROWN) {
                    r.setRoute();
                }
            }
            else if (b instanceof Light) {
                Light l = (Light)b;
                l.setState( (state == Turnout.THROWN) ? Light.ON : Light.OFF );
            }
            else if (b instanceof SignalMast) {
                SignalMast s = (SignalMast)b;
                s.setHeld(state == Turnout.CLOSED);
            }
            else if (b instanceof SignalHead) {
                SignalHead s = (SignalHead)b;
                s.setHeld(state == Turnout.CLOSED);
            }
            else if (b instanceof Sensor) {
                Sensor s = (Sensor)b;
                try {
                    s.setKnownState((state == Turnout.THROWN) ? Sensor.ACTIVE : Sensor.INACTIVE );
                }
                catch (JmriException e) {
                    log.warn("Sensor not set");
                }
            }
        }
    }

/**
 * Load our local hash map, so we have a cache
 */
    public void loadNumberList() {
        turnoutNumberList.clear();
        for (Class<?> clazz : mgrList) {
            loadNumberTable(clazz);
        }        
    }
    
/**
 * Load the mapping information for all components of a given class.
 * 
 * @param <T>
 * @param clazz - the manager class to be used expressed as a classname, e.g. TurnoutManager.class
 */
    @SuppressWarnings("unchecked")
    private <T extends NamedBean> void loadNumberTable(Class<?> clazz) {
        Pattern pattern = Pattern.compile("^(\\d+)$");
        Manager<T> mgr = (Manager<T>)InstanceManager.getNullableDefault(clazz);
        if (mgr != null) {
            log.trace("mgr: {} {}", mgr, mgr.getClass().getName());
            for (T t : mgr.getNamedBeanSet()) {
                Object o = t.getProperty(beanProperty);
                if (o != null) {
                    String val = o.toString();
                    Matcher matcher = pattern.matcher(val);
                    if (matcher.matches()) {
                        if (matcher.group(0) != null) {
                            int num = Integer.parseInt(matcher.group(0)); //mapped turnout number
                            log.debug("Found number {}: {} - {}", num, t.getSystemName(), t.getUserName());
                            turnoutNumberList.put(num, t);
                        }
                    }
                }
            }
        }
    }
    
/**
 * Add property change listener to all supported component managers, so we
 * will be informed, if the tables have changed.
 */
    @SuppressWarnings("unchecked")
    private void addPropertyChangeListeners() {
        for (Class<?> clazz : mgrList) {
            Manager<?> mgr = (Manager<?>)InstanceManager.getNullableDefault(clazz);
            if (mgr != null) {
                mgr.addPropertyChangeListener(instance);
            }
        }
    }

/**
 * Remove listener from all managers
 */    
    @SuppressWarnings("unchecked")
    private void removePropertyChangeListeners() {
        for (Class<?> clazz : mgrList) {
            Manager<?> mgr = (Manager<?>)InstanceManager.getNullableDefault(clazz);
            if (mgr != null) {
                mgr.removePropertyChangeListener(instance);
            }
        }
    }
    
/**
 * on destruction of the instance - would probably never occur...
 */
    public void dispose() {
        removePropertyChangeListeners();
    }
    
/**
 * Property change listener.
 * (Re-)loads the cache from all mapped components.
 * Also called from the UI if the mapping has been changed by the user.
 * 
 * @param e is the propery change event
 */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.trace("property changed: {}", e.getPropertyName());
        loadNumberList(); //reload list
    }

    
    private final static Logger log = LoggerFactory.getLogger(TurnoutNumberMapHandler.class);

}
