# Test the TurnoffXmlValidation.py script
import jmri

# capture before values
v1before = jmri.InstanceManager.getDefault(jmri.ConfigureManager).getValidate()
v2before = jmri.jmrit.XmlFile.getDefaultValidate()

execfile("jython/TurnOffXmlValidation.py")

# capture after values
v1after = jmri.InstanceManager.getDefault(jmri.ConfigureManager).getValidate()
v2after = jmri.jmrit.XmlFile.getDefaultValidate()

# restore before
jmri.InstanceManager.getDefault(jmri.ConfigureManager).setValidate(v1before)
jmri.jmrit.XmlFile.setDefaultValidate(v2before)

# check results
if (v1after != jmri.jmrit.XmlFile.Validate.None) : raise AssertionError('ConfigureManager not set')
if (v2after != jmri.jmrit.XmlFile.Validate.None) : raise AssertionError('XmlFile not set')
