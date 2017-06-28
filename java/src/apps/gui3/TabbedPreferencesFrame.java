package apps.gui3;

import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import jmri.InstanceManager;
import jmri.util.JmriJFrame;

/**
 * Provide a preferences window.
 * <P>
 * @author Kevin Dickerson Copyright 2010
 */
public class TabbedPreferencesFrame extends JmriJFrame {

    @Override
    public String getTitle() {
        return getTabbedPreferences().getTitle();
    }

    public boolean isMultipleInstances() {
        return true;
    }

    public TabbedPreferencesFrame() {
        super();
        add(getTabbedPreferences());
        addHelpMenu("package.apps.TabbedPreferences", true); // NOI18N
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    public void gotoPreferenceItem(String item, String sub) {
        getTabbedPreferences().gotoPreferenceItem(item, sub);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (getTabbedPreferences().isDirty()) {
            switch (JOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("UnsavedChangesMessage", getTabbedPreferences().getTitle()), // NOI18N
                    Bundle.getMessage("UnsavedChangesTitle"), // NOI18N
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE)) {
                case JOptionPane.YES_OPTION:
                    // save preferences
                    getTabbedPreferences().savePressed(getTabbedPreferences().invokeSaveOptions());
                    break;
                case JOptionPane.NO_OPTION:
                    // do nothing
                    break;
                case JOptionPane.CANCEL_OPTION:
                default:
                    // abort window closing
                    return;
            }
        }
        this.setVisible(false);
    }

    /**
     * Ensure a TabbedPreferences instance is always available.
     *
     * @return the default TabbedPreferences instance, creating it if needed
     */
    private TabbedPreferences getTabbedPreferences() {
        return InstanceManager.getOptionalDefault(TabbedPreferences.class).orElseGet(() -> {
            return InstanceManager.setDefault(TabbedPreferences.class, new TabbedPreferences());
        });
    }
}
