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
public class TestUserPreferencesManager extends JmriUserPreferencesManager {

    public TestUserPreferencesManager() {
        if (jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class) == null) {
            //We add this to the instanceManager so that other components can access the preferences
            //We need to make sure that this is registered before we do the read
            jmri.InstanceManager.store(this, jmri.UserPreferencesManager.class);
        }

        preferenceItemDetails(getClassName(), "reminder", "Hide Reminder Location Message");
        this.getClassPreferences(getClassName()).setDescription("User Preferences");
    }

    @Override
    protected void showMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember, int type) {
        // Uncomment to force failure if wanting to verify that showMessage does not get called.
        //org.slf4j.LoggerFactory.getLogger(TestUserPreferencesManager.class).error("showMessage called.", new Exception());
    }

}
