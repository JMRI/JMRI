package apps.gui3;


/**
 * Tabbed Preferences Action for going direct to Profiles
 * <P>
 * @author	Bob Jacobsen (C) 2014
 * @version	$Revision$
 */
public class TabbedPreferencesProfileAction extends TabbedPreferencesAction {

    /**
     *
     */
    private static final long serialVersionUID = 422306041173570634L;

    public TabbedPreferencesProfileAction() {
        super(Bundle.getMessage("MenuItemPreferencesProfile"));
        //preferencesItem = jmri.profile.Bundle.getMessage("ProfilePreferencesPanel.enabledPanel.TabConstraints.tabTitle");
        preferencesItem = "Profiles";
    }

}
