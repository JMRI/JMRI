package jmri.jmrit.logixng.actions;

import jmri.*;

/**
 * Defines types of NamedBeans, for example Turnout and Light.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public enum NamedBeanType {
    Light(Bundle.getMessage("BeanNameLight"), Light.class, "KnownState", () -> {
        return InstanceManager.getDefault(LightManager.class);
    }),
    Memory(Bundle.getMessage("BeanNameMemory"), Memory.class, "value", () -> {
        return InstanceManager.getDefault(MemoryManager.class);
    }),
    Sensor(Bundle.getMessage("BeanNameSensor"), Sensor.class, "KnownState", () -> {
        return InstanceManager.getDefault(SensorManager.class);
    }),
    Turnout(Bundle.getMessage("BeanNameTurnout"), Turnout.class, "KnownState", () -> {
        return InstanceManager.getDefault(TurnoutManager.class);
    });
    private final String _name;
    private final Class<? extends NamedBean> _clazz;
    final String _propertyName;
    private final GetManager _getManager;
    Manager<? extends NamedBean> _manager;

    NamedBeanType(String name, Class<? extends NamedBean> clazz, String propertyName, GetManager getManager) {
        _name = name;
        _clazz = clazz;
        _propertyName = propertyName;
        _getManager = getManager;
        _manager = _getManager.getManager();
    }

    @Override
    public String toString() {
        return _name;
    }

    public Class<? extends NamedBean> getClazz() {
        return _clazz;
    }

    public Manager<? extends NamedBean> getManager() {
        return _manager;
    }

    // This method is used by test classes to reset this enum.
    // Each test resets the InstanceManager so we need to reset the
    // managers in this enum.
    public static void reset() {
        for (NamedBeanType type : NamedBeanType.values()) {
            type._manager = type._getManager.getManager();
        }
    }

    private interface GetManager {

        Manager<? extends NamedBean> getManager();
    }
    
}
