package jmri.jmrit.logixng.actions;

import jmri.*;

/**
 * Enum of common managers.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public enum CommonManager {

    Sensors(() -> InstanceManager.getNullableDefault(SensorManager.class), Bundle.getMessage("CommonManager_Sensors")),
    Turnouts(() -> InstanceManager.getNullableDefault(TurnoutManager.class), Bundle.getMessage("CommonManager_Turnouts")),
    Lights(() -> InstanceManager.getNullableDefault(LightManager.class), Bundle.getMessage("CommonManager_Lights")),
    SignalHeads(() -> InstanceManager.getNullableDefault(SignalHeadManager.class), Bundle.getMessage("CommonManager_SignalHeads")),
    SignalMasts(() -> InstanceManager.getNullableDefault(SignalMastManager.class), Bundle.getMessage("CommonManager_SignalMasts")),
    Routes(() -> InstanceManager.getNullableDefault(RouteManager.class), Bundle.getMessage("CommonManager_Routes")),
    Blocks(() -> InstanceManager.getNullableDefault(BlockManager.class), Bundle.getMessage("CommonManager_Blocks")),
    Reporters(() -> InstanceManager.getNullableDefault(ReporterManager.class), Bundle.getMessage("CommonManager_Reporters")),
    Memories(() -> InstanceManager.getNullableDefault(MemoryManager.class), Bundle.getMessage("CommonManager_Memories")),
    Audio(() -> InstanceManager.getNullableDefault(AudioManager.class), Bundle.getMessage("CommonManager_Audio")),
    LayoutBlocks(() -> InstanceManager.getNullableDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class), Bundle.getMessage("CommonManager_LayoutBlocks")),
    EntryExit(() -> InstanceManager.getNullableDefault(jmri.jmrit.entryexit.EntryExitPairs.class), Bundle.getMessage("CommonManager_EntryExit")),
    Warrants(() -> InstanceManager.getNullableDefault(jmri.jmrit.logix.WarrantManager.class), Bundle.getMessage("CommonManager_Warrants")),
    Sections(() -> InstanceManager.getNullableDefault(SectionManager.class), Bundle.getMessage("CommonManager_Sections")),
    Transits(() -> InstanceManager.getNullableDefault(TransitManager.class), Bundle.getMessage("CommonManager_Transits"));

    private final String _description;
    private final GetManager _getManager;
    private Manager<? extends NamedBean> _manager;

    private CommonManager(GetManager getManager, String description) {
        _getManager = getManager;
        _manager = _getManager.getManager();
        _description = description;
    }

    public Manager<? extends NamedBean> getManager() {
        return _manager;
    }

    @Override
    public String toString() {
        return _description;
    }

    // This method is used by test classes to reset this enum.
    // Each test resets the InstanceManager so we need to reset the
    // managers in this enum.
    public static void reset() {
        for (CommonManager manager : CommonManager.values()) {
            manager._manager = manager._getManager.getManager();
        }
    }

    private interface GetManager {

        Manager<? extends NamedBean> getManager();
    }

}
