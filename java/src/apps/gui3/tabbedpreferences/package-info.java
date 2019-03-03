/**
 * Provides the tabbed preferences window and its contents.
 * <p>
 * There is generally only one {@link TabbedPreferencesFrame} in the system, containing 
 * the single {@link TabbedPreferences} content object. Both are available in the
 * {@link jmri.InstanceManager}.  Various classes across JMRI show these by using
 * {@link TabbedPreferencesAction} instances, or instances of subclasses.
 *
 * <h2>Related Documentation</h2>
 *
 * Please see:
 * <ul>
 * <li><a href="http://jmri.org/help/en/html/doc/Technical/AppPreferences.shtml">JMRI Preferences tech documentation</a>
 * <li><a href="http://jmri.org/help/en/html/doc/Technical/index.shtml">JMRI
 * project technical info</a>
 * </ul>
 * 
 * <h2>The Startup Process</h2>
 *
 * <a href="doc-files/TabbedPreferencesCreation.png">
 *   <img src="doc-files/TabbedPreferencesCreation.png" style="text-align: right;" alt="TabbedPreferences creation process" height="25%" width="25%">
 * </a>
 * 
 * <!-- Put @see and @since tags down here. -->
 *
 * @see jmri.spi.PreferencesManager
 * @see jmri.swing.PreferencesPanel
 * @see apps.AppConfigBase
 */
package apps.gui3.tabbedpreferences;
