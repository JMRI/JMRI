package apps.gui3;


/**
 * Tabbed Preferences Action for going direct to Profiles
 *
 * @author Bob Jacobsen (C) 2014
 */
public class TabbedPreferencesProfileAction extends TabbedPreferencesAction {

    public TabbedPreferencesProfileAction() {
        super(Bundle.getMessage("MenuItemPreferencesProfile"));
        //preferencesItem = jmri.profile.Bundle.getMessage("ProfilePreferencesPanel.enabledPanel.TabConstraints.tabTitle");
        preferencesItem = "Profiles";
    }

}
