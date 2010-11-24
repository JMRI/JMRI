package apps.gui3;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

    /**
     * Tabbed Preferences Action for dealing with all the preferences in a single view
     * with a list option to the left hand side.
     * <P>
     * @author	Kevin Dickerson   Copyright (C) 2009
     * @version	$Revision: 1.1 $
     */

public class TabbedPreferencesAction extends AbstractAction {

    String gotoListItem = null;
    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */

   public TabbedPreferencesAction(String s, String selection) {
        super(s);
        preferencesItem = selection;
    }

    public TabbedPreferencesAction(String s) {
        super(s);
    }
    
    public TabbedPreferencesAction() { this("Listed Table Access");}
    
    static TabbedPreferencesFrame f;
    String preferencesItem;

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        // create the frame
        if (f==null){
            jmri.InstanceManager.tabbedPreferencesInstance().init();
            f = new TabbedPreferencesFrame(){
            };
        }
        
        f.gotoPreferenceItem(preferencesItem);
        f.pack();
        
        f.setVisible(true);
    }
        
    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }
    
    void setTitle() { //Note required as sub-panels will set them
    }
    
    String helpTarget() {
        return "package.apps.TabbedPreferences";
    }
    
}