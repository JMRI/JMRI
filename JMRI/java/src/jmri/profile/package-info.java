/**
 * JMRI profile management. JMRI uses {@link jmri.profile.Profile}s to provide a
 * mechanism for storing multiple JMRI configurations that can be loaded at
 * application launch time.
 *
 * JMRI searches pre-determined paths for previously unknown Profiles when
 * listing Profiles in the {@link jmri.profile.ProfileManagerDialog} or
 * {@link jmri.profile.ProfilePreferencesPanel}. This allows profiles to be
 * imported into a JMRI instance while that instance is not running.
 */
package jmri.profile;
