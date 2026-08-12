package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;

/**
 * Represents the settings for the Tables menu.
 *
 * @author Bill Hood Copyright (C) 2024
 */
public class TablesSettings {

    private static boolean mainMenuEnabled = false;

    // Load settings when class is initialized
    static {
        load();
    }

    public static boolean isMainMenuEnabled() {
        return mainMenuEnabled;
    }

    public static void setMainMenuEnabled(boolean enabled) {
        mainMenuEnabled = enabled;
    }

    public static void load() {
        UserPreferencesManager prefMgr = InstanceManager.getNullableDefault(UserPreferencesManager.class);
        if (prefMgr != null) {
            Object pref = prefMgr.getProperty("jmri.jmrit.ToolsMenu", "showTablesMenu");
            if (pref instanceof Boolean) {
                mainMenuEnabled = (Boolean) pref;
            }
        }
    }

    public static void save() {
        UserPreferencesManager prefMgr = InstanceManager.getNullableDefault(UserPreferencesManager.class);
        if (prefMgr != null) {
            prefMgr.setProperty("jmri.jmrit.ToolsMenu", "showTablesMenu", mainMenuEnabled);
        }
    }

}
