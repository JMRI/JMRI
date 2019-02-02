/**
 * This package provides two mechanisms for storing preferences and one
 * mechanism for storing user interface state within a JMRI profile:
 *
 * <dl>
 * <dt>{@link jmri.util.prefs.JmriConfigurationProvider}</dt>
 * <dd>Complex preferences within an XML element provided by the object storing
 * and retrieving the element.</dd>
 * <dt>{@link jmri.util.prefs.JmriPreferencesProvider}</dt>
 * <dd>Simple preferences within a {@link java.util.prefs.Preferences}
 * construct.</dd>
 * <dt>{@link jmri.util.prefs.JmriUserInterfaceConfigurationProvider}</dt>
 * <dd>Complex user interface state within an XML element provided by the object
 * storing and retrieving the element.</dd>
 * </dl>
 *
 * These three classes provide arbitrary read/write access to the
 * underlying storage, such that writing a users preferences does not require
 * any part of a JMRI application have knowledge of every object that manages
 * users preferences.
 * <p>
 * The public interfaces of these classes requires that a
 * {@link jmri.profile.Profile} be passed so that these are ready to (even
 * though nothing currently uses this capability) handle multiple profiles, or
 * non-profile-specific preferences or state.
 * <p>
 * <strong>Note</strong> the above implies that it is critical that no method
 * in any class in this package refer to the results of
{@link jmri.profile.ProfileManager#getActiveProfile()}.
 */
package jmri.util.prefs;
