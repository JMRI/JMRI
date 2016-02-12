package apps.gui3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesProfileAction.class.getName());

}
