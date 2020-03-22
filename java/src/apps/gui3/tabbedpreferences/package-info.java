/**
 * Provides the tabbed preferences window and its contents.
 * <p>
 * There is generally only one
 * {@link apps.gui3.tabbedpreferences.TabbedPreferencesFrame} in the system,
 * containing the single {@link apps.gui3.tabbedpreferences.TabbedPreferences}
 * content object. Both are available in the {@link jmri.InstanceManager}.
 * Various classes across JMRI show these by using
 * {@link apps.gui3.tabbedpreferences.TabbedPreferencesAction} instances, or
 * instances of subclasses.
 * <h2>Related Documentation</h2> Please see:
 * <ul>
 * <li><a href=
 * "http://jmri.org/help/en/html/doc/Technical/AppPreferences.shtml">JMRI
 * Preferences tech documentation</a>
 * </ul>
 * <h2>The Startup Process</h2>
 * <ul>
 * <li>During normal startup,
 * {@link apps.gui3.tabbedpreferences.TabbedPreferencesAction} instance(s) are
 * created for menus, etc. They don't reference the
 * {@link apps.gui3.tabbedpreferences.TabbedPreferences} or
 * {@link apps.gui3.tabbedpreferences.TabbedPreferencesFrame} objects at that
 * point.
 * <li>When a {@link apps.gui3.tabbedpreferences.TabbedPreferencesAction} is
 * invoked, it asks the {@link jmri.InstanceManager} for a
 * {@link apps.gui3.tabbedpreferences.TabbedPreferencesFrame} instance, creating
 * and initializing it if needed. That in turn asks the
 * {@link jmri.InstanceManager} for a
 * {@link apps.gui3.tabbedpreferences.TabbedPreferences} instance, creating and
 * initializing it if needed.
 * <li>In some cases, the startup code needs a
 * {@link apps.gui3.tabbedpreferences.TabbedPreferencesFrame} object. In those
 * cases, that code asks the {@link jmri.InstanceManager} for the
 * {@link apps.gui3.tabbedpreferences.TabbedPreferences} instance, again
 * creating and initializing it if needed.
 * </ul>
 * The current (March 2019, JMRI 4.15.3) implementation does all this in the
 * requesting thread, which can take a few seconds when the preferences are
 * eventually requested. That's not an every-time thing, so perhaps that delay
 * is OK, perhaps not. Eventually, it would be desirable to separate the content
 * vs GUI sections of {@link apps.gui3.tabbedpreferences.TabbedPreferences} and
 * defer the GUI creation (which is the majority of the time) to a separate
 * GUI-update thread. <a href="doc-files/TabbedPreferencesCreation.png">
 * <img src="doc-files/TabbedPreferencesCreation.png" style="text-align: right;"
 * alt="TabbedPreferences creation process" height="33%" width="33%"> </a> <!--
 * Put @see and @since tags down here. -->
 *
 * @see jmri.spi.PreferencesManager
 * @see jmri.swing.PreferencesPanel
 * @see apps.AppConfigBase
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({})
package apps.gui3.tabbedpreferences;
