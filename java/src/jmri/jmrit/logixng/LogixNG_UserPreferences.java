package jmri.jmrit.logixng;

import java.util.HashMap;

/**
 * This class is used for user preferences, like checkboxes for SystemNameAuto.
 * UserPreferencesManager uses reflection to access this class.
 *
 * @author Daniel Bergqvist   Copyright (C) 2021
 */
public class LogixNG_UserPreferences {

    // A default constructor is required for JmriUserPreferencesManager
    public LogixNG_UserPreferences() {
    }

    public String getClassDescription() {
        return Bundle.getMessage("LogixNGReminderMessages");
    }

    public void setMessagePreferencesDetails() {
        HashMap<Integer, String> options = new HashMap<>(3);
        options.put(0x00, Bundle.getMessage("DeleteAsk"));
        options.put(0x01, Bundle.getMessage("DeleteNever"));
        options.put(0x02, Bundle.getMessage("DeleteAlways"));
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMessageItemDetails(LogixNG_UserPreferences.class.getName(),
                "deleteLogixNG", Bundle.getMessage("DeleteItemInUse"), options, 0x00);

        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setPreferenceItemDetails(
                "jmri.jmrit.logixng.LogixNG_UserPreferences", "remindSaveLogixNG", Bundle.getMessage("HideSaveReminder"));

        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setPreferenceItemDetails(
                "jmri.jmrit.logixng.LogixNG_UserPreferences", "remindSaveReLoad", Bundle.getMessage("HideMoveUserReminder"));
    }
}
