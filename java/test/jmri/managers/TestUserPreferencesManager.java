package jmri.managers;

/**
 * Mock Implementation for the User Preferences Manager.
 * <P>
 *
 * @see jmri.UserMessageManager
 * @see jmri.managers.DefaultUserMessagePreferences
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class TestUserPreferencesManager extends DefaultUserMessagePreferences {

    public TestUserPreferencesManager() {
        super(false); // don't do superclass initialization 
        if (jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class) == null) {
            //We add this to the instanceManager so that other components can access the preferences
            //We need to make sure that this is registered before we do the read
            jmri.InstanceManager.store(this, jmri.UserPreferencesManager.class);
        }

        preferenceItemDetails(getClassName(), "reminder", "Hide Reminder Location Message");
        classPreferenceList.get(getClassName()).setDescription("User Preferences");
        
        DefaultUserMessagePreferencesHolder.instance = this;
    }

    public void readUserPreferences() { }
    
}
