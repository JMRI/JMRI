# Test access to legacy names
import jmri

jmri.util.JUnitUtil.initShutDownManager()

jmri.Manager.legacyNameSet.clear() # just in case

# start actual test
if (jmri.Manager.legacyNameSet.size() != 0) : raise AssertionError('Not empty at first')

jmri.Manager.getSystemPrefix("DCCPPS01")
if (jmri.Manager.legacyNameSet.size() != 1) : raise AssertionError('Didn\'t catch a reference')

jmri.Manager.getSystemPrefix("IS01")
if (jmri.Manager.legacyNameSet.size() != 1) : raise AssertionError('Tagged one in error')

# there should be a ShutDownTask registered, remove it
jmri.InstanceManager.getDefault(jmri.ShutDownManager).deregister(jmri.Manager.legacyReportTask)
