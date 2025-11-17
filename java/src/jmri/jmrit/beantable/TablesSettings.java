package jmri.jmrit.beantable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the settings for the Tables menu.
 *
 * @author Bill Hood Copyright (C) 2024
 */
public class TablesSettings {

    private static boolean mainMenuEnabled = true;

    public static boolean isMainMenuEnabled() {
        return mainMenuEnabled;
    }

    public static void setMainMenuEnabled(boolean enabled) {
        mainMenuEnabled = enabled;
    }

    public static void load() {
        // This will be implemented later to load from XML
    }

    public static void save() {
        // This will be implemented later to save to XML
    }

    private final static Logger log = LoggerFactory.getLogger(TablesSettings.class);
}
