"""
Simple examples of getting both simple and complex preferences elements from
the current JMRI Configuration Profile.

Note that although these examples do not show it, the objects returned by
the getPreferences() and getConfiguration() functions in preferences.py can be
used to manipulate as well as read preferences information so that python
scripts do not need to be edited to change preferences or settings.
"""
import preferences

def printSomePreferences():
    """
    Simple examples of getting some JMRI Preferences. Although this example only
    shows getting preferences, both JMRI and arbitrary preferences can be stored
    using the methods outlined in the java.util.prefs.Preferences Javadocs.
    """
    profile = preferences.getPreferences("profile", True)
    # when getting a Preferences value, a key and a default value are passed
    # the default value is returned if the requested key does not exist
    print "Profile name:", profile.get("name", "default value")
    # the default value can be None
    print "Profile id:", profile.get("id", None)
    gui = preferences.getPreferences("apps-gui", True)
    print "Look and Feel:", gui.get("lookAndFeel", None)
    # default value must be an integer for getInt
    print "Font Size:", gui.getInt("fontSize", -1)
    # default value must be boolean for getBoolean
    print "Non-standard Mouse:", gui.getBoolean("nonstandardMouseEvent", False)

def printSomeConfigurations():
    """
    Example of getting a JMRI AuxiliaryConfiguration fragment. A fragment is a
    org.w3c.dom.Element object that is stored as an XML element.
    """
    startupElement = "startup"
    startupNamespace = "http://jmri.org/xml/schema/auxiliary-configuration/startup-4-3-5.xsd"
    # shared should be True if the configuration is common to multiple computers
    shared = True
    # A fragment is defined as any XML element with a name and namespace
    # See the org.w3c.dom.Element Javadocs to manipulate a fragment.
    fragment = preferences.getConfiguration().getConfigurationFragment(startupElement, startupNamespace, shared)
    print "There are", fragment.getElementsByTagName("perform").getLength() , "startup items in this profile."

printSomePreferences()
printSomeConfigurations()
