# Jython Siglet that sets the state of the first output to the state of the first input

import jmri
import jarray

turnout = None

def defineIO():
    global turnout
    turnout = jarray.array(inputs, jmri.NamedBean)[0]

def setOutput():
    turnouts.provide("output").setState(turnout.getState())
