# Actions in this directory or in the user directory can be called before or during a dispatch
import java
import jmri

class Delay(jmri.jmrit.automat.AbstractAutomaton):

    def my_delay(self):
        self.waitMsec(10000)
        
paul =  Delay() 

paul.my_delay()
