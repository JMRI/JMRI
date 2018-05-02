"""
Python functions for getting the preferences storage objects for the current
JMRI Configuration Profile. See PreferencesExamples.py for usage examples.

These can be used in other python scripts by adding the following statement
to the using script:

import preferences

"""
import jmri

def getPreferences(node, shared = True):
    """
    Return a java.util.prefs.Preferences object from the current JMRI
    Configuration Profile for the given node. If shared is True, return
    preferences that are common to all computers using this Configuration
    Profile, otherwise return preferences specific to this computer.
    
    A Preferences object can be used to store simple preferences values. See
    the Javadocs for java.util.prefs.Preferences for more information.
    
    Preferences objects are stored in the JMRI portable paths
    profile:profile/profile.properties if shared, and
    profile:profile/<node-identity>/profile.properties if not shared.
    """
    project = jmri.profile.ProfileManager.getDefault().getActiveProfile()
    root = jmri.profile.ProfileUtils.getPreferences(project, None, shared)
    return root.node(node)

def getConfiguration():
    """
    Return a jmri.profile.AuxiliaryConfiguration object from the current JMRI
    Configuration Profile.
    
    An AuxiliaryConfiguration object can be used to store XML elements. See
    the Javadocs for jmri.profile.AuxiliaryConfiguration for more information.
    
    Configurations are stored in the JMRI portable paths
    profile:profile/profile.xml if shared, and
    profile:profile/<node-identity>/profile.xml if not shared.
    """
    project = jmri.profile.ProfileManager.getDefault().getActiveProfile()
    return jmri.profile.ProfileUtils.getAuxiliaryConfiguration(project)
