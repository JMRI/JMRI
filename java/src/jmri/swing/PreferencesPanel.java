package jmri.swing;

import javax.swing.JComponent;

/**
 * An interface to define methods that the Preferences Window can and should
 * expect Preferences panels to implement.
 *
 * This class allows the Preferences Window become less formally aware of all
 * possible preferences settings in JMRI, and to instead interrogate a
 * PreferencesPanel for most of the information that the Preferences window
 * requires to add the PreferencesPanel to the window.
 *
 * @author Randall Wood (C) 2012, 2014
 */
public interface PreferencesPanel {

    /**
     * Get the Preferences Item identifier.
     *
     * Multiple PreferencePanels can be displayed as tabs in a single item.
     * Preferences items are listed in the menu on the left of the preferences
     * window.
     *
     * @return the preferences item identifier.
     */
    public abstract String getPreferencesItem();

    /**
     * Get the text for the Preferences Item in the preferences window list of
     * preferences categories.
     *
     * Multiple PreferencePanels can be displayed as tabs in a single item.
     * Preferences items are listed in the menu on the left of the preferences
     * window.
     *
     * @return the text for the preferences item.
     */
    public abstract String getPreferencesItemText();

    /**
     * Get the title for the tab containing this preferences item.
     *
     * @return a tab title
     */
    public abstract String getTabbedPreferencesTitle();

    /**
     * Text displayed above the preferences panel
     *
     * This label is only displayed if the preferences panel is in a tabbed set
     * of preferences. This label can contain multiple lines.
     *
     * @return label text
     */
    public abstract String getLabelKey();

    /**
     * Get the preferences component for display
     *
     * @return the preferences panel
     */
    public abstract JComponent getPreferencesComponent();

    /**
     * Indicates that this PrefernecesPanel should be stored across application
     * starts by the PreferencesManager
     *
     * This should be true if the implementing class relies on the
     * {@link jmri.ConfigureManager} stores and retrieves the preferences
     * managed by the implementing class on behalf of the implementing class.
     *
     * @return false if the implementing class stores its own preferences
     */
    public abstract boolean isPersistant();

    /**
     * The tooltip to display for a tabbed preferences panel
     *
     * @return tooltip text
     */
    public abstract String getPreferencesTooltip();

    /**
     * Save any changes to preferences.
     *
     * This method is called for every instance of a PreferencesPanel that is
     * loaded by {@link apps.gui3.TabbedPreferences} if {@link #isPersistant()}
     * is false.
     */
    public abstract void savePreferences();

    /**
     * Indicate that preferences need to be saved
     *
     * @return true if preferences need to be saved, false otherwise
     */
    public abstract boolean isDirty();

}
