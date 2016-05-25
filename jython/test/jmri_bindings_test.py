# Test for jmri_bindings_test.py
import jmri

if (GREEN != jmri.SignalHead.GREEN) : raise AssertionError('GREEN Failed')
if (YELLOW != jmri.SignalHead.YELLOW) : raise AssertionError('YELLOW Failed')
if (RED != jmri.SignalHead.RED) : raise AssertionError('RED Failed')

if (ON != jmri.Light.On) : raise AssertionError('ON Failed')
if (OFF != jmri.Light.Off) : raise AssertionError('OFF Failed')
