# Sample script showing how to operate a grade crossing with block detection.
#
# This scrip requires an already configured audio buffer loaded with a .wav 
# file of the users choice. It also requires an audio source configure to use
# the audio buffer.
#
# This script will detect a train entering any approach block that is listed in 
# approachBloks and will play the sound (.wave file). The sound will play until
# the train exits all island blocks listed in islandBlocks or if the train backs
# out of the approach block or if the island block is not occupied with in the
# the delay setting (the sound will start again if the island is later occupied).
#
# The ability to control crossing gates and signals can easily be added the the 
# GradeCrossingListener.StartGradeCrossing and 
# GradeCrossingListener.StopGradeCrossing methods.
#
# Author: Keith Ruberson, copyright 2016
# Part of the JMRI distribution
#
import java
import java.util.ArrayList as ArrayList
import jmri
import javax.swing.Timer as Timer
import java.awt.event.ActionListener as ActionListener

bell = audio.provideAudio("IAS1")

# Timer delay in seconds, adjust as necessary.
delay = 20 

# A list that holds each approch block by userName, should contain at least two
# blocks for each track crossing the grade, but may contain more. Adjust the 
# string names as necessary. The blocks named will be created if they do not
# already exist.
approachBlocks = ArrayList(java.util.Arrays.asList("LB1", "LB3", "LB7"))

# A list that holds each island block by userName, should contain one block
# for each track crossing the grade, but may contain more for more than one 
# track. Adjust the string names as necessary. The blocks named will be created
# if they do not already exist.
islandBlocks = ArrayList(java.util.Arrays.asList("LB2"))

class GradeCrossingListener(java.beans.PropertyChangeListener):
    
    # Event handler.
    def propertyChange(self, event):
        
        # Is this event for an approach block?       
        if (approachBlocks.contains(event.source.userName)) :    
            print "change",event.propertyName
            print "source userName", event.source.userName
            
            # Has a train entered the approach block?
            if (event.newValue == jmri.Block.OCCUPIED):
                print "OCCUPIED"
                
                # Are all island block unoocupied? No, break. Yes, start the 
                # grade crossing.
                for i in range(islandBlocks.size()) :
                    if (blocks.getBlock(islandBlocks.get(i)).getState() == jmri.Block.OCCUPIED):
                        break
                    else : 
                        self.StartGradeCrossing()
                        timer.start()
                        
            # Has the train exited the approach block?        
            elif (event.newValue == jmri.Block.UNOCCUPIED) :
                
                # Is any island block occupied? Yes, break. No, stop the grade
                # crossing.
                for i in range(islandBlocks.size()) :
                    if (blocks.getBlock(islandBlocks.get(i)).getState() == jmri.Block.OCCUPIED):
                        break
                    else :
                        print "UNOCCUPIED"
                        self.StopGradeCrossing()
               
        # Is this event for an island block?
        if (islandBlocks.contains(event.source.userName)) :
            print "change",event.propertyName            
            print "source userName", event.source.userName
            
            # Has the train entered the island block?
            if (event.newValue == jmri.Block.OCCUPIED):
                print "OCCUPIED"
                if (bell.getState() == jmri.Audio.STATE_STOPPED):
                    self.StartGradeCrossing()
                    
                else :
                    timer.stop()
                    print "Stopping Timer"
            
            # Has the train exited the island? Yes, stop the grade crossing. No,
            # break.
            for i in range(islandBlocks.size()) :
                    if (blocks.getBlock(islandBlocks.get(i)).getState() == jmri.Block.OCCUPIED):
                        break
                    else :
                        if (bell.getState() == jmri.Audio.STATE_PLAYING) :
                            print "UNOCCUPIED"
                            self.StopGradeCrossing()
                 
            
    # Start the grade crossing method.                
    def StartGradeCrossing(self) :
        print "Playing Sound"                    
        bell.fadeIn()
     
    # Stop the grade crossing method.
    def StopGradeCrossing(self):
        print "Stopping Sound"
        bell.fadeOut()
        timer.stop()              

# Timer elapsed listener
class timerElapsed(ActionListener):
    def actionPerformed(self, e):
        for i in range(islandBlocks.size()) :
            if (blocks.getBlock(islandBlocks.get(i)).getState() == jmri.Block.OCCUPIED):
                break
            else :
                bell.fadeOut()        
                timer.stop()
                print "Timer Stopped Sound"

# Create the grade crossing listener
listener = GradeCrossingListener()

# Create a timer that calls its elaps listener after a delay. Stops the grade
# crossing after the delay if the island is not occupied by a train.
timer = Timer(1000 * delay, timerElapsed())

# Add PropertyChangeListner for each approach block and create the block if it
# does not already exist.
for i in range(approachBlocks.size()) :
    blocks.provideBlock(approachBlocks.get(i)).addPropertyChangeListener(listener)
    
# Add PropertyChangeListner for each island block and create the block if it
# does not already exist.
for i in range(islandBlocks.size()) :
    blocks.provideBlock(islandBlocks.get(i)).addPropertyChangeListener(listener)

