# Sample script showing how to get the automation manager for OperationsPro.
#
# Author: Daniel Boudreau, copyright 2025
# Part of the JMRI distribution

import jmri

class automationManager(jmri.jmrit.automat.AbstractAutomaton) :      
  def init(self):
    # get the train manager
    self.am = jmri.InstanceManager.getDefault(jmri.jmrit.operations.automation.AutomationManager)
    return

  def handle(self):
    # get a list of automations from the manager
    print ('The number of automations {}'.format(self.am.getSize()))
    
    if self.am.getSize() > 0:
      list = self.am.getAutomationsByNameList()
      for automation in list:
        print ('found automation: {}'.format(automation.getName()))
        # and now run it
        automation.run()
        print ('ran automation: {}'.format(automation.getName()))

    return False              # all done, don't repeat again

automationManager().start()          # create one of these, and start it running
