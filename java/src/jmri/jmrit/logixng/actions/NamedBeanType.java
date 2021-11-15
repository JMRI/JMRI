package jmri.jmrit.logixng.actions;

import jmri.*;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;

/**
 * Defines types of NamedBeans, for example Turnout and Light.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public enum NamedBeanType {
    Block(Bundle.getMessage("BeanNameBlock"), Light.class, null, () -> {
        return InstanceManager.getDefault(BlockManager.class);
    }),
    EntryExit(Bundle.getMessage("BeanNameEntryExit"), Light.class, "active", () -> {
        return InstanceManager.getDefault(EntryExitPairs.class);
    }),
    Light(Bundle.getMessage("BeanNameLight"), Light.class, "KnownState", () -> {
        return InstanceManager.getDefault(LightManager.class);
    }),
    Memory(Bundle.getMessage("BeanNameMemory"), Memory.class, "value", () -> {
        return InstanceManager.getDefault(MemoryManager.class);
    }),
    OBlock(Bundle.getMessage("BeanNameOBlock"), OBlock.class, "state", () -> {
        return InstanceManager.getDefault(OBlockManager.class);
    }),
    Reporter(Bundle.getMessage("BeanNameReporter"), Reporter.class, "value", () -> {
        return InstanceManager.getDefault(ReporterManager.class);
    }),
    Sensor(Bundle.getMessage("BeanNameSensor"), Sensor.class, "KnownState", () -> {
        return InstanceManager.getDefault(SensorManager.class);
    }),
    SignalHead(Bundle.getMessage("BeanNameSignalHead"), SignalHead.class, null, () -> {
        return InstanceManager.getDefault(SignalHeadManager.class);
    }),
    SignalMast(Bundle.getMessage("BeanNameSignalMast"), SignalMast.class, null, () -> {
        return InstanceManager.getDefault(SignalMastManager.class);
    }),
    Turnout(Bundle.getMessage("BeanNameTurnout"), Turnout.class, "KnownState", () -> {
        return InstanceManager.getDefault(TurnoutManager.class);
    }),
    Warrant(Bundle.getMessage("BeanNameWarrant"), Warrant.class, "KnownState", () -> {
        return InstanceManager.getDefault(WarrantManager.class);
    });
    
    private final String _name;
    private final Class<? extends NamedBean> _clazz;
    private final String _propertyName;
    private final GetManager _getManager;
    private Manager<? extends NamedBean> _manager;

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

    public String getPropertyName() {
        return _propertyName;
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
