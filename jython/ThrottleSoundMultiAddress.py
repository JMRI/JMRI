# This script as described in the file preamble, is based on the JMRI
# ThrottleSound.py script, but has been altered to allow the user to link
# wav files to an unlimited (seemingly)number of loco addresses. I am
# using 114 loco addresses and files at the moment. Wav files are played
# for specified loco address on handheld, command station, of virtual JMRI
# throttles when F3 is pressed. I experience some fussiness used through
# loconet: sometimes double pressing F3 is needed, and system asks to
# steal loco address, and a failure to play at times. This seems a
# function of the complexity of loconet, as the script runs seamlessly
# when created and tested with virtual throttle on loconet simulator.
# 
# Still, you may enjoy this or find answers of your own. Users can
# determine their own needs. I use it to read off loco information about
# the model, the prototype, and info about consisting and speed
# matching.....as opposed to me looking for the correct page in my info
# binders or turning my head from the layout operation to read something.
# 
# It has many presentational possibilites also. I have expanded it to also
# link to historical data about the prototype and the rail line etc. Guest
# users and I can access this info at any time by pressing F3 on the
# throttle (though I believe I can also alter it to use one or more
# additional throttle keys.) As with all scripts, accuracy if critical in
# writing your lines, copying and pasting wav file names etc. A single
# error/omission in any of the hundreds of lines will cause the script to
# not perform. Various formats can be used for initial line entries. This
# writer used a1= a2= a3=....a114= etc. Though formats cannot be mixed. As
# for the data itself, to facilitate this use, I scan print data and
# convert information to .wav format using a good version of
# text-to-speech software. Producing or live recording the amount of data
# I currently have available for playback with the throttle would be
# daunting, but not impossible for those with their own ideas. 
# 
# Obviously this is a simple adaptation of others' creative work. The original
# authors of the ThrottleSound.py script and Ken Cameron very kindly
# showed me the template for proceeding.
# 


# Play sound file for any particular loco address when the F3 key is pressed on the 
# handheld or JMRI
#
# This sample shows script for only17 files.  This writer is currently using 114 files for 114
# loco addresses elsewhere
#
# Only the first .wav file in the sample is part of the JMRI distribution
# This script is based on the ThrottleSound script authored by 
# Peter Lloyd-Jones, Bob Jacobsen, copyright 2006
# Part of the JMRI distribution
#
# following an idea by Dave Heine 
#

import jmri
import java
import java.beans

# Define the listener class to play a sound when a  throttle function changes
class ThrottleListener(java.beans.PropertyChangeListener):

    def propertyChange(self, event):
      if ((event.propertyName == "F3") and (event.newValue == True) and (event.oldValue == False)) :
         self.snd.play()
     print "F3"

# create a specific listener object, with a specific sound
Script111 = ThrottleListener()
Script111.snd = jmri.jmrit.Sound("resources/sounds/bell.wav")
Script6121 = ThrottleListener()
Script6121.snd = jmri.jmrit.Sound("resources/sounds/PRR6121sd45.wav")
Script404 = ThrottleListener()
Script404.snd = jmri.jmrit.Sound("resources/sounds/ATSF404GE44TONNER.wav")
Script3014 = ThrottleListener()
Script3014.snd = jmri.jmrit.Sound("resources/sounds/ATSF3014.wav")
Script3015 = ThrottleListener()
Script3015.snd = jmri.jmrit.Sound("resources/sounds/ATSF3015.wav")
Script3019 = ThrottleListener()
Script3019.snd = jmri.jmrit.Sound("resources/sounds/ATSF3019.wav")
Script3400 = ThrottleListener()
Script3400.snd = jmri.jmrit.Sound("resources/sounds/BO3400EMDGP7.wav")
Script803 = ThrottleListener()
Script803.snd = jmri.jmrit.Sound("resources/sounds/BCR803AlcoC424.wav")
Script805 = ThrottleListener()
Script805.snd = jmri.jmrit.Sound("resources/sounds/BCR805AlcoC424.wav")
Script812 = ThrottleListener()
Script812.snd = jmri.jmrit.Sound("resources/sounds/BCR812AlcoC424.wav")
Script370 = ThrottleListener()
Script370.snd = jmri.jmrit.Sound("resources/sounds/B&O370EMDF7A.wav")
Script6904 = ThrottleListener()
Script6904.snd = jmri.jmrit.Sound("resources/sounds/BO6904EMDGP30.wav")
Script6311 = ThrottleListener()
Script6311.snd = jmri.jmrit.Sound("resources/sounds/PRR6311c628.wav")
Script231 = ThrottleListener()
Script231.snd = jmri.jmrit.Sound("resources/sounds/SantaFe231ALCOc415.wav")
Script7404 = ThrottleListener()
Script7404.snd = jmri.jmrit.Sound("resources/sounds/CP7404EMDSW9.wav")
Script1000 = ThrottleListener()
Script1000.snd = jmri.jmrit.Sound("resources/sounds/BNSF1000GEDash9CW.wav")


# define class to request a throttle, wait for it, then attach a listener
class BlowWhistle(jmri.jmrit.automat.AbstractAutomaton) :

          def init(self):

                  # get the throttle object
                  throttle = self.getThrottle(self.number, self.long)
                  if (throttle == None) :
                          print "Couldn't assign throttle!"
                  # set listener
                  throttle.addPropertyChangeListener(self.lis)
                  return

          # handle() does nothing on this one
          def handle(self):
                return  0   # want to end

# run it


print "Lets Go!"
a = BlowWhistle()
a.number = 111
a.long = True
a.lis = Script111
a.start()

print "Lets Go!"
b = BlowWhistle()
b.number = 6121
b.long = True
b.lis = Script6121
b.start()

print "Lets Go!"
c = BlowWhistle()
c.number = 404
c.long = True
c.lis = Script404
c.start()

print "Lets Go!"
d = BlowWhistle()
d.number = 3014
d.long = True
d.lis = Script3014
d.start()

print "Lets Go!"
e = BlowWhistle()
e.number = 3015
e.long = True
e.lis = Script3015
e.start()

print "Lets Go!"
f = BlowWhistle()
f.number = 3019
f.long = True
f.lis = Script3019
f.start()

print "Lets Go!"
g = BlowWhistle()
g.number = 3400
g.long = True
g.lis = Script3400
g.start()

print "Lets Go!"
h = BlowWhistle()
h.number = 803
h.long = True
h.lis = Script803
h.start()

print "Lets Go!"
i= BlowWhistle()
i.number = 805
i.long = True
i.lis = Script805
i.start()

print "Lets Go!"
j = BlowWhistle()
j.number = 812
j.long = True
j.lis = Script812
j.start()

print "Lets Go!"
k = BlowWhistle()
k.number = 370
k.long = True
k.lis = Script370
k.start()

print "Lets Go!"
l = BlowWhistle()
l.number = 6904
l.long = True
l.lis = Script6904
l.start()

print "Lets Go!"
m = BlowWhistle()
m.number = 6311
m.long = True
m.lis = Script6311
m.start()

print "Lets Go!"
n = BlowWhistle()
n.number = 231
n.long = True
n.lis = Script231
n.start()

print "Lets Go!"
o = BlowWhistle()
o.number = 7404
o.long = True
o.lis = Script7404
o.start()

print "Lets Go!"
p = BlowWhistle()
p.number = 1000
p.long = True
p.lis = Script1000
p.start()

print "Lets Go!"
q = BlowWhistle()
q.number = 803
q.long = True
q.lis = Script803
q.start()
