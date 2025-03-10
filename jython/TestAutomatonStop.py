



import jarray
import jmri

class KillTest(jmri.jmrit.automat.AbstractAutomaton) :

    def handle(self):
        # we'll use this to wait forever
        snd = jmri.jmrit.Sound("resources/sounds/Crossing.wav")


        # play the sound once
        while (True): 
            snd.play()
            self.waitMsec(20)
        
        # Thread.stop() during the above would not execute the following line
        # Thread.interrupt() runs the following line
        print "Executing after attempt to stop"
        return False
        
KillTest().start()
