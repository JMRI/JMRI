package apps.gui3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

    /**
     * Tabbed Preferences Action for going direct to Profiles
     * <P>
     * @author	Bob Jacobsen (C) 2014
     * @version	$Revision$
     */

public class TabbedPreferencesProfileAction extends TabbedPreferencesAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     * @param category
     * @param subCategory
     */

    public TabbedPreferencesProfileAction() { 
        super(Bundle.getMessage("MenuItemPreferencesProfile"));
        //preferencesItem = jmri.profile.Bundle.getMessage("ProfilePreferencesPanel.enabledPanel.TabConstraints.tabTitle");
        preferencesItem = "Profiles";
    }
            
    static Logger log = LoggerFactory.getLogger(TabbedPreferencesProfileAction.class.getName());
    
}
