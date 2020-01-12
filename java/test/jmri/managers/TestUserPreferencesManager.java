package jmri.managers;

/**
 * Mock Implementation for the User Preferences Manager.
 *
 * @see jmri.UserPreferencesManager
 * @see jmri.managers.JmriUserPreferencesManager
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class TestUserPreferencesManager extends JmriUserPreferencesManager {

    public TestUserPreferencesManager() {
        this.setPreferenceItemDetails(getClassName(), "reminder", "Hide Reminder Location Message");
        this.getClassPreferences(getClassName()).setDescription("User Preferences");
    }

    @Override
    protected void showMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember, int type) {
        // Uncomment to force failure if wanting to verify that showMessage does not get called.
        //org.slf4j.LoggerFactory.getLogger(TestUserPreferencesManager.class).error("showMessage called.", new Exception());
    }

}
