package apps.gui3.tabbedpreferences;

/**
 * Tabbed Preferences Action for going direct to Profiles.
 *<p>
 * Most of the behavior comes from the {@link TabbedPreferencesAction} superclass;
 * this just adds a specific target.
 *
 * @author Bob Jacobsen (C) 2014, 2019
 */
public class TabbedPreferencesProfileAction extends TabbedPreferencesAction {

    public TabbedPreferencesProfileAction() {
        super(Bundle.getMessage("MenuItemPreferencesProfile"));
        preferencesItem = "Profiles";
    }

}
