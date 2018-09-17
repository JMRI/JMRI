# Fill a memory with a two-digit number, using 10 sensors
# as digit inputs. Use this to e.g. put a keypad on 
# a panel.
#
# Author: Bob Jacobsen, copyright 2008, 2017
# Part of the JMRI distribution
#

import jmri
import java
import java.beans

# Define a listener which appends a single character (from its
# local 'digit' variable) to a memory (in its local 'memory' variable)
#
class NumberInputListener(java.beans.PropertyChangeListener) :
  def propertyChange(self, event) :
    if (event.propertyName == "KnownState" and event.newValue == ACTIVE and event.oldValue != ACTIVE) :
      oldValue = self.memory.getValue()
      self.memory.setValue(oldValue[1:]+self.digit)
      return

# create the memory to use and give it initial contents
memory = memories.provideMemory("IM1")
memory.setValue("00")

# now use this 10 times, for the 10 sensors that give digits

listener = NumberInputListener()
listener.memory = memory
listener.digit = "0"
sensors.provideSensor("IS10").addPropertyChangeListener(listener)

listener = NumberInputListener()
listener.memory = memory
listener.digit = "1"
sensors.provideSensor("IS11").addPropertyChangeListener(listener)

listener = NumberInputListener()
listener.memory = memory
listener.digit = "2"
sensors.provideSensor("IS12").addPropertyChangeListener(listener)

listener = NumberInputListener()
listener.memory = memory
listener.digit = "3"
sensors.provideSensor("IS13").addPropertyChangeListener(listener)

listener = NumberInputListener()
listener.memory = memory
listener.digit = "4"
sensors.provideSensor("IS14").addPropertyChangeListener(listener)

listener = NumberInputListener()
listener.memory = memory
listener.digit = "5"
sensors.provideSensor("IS15").addPropertyChangeListener(listener)

listener = NumberInputListener()
listener.memory = memory
listener.digit = "6"
sensors.provideSensor("IS16").addPropertyChangeListener(listener)

listener = NumberInputListener()
listener.memory = memory
listener.digit = "7"
sensors.provideSensor("IS17").addPropertyChangeListener(listener)

listener = NumberInputListener()
listener.memory = memory
listener.digit = "8"
sensors.provideSensor("IS18").addPropertyChangeListener(listener)

listener = NumberInputListener()
listener.memory = memory
listener.digit = "9"
sensors.provideSensor("IS19").addPropertyChangeListener(listener)



