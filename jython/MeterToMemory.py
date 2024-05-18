# Continuously copy the contents of available Meters to memory variables
#
# Bob Jacobsen (C) 2024
# This script is part of the JMRI distribution


import jmri

class MeterToMemory(jmri.jmrit.automat.AbstractAutomaton) :

   def init(self) :
       self.setName("MeterToMemory")
       return

   def handle(self) :
       mm = jmri.InstanceManager.getNullableDefault(jmri.MeterManager)

       for meter in mm.getNamedBeanSet() :
           name = meter.getDisplayName()
           memory = memories.provideMemory("IMeter$"+name)
           memory.setValue(meter.getKnownAnalogValue())

       self.waitMsec(1000)
       return True

MeterToMemory().start()
