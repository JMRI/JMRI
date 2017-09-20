package apps.gui3;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tabbed Preferences Action for dealing with all the preferences in a single
 * view with a list option to the left hand side.
 *
 * @author Kevin Dickerson Copyright (C) 2009
 */
public class TabbedPreferencesAction extends jmri.util.swing.JmriAbstractAction {

    // must be null until first use to allow app initialization before construction
    static TabbedPreferencesFrame f = null;
    String preferencesItem = null;
    String preferenceSubCat = null;
    static boolean inWait = false;

    /**
     * Create an action with a specific title.
     * <P>
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

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        // create the frame
        if (inWait) {
            log.info("We are already waiting for the preferences to be displayed");
            return;
        }

        if (f == null) {
            f = new TabbedPreferencesFrame();
            Thread preferencesInitThread = new Thread(() -> {
                final Object waiter = new Object();
                try {
                    setWait(true);
                    while (jmri.InstanceManager.getDefault(TabbedPreferences.class).init() != TabbedPreferences.INITIALISED) {
                        synchronized (waiter) {
                            waiter.wait(50);
                        }
                    }
                    SwingUtilities.updateComponentTreeUI(f);
                    showPreferences();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    setWait(false);
                }
            });
            preferencesInitThread.setName("TabbedPreferencesAction actionPerformed");
            preferencesInitThread.start();
        } else {
            showPreferences();
        }
    }

    private void showPreferences() {
        // Update the GUI Look and Feel
        // This is needed as certain controls are instantiated
        // prior to the setup of the Look and Feel
        setWait(false);
        
        // might not be a preferences item set yet
        if (preferencesItem != null) f.gotoPreferenceItem(preferencesItem, preferenceSubCat);
        
        f.pack();

        f.setVisible(true);
    }

    synchronized static void setWait(boolean boo) {
        inWait = boo;
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

    private final static Logger log = LoggerFactory.getLogger(TabbedPreferencesAction.class);

}
