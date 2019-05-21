package apps.gui3.tabbedpreferences;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import jmri.InstanceManager;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action launches the tabbed preferences window.
 *
 * The {@link TabbedPreferencesFrame} object is requested from the InstanceManager, and
 * if need-be created and initialized in the process of doing that.
 *
 * @author Kevin Dickerson Copyright (C) 2009
 * @author Bob Jacobsen Copyright (C) 2019
 */
public class TabbedPreferencesAction extends jmri.util.swing.JmriAbstractAction {

    String preferencesItem = null;
    String preferenceSubCat = null;

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s           action title
     * @param category    action category
     * @param subCategory action sub-category
     */
    public TabbedPreferencesAction(String s, String category, String subCategory) {
        super(s);
        preferencesItem = category;
        preferenceSubCat = subCategory;
    }

    public TabbedPreferencesAction(String s, String category) {
        super(s);
        preferencesItem = category;
    }

    public TabbedPreferencesAction(String s) {
        super(s);
    }

    public TabbedPreferencesAction() {
        this(Bundle.getMessage("MenuItemPreferences"));
    }

    public TabbedPreferencesAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public TabbedPreferencesAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public TabbedPreferencesAction(String s, WindowInterface wi, String category, String subCategory) {
        super(s, wi);
        preferencesItem = category;
        preferenceSubCat = subCategory;
    }

    public TabbedPreferencesAction(String s, Icon i, WindowInterface wi, String category) {
        super(s, i, wi);
        preferencesItem = category;
    }

    final public void actionPerformed() {
        TabbedPreferencesFrame f = InstanceManager.getOptionalDefault(TabbedPreferencesFrame.class).orElseGet(() -> {
            return InstanceManager.setDefault(TabbedPreferencesFrame.class, new TabbedPreferencesFrame());
        });
            
        showPreferences(f);

    }

    private void showPreferences(TabbedPreferencesFrame f) {
        // Update the GUI Look and Feel
        // This is needed as certain controls are instantiated
        // prior to the setup of the Look and Feel
        
        // might not be a preferences item set yet
        if (preferencesItem != null) f.gotoPreferenceItem(preferencesItem, preferenceSubCat);
        
        f.pack();

        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }

    void setTitle() { //Note required as sub-panels will set them
    }

    String helpTarget() {
        return "package.apps.TabbedPreferences";
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesAction.class);

}
