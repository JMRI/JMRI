package jmri.jmrit.logixng;

import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
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

    Audio(
            Bundle.getMessage("BeanNameAudio"),
            Bundle.getMessage("BeanNameAudios"),
            Audio.class,
            "State",
            () -> InstanceManager.getDefault(AudioManager.class),
            null,
            (NamedBean bean, String property) -> {
                if (!(bean instanceof Audio)) {
                    throw new IllegalArgumentException("bean is not an Audio");
                }
                InstanceManager.getDefault(AudioManager.class)
                        .deleteBean((Audio)bean, property);
            }),

    Block(
            Bundle.getMessage("BeanNameBlock"),
            Bundle.getMessage("BeanNameBlocks"),
            Block.class,
            null,
            () -> InstanceManager.getDefault(BlockManager.class),
            new CreateBean() {
                @Override
                public NamedBean createBean(String systemName, String userName) {
                    return InstanceManager.getDefault(BlockManager.class)
                            .createNewBlock(systemName, userName);
                }
            },
            (NamedBean bean, String property) -> {
                if (!(bean instanceof Block)) {
                    throw new IllegalArgumentException("bean is not a Block");
                }
                InstanceManager.getDefault(BlockManager.class)
                        .deleteBean((Block)bean, property);
            }),

    GlobalVariable(
            Bundle.getMessage("BeanNameGlobalVariable"),
            Bundle.getMessage("BeanNameGlobalVariables"),
            GlobalVariable.class,
            "value",
            () -> InstanceManager.getDefault(GlobalVariableManager.class),
            new CreateBean() {
                @Override
                public NamedBean createBean(String systemName, String userName) {
                    return InstanceManager.getDefault(GlobalVariableManager.class)
                            .createGlobalVariable(systemName, userName);
                }
            },
            (NamedBean bean, String property) -> {
                if (!(bean instanceof GlobalVariable)) {
                    throw new IllegalArgumentException("bean is not a GlobalVariable");
                }
                InstanceManager.getDefault(GlobalVariableManager.class)
                        .deleteBean((GlobalVariable)bean, property);
            }),

    EntryExit(
            Bundle.getMessage("BeanNameEntryExit"),
            Bundle.getMessage("BeanNameEntryExits"),
            DestinationPoints.class,
            "active",
            () -> InstanceManager.getDefault(EntryExitPairs.class),
            null,
            (NamedBean bean, String property) -> {
                if (!(bean instanceof DestinationPoints)) {
                    throw new IllegalArgumentException("bean is not a DestinationPoints");
                }
                InstanceManager.getDefault(EntryExitPairs.class)
                        .deleteBean((DestinationPoints)bean, property);
            }),

    Light(
            Bundle.getMessage("BeanNameLight"),
            Bundle.getMessage("BeanNameLights"),
            Light.class,
            "KnownState",
            () -> InstanceManager.getDefault(LightManager.class),
            new CreateBean() {
                @Override
                public NamedBean createBean(String systemName, String userName) {
                    return InstanceManager.getDefault(LightManager.class)
                            .newLight(systemName, userName);
                }
            },
            (NamedBean bean, String property) -> {
                if (!(bean instanceof Light)) {
                    throw new IllegalArgumentException("bean is not a Light");
                }
                InstanceManager.getDefault(LightManager.class).deleteBean((Light)bean, property);
            }),

    Memory(
            Bundle.getMessage("BeanNameMemory"),
            Bundle.getMessage("BeanNameMemories"),
            Memory.class,
            "value",
            () -> InstanceManager.getDefault(MemoryManager.class),
            new CreateBean() {
                @Override
                public NamedBean createBean(String systemName, String userName) {
                    return InstanceManager.getDefault(MemoryManager.class)
                            .newMemory(systemName, userName);
                }
            },
            (NamedBean bean, String property) -> {
                if (!(bean instanceof Memory)) {
                    throw new IllegalArgumentException("bean is not a Memory");
                }
                InstanceManager.getDefault(MemoryManager.class)
                        .deleteBean((Memory)bean, property);
            }),

    OBlock(
            Bundle.getMessage("BeanNameOBlock"),
            Bundle.getMessage("BeanNameOBlocks"),
            OBlock.class,
            "state",
            () -> InstanceManager.getDefault(OBlockManager.class),
            new CreateBean() {
                @Override
                public NamedBean createBean(String systemName, String userName) {
                    return InstanceManager.getDefault(OBlockManager.class)
                            .createNewOBlock(systemName, userName);
                }
            },
            (NamedBean bean, String property) -> {
                if (!(bean instanceof OBlock)) {
                    throw new IllegalArgumentException("bean is not a OBlock");
                }
                InstanceManager.getDefault(OBlockManager.class)
                        .deleteBean((OBlock)bean, property);
            }),

    Reporter(
            Bundle.getMessage("BeanNameReporter"),
            Bundle.getMessage("BeanNameReporters"),
            Reporter.class,
            "currentReport",
            () -> InstanceManager.getDefault(ReporterManager.class),
            new CreateBean() {
                @Override
                public NamedBean createBean(String systemName, String userName) {
                    return InstanceManager.getDefault(ReporterManager.class)
                            .newReporter(systemName, userName);
                }
            },
            (NamedBean bean, String property) -> {
                if (!(bean instanceof Reporter)) {
                    throw new IllegalArgumentException("bean is not a Reporter");
                }
                InstanceManager.getDefault(ReporterManager.class)
                        .deleteBean((Reporter)bean, property);
            }),

    Sensor(
            Bundle.getMessage("BeanNameSensor"),
            Bundle.getMessage("BeanNameSensors"),
            Sensor.class,
            "KnownState",
            () -> InstanceManager.getDefault(SensorManager.class),
            new CreateBean() {
                @Override
                public NamedBean createBean(String systemName, String userName) {
                    return InstanceManager.getDefault(SensorManager.class)
                            .newSensor(systemName, userName);
                }
            },
            (NamedBean bean, String property) -> {
                if (!(bean instanceof Sensor)) {
                    throw new IllegalArgumentException("bean is not a Sensor");
                }
                InstanceManager.getDefault(SensorManager.class)
                        .deleteBean((Sensor)bean, property);
            }),

    SignalHead(
            Bundle.getMessage("BeanNameSignalHead"),
            Bundle.getMessage("BeanNameSignalHeads"),
            SignalHead.class,
            "Appearance",
            () -> InstanceManager.getDefault(SignalHeadManager.class),
            null,
            (NamedBean bean, String property) -> {
                if (!(bean instanceof SignalHead)) {
                    throw new IllegalArgumentException("bean is not a SignalHead");
                }
                InstanceManager.getDefault(SignalHeadManager.class)
                        .deleteBean((SignalHead)bean, property);
            }),

    SignalMast(
            Bundle.getMessage("BeanNameSignalMast"),
            Bundle.getMessage("BeanNameSignalMasts"),
            SignalMast.class,
            "Aspect",
            () -> InstanceManager.getDefault(SignalMastManager.class),
            null,
            (NamedBean bean, String property) -> {
                if (!(bean instanceof SignalMast)) {
                    throw new IllegalArgumentException("bean is not a SignalMast");
                }
                InstanceManager.getDefault(SignalMastManager.class)
                        .deleteBean((SignalMast)bean, property);
            }),

    Turnout(
            Bundle.getMessage("BeanNameTurnout"),
            Bundle.getMessage("BeanNameTurnouts"),
            Turnout.class, "KnownState",
            () -> InstanceManager.getDefault(TurnoutManager.class),
            new CreateBean() {
                @Override
                public NamedBean createBean(String systemName, String userName) {
                    return InstanceManager.getDefault(TurnoutManager.class)
                            .newTurnout(systemName, userName);
                }
            },
            (NamedBean bean, String property) -> {
                if (!(bean instanceof Turnout)) {
                    throw new IllegalArgumentException("bean is not a Turnout");
                }
                InstanceManager.getDefault(TurnoutManager.class)
                        .deleteBean((Turnout)bean, property);
            }),

    Warrant(
            Bundle.getMessage("BeanNameWarrant"),
            Bundle.getMessage("BeanNameWarrants"),
            Warrant.class, "KnownState",
            () -> InstanceManager.getDefault(WarrantManager.class),
            null,
            (NamedBean bean, String property) -> {
                if (!(bean instanceof Warrant)) {
                    throw new IllegalArgumentException("bean is not a Warrant");
                }
                InstanceManager.getDefault(WarrantManager.class)
                        .deleteBean((Warrant)bean, property);
            });


    private final String _name;
    private final String _namePlural;
    private final Class<? extends NamedBean> _clazz;
    private final String _propertyName;
    private final GetManager _getManager;
    private Manager<? extends NamedBean> _manager;
    private final CreateBean _createBean;
    private final DeleteBean _deleteBean;

    NamedBeanType(
            String name,
            String namePlural,
            Class<? extends NamedBean> clazz,
            String propertyName,
            GetManager getManager,
            CreateBean createBean,
            DeleteBean deleteBean) {
        _name = name;
        _namePlural = namePlural;
        _clazz = clazz;
        _propertyName = propertyName;
        _getManager = getManager;
        _manager = _getManager.getManager();
        _createBean = createBean;
        _deleteBean = deleteBean;
    }

    @Override
    public String toString() {
        return _name;
    }

    public String getName(boolean plural) {
        return plural ? _namePlural : _name;
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

    public CreateBean getCreateBean() {
        return _createBean;
    }

    public DeleteBean getDeleteBean() {
        return _deleteBean;
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

    public interface CreateBean {

        public NamedBean createBean(String systemName, String userName);
    }

    public interface DeleteBean {

        public void deleteBean(NamedBean bean, String property)
                throws java.beans.PropertyVetoException;
    }

}
